package com.ismail.nooktv;

import org.json.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/** Bounded HTTP server serving only bundled editor assets and scoped configuration APIs. */
public final class EditorServer {
    public interface Host {
        byte[] asset(String name) throws IOException;
        JSONObject config() throws Exception;
        JSONObject save(JSONObject input, long revision, String mutation) throws Exception;
        default JSONObject restore(JSONObject input,long revision,String mutation) throws Exception { throw new IllegalArgumentException("Import unavailable"); }
        default JSONObject inventory() throws Exception { return new JSONObject().put("apps",new JSONArray()); }
        void approve(String name);
    }
    private final Pairing pairing;
    private final Host host;
    private ServerSocket listener;
    private ExecutorService pool;
    private final Set<String> hosts = ConcurrentHashMap.newKeySet();
    private final Set<Socket> clients = ConcurrentHashMap.newKeySet();
    public EditorServer(Pairing pairing, Host host) { this.pairing=pairing; this.host=host; }
    public synchronized List<String> start() throws IOException {
        if(listener!=null) return addresses();
        listener=new ServerSocket(); listener.setReuseAddress(true); listener.bind(new InetSocketAddress(8765));
        hosts.clear();
        for(NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) for(InetAddress a : Collections.list(ni.getInetAddresses()))
            if(a instanceof Inet4Address && (a.isSiteLocalAddress()||a.isLoopbackAddress())) hosts.add(a.getHostAddress()+":8765");
        pool=new ThreadPoolExecutor(2,4,30,TimeUnit.SECONDS,new ArrayBlockingQueue<>(8),new ThreadPoolExecutor.AbortPolicy());
        ServerSocket socket=listener;
        Thread acceptor=new Thread(()->{
            while(!socket.isClosed()) try {
                Socket client=socket.accept();
                if(!client.getInetAddress().isSiteLocalAddress()&&!client.getInetAddress().isLoopbackAddress()) { client.close(); continue; }
                clients.add(client);
                try { pool.execute(()->serve(client)); } catch(RejectedExecutionException e) { clients.remove(client); client.close(); }
            } catch(IOException e) { break; }
        },"nook-editor"); acceptor.setDaemon(true); acceptor.start(); return addresses();
    }
    private List<String> addresses() { List<String> result=new ArrayList<>(); for(String h:hosts) if(!h.startsWith("127.")) result.add("http://"+h); Collections.sort(result); return result; }
    public synchronized void stop() { if(listener!=null) try { listener.close(); } catch(IOException ignored) {} listener=null; if(pool!=null) pool.shutdownNow(); for(Socket client:clients) try { client.close(); } catch(IOException ignored) {} clients.clear(); pairing.cancel(); }
    private void serve(Socket socket) {
        try(Socket client=socket) {
          try {
            client.setSoTimeout(4000); InputStream in=client.getInputStream(); ByteArrayOutputStream header=new ByteArrayOutputStream(); int b,tail=0;
            while((b=in.read())!=-1) { header.write(b); tail=(tail<<8)|b; if(header.size()>8192) { reply(client,413,"text/plain",bytes("Headers too large")); return; } if(tail==0x0d0a0d0a) break; }
            String[] lines=new String(header.toByteArray(),StandardCharsets.US_ASCII).split("\r\n");
            String[] request=lines[0].split(" "); if(request.length!=3) return;
            Map<String,String> h=new HashMap<>();
            for(int i=1;i<lines.length;i++) { int colon=lines[i].indexOf(':'); if(colon<1) throw new IOException(); String k=lines[i].substring(0,colon).toLowerCase(Locale.ROOT); if(h.put(k,lines[i].substring(colon+1).trim())!=null) throw new IOException(); }
            String authority=h.get("host"), method=request[0], path=request[1];
            if(!Targets.allowedHost(authority,hosts) || h.containsKey("transfer-encoding")) { reply(client,403,"text/plain",bytes("Invalid host")); return; }
            boolean api=path.startsWith("/api/");
            if(api && (!"editor-v1".equals(h.get("x-nook-client")) || (h.containsKey("origin")&&!h.get("origin").equals("http://"+authority)) || (method.equals("POST")&&!Objects.equals(h.get("origin"),"http://"+authority)))) { reply(client,403,"text/plain",bytes("Invalid editor origin")); return; }
            if(!api && method.equals("GET") && (path.equals("/")||path.equals("/editor.js")||path.equals("/editor.css"))) {
                reply(client,200,path.endsWith(".js")?"application/javascript":path.endsWith(".css")?"text/css":"text/html",host.asset(path.equals("/")?"editor.html":path.substring(1))); return;
            }
            if(!api) { reply(client,404,"text/plain",bytes("Not found")); return; }
            int length=Integer.parseInt(h.getOrDefault("content-length","0"));
            if(length<0||length>1000000) { reply(client,413,"text/plain",bytes("Body too large")); return; }
            if(method.equals("POST")&&!"application/json".equals(h.get("content-type"))) { reply(client,415,"text/plain",bytes("JSON required")); return; }
            byte[] body=new byte[length]; for(int pos=0;pos<length;) { int n=in.read(body,pos,length-pos); if(n<0) throw new IOException(); pos+=n; }
            JSONObject data=length==0?new JSONObject():new JSONObject(new String(body,StandardCharsets.UTF_8));
            JSONObject result=new JSONObject(); int status=200;
            if(path.equals("/api/pair")&&method.equals("POST")) {
                String name=data.optString("name").trim(); if(name.isEmpty()||name.length()>40) throw new IllegalArgumentException("Use a name of 1–40 characters");
                String ticket=pairing.request(data.optString("code"),name);
                if(ticket==null) { status=403; result.put("error","Code expired, incorrect, locked, or another request is pending. Start pairing again on TV."); }
                else { result.put("ticket",ticket); host.approve(name); }
            } else if(path.equals("/api/claim")&&method.equals("POST")) {
                String[] session=pairing.claim(data.optString("ticket"));
                if(session==null) { status=202; result.put("status","Awaiting TV approval; pairing expires after 2 minutes."); }
                else { result.put("token",session[0]); result.put("csrf",session[1]); }
            } else {
                String auth=h.getOrDefault("authorization",""); String token=auth.startsWith("Bearer ")?auth.substring(7):"";
                if(!pairing.authorized(token,h.get("x-nook-csrf"))) { reply(client,401,"application/json",bytes("{\"error\":\"Pair this browser again on the TV.\"}")); return; }
                if(path.equals("/api/config")&&method.equals("GET")) result=host.config();
                else if(path.equals("/api/inventory")&&method.equals("GET")) result=host.inventory();
                else if(path.equals("/api/config")&&method.equals("POST")) result=host.save(data.getJSONObject("config"),data.getLong("expectedRevision"),data.getString("mutationId"));
                else if(path.equals("/api/import")&&method.equals("POST")) result=host.restore(data.getJSONObject("config"),data.getLong("expectedRevision"),data.getString("mutationId"));
                else { status=404; result.put("error","Not found"); }
            }
            reply(client,status,"application/json",bytes(result.toString()));
          } catch(java.util.ConcurrentModificationException e) { reply(client,409,"application/json",bytes("{\"error\":\"Dashboard changed elsewhere. Reload before saving.\"}")); }
            catch(IllegalArgumentException|JSONException e) { reply(client,400,"application/json",bytes("{\"error\":\"Invalid configuration or request. Check names, URLs and limits.\"}")); }
            catch(Exception e) { reply(client,503,"application/json",bytes("{\"error\":\"TV unavailable or unable to save. Reopen Nook and reload.\"}")); }
        } catch(Exception ignored) { /* Client disconnect or timed-out connection. No request secrets logged. */ }
        finally { clients.remove(socket); }
    }
    private static byte[] bytes(String value) { return value.getBytes(StandardCharsets.UTF_8); }
    private static void reply(Socket socket,int code,String type,byte[] body) throws IOException {
        OutputStream out=socket.getOutputStream();
        String reason=code==200?"OK":code==202?"Accepted":"Error";
        String headers="HTTP/1.1 "+code+" "+reason+"\r\nContent-Type: "+type+"; charset=utf-8\r\nContent-Length: "+body.length+"\r\nConnection: close\r\nCache-Control: no-store\r\nX-Content-Type-Options: nosniff\r\nReferrer-Policy: no-referrer\r\nContent-Security-Policy: default-src 'none'; script-src 'self'; style-src 'self'; connect-src 'self'; frame-ancestors 'none'; base-uri 'none'; form-action 'self'\r\n\r\n";
        out.write(bytes(headers)); out.write(body); out.flush();
    }
}
