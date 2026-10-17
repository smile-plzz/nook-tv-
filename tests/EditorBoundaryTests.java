package com.ismail.nooktv;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;

/** Exercises the actual request handler without opening a network listener. */
public final class EditorBoundaryTests {
    private static int count;
    private static void check(boolean value,String message) { count++; if(!value) throw new AssertionError(message); }
    static final class Connection extends Socket {
        final InputStream in; final ByteArrayOutputStream out=new ByteArrayOutputStream();
        Connection(String request) { in=new ByteArrayInputStream(request.getBytes(StandardCharsets.UTF_8)); }
        @Override public InputStream getInputStream() { return in; }
        @Override public OutputStream getOutputStream() { return out; }
        @Override public void setSoTimeout(int timeout) {}
        @Override public synchronized void close() {}
    }
    static final class Host implements EditorServer.Host {
        int saves, approvals,uploads,uploadedBytes; long revision;
        public byte[] asset(String name) { return "fixture".getBytes(StandardCharsets.UTF_8); }
        public JSONObject config() throws Exception { return new JSONObject().put("revision",revision); }
        public JSONObject save(JSONObject c,long expected,String mutation) throws Exception { if(expected!=revision) throw new ConcurrentModificationException(); saves++; revision++; return config(); }
        public JSONObject media() throws Exception { return new JSONObject().put("items",new JSONArray()); }
        public JSONObject upload(InputStream in,long length,String name,String id,java.util.function.BooleanSupplier allowed)throws Exception{uploads++;if(!allowed.getAsBoolean())throw new AssertionError("Upload auth");for(int n=0;n<length;n++){if(in.read()<0)throw new EOFException();uploadedBytes++;}return new JSONObject().put("id",id).put("label",name);}
        public void approve(String name) { approvals++; }
    }
    static String invoke(EditorServer server,String method,String path,String headers,String body) throws Exception {
        Connection connection=new Connection(method+" "+path+" HTTP/1.1\r\n"+headers+"Content-Length: "+body.getBytes(StandardCharsets.UTF_8).length+"\r\n\r\n"+body);
        Method serve=EditorServer.class.getDeclaredMethod("serve",Socket.class); serve.setAccessible(true); serve.invoke(server,connection); return connection.out.toString("UTF-8");
    }
    public static void main(String[] args) throws Exception {
        Pairing pairing=new Pairing(); Host host=new Host(); EditorServer server=new EditorServer(pairing,host);
        Field hosts=EditorServer.class.getDeclaredField("hosts"); hosts.setAccessible(true); ((Set<String>)hosts.get(server)).add("192.168.1.2:8765");
        String base="Host: 192.168.1.2:8765\r\nX-Nook-Client: editor-v1\r\n", origin="Origin: http://192.168.1.2:8765\r\nContent-Type: application/json\r\n";
        String r=invoke(server,"GET","/", "Host: 192.168.1.2:8765\r\n",""); check(r.startsWith("HTTP/1.1 200"),"bundled editor reachable"); check(r.contains("frame-ancestors 'none'"),"frame policy");
        check(invoke(server,"GET","/api/config",base,"").startsWith("HTTP/1.1 401"),"private reads need auth");
        check(invoke(server,"GET","/api/config",base.replace("192.168.1.2","evil.example"),"").startsWith("HTTP/1.1 403"),"host rebinding rejected");
        check(invoke(server,"POST","/api/pair",base+origin.replace("http://192.168.1.2:8765","https://evil.example"),"{}").startsWith("HTTP/1.1 403"),"foreign origin rejected");
        check(invoke(server,"POST","/api/pair",base+"Content-Type: application/json\r\n","{}").startsWith("HTTP/1.1 403"),"mutations require origin");
        check(invoke(server,"POST","/api/pair",base+origin.replace("application/json","text/plain"),"{}").startsWith("HTTP/1.1 415"),"simple form content blocked");
        String code=pairing.begin();
        r=invoke(server,"POST","/api/pair",base+origin,new JSONObject().put("code",code).put("name","PC").toString());
        check(r.startsWith("HTTP/1.1 200")&&host.approvals==1,"pairing calls TV approval"); String ticket=new JSONObject(r.substring(r.indexOf("\r\n\r\n")+4)).getString("ticket");
        String claim=new JSONObject().put("ticket",ticket).toString();
        check(invoke(server,"POST","/api/claim",base+origin,claim).startsWith("HTTP/1.1 202"),"no token before TV approval");
        pairing.approve(); r=invoke(server,"POST","/api/claim",base+origin,claim); JSONObject session=new JSONObject(r.substring(r.indexOf("\r\n\r\n")+4));
        String auth="Authorization: Bearer "+session.getString("token")+"\r\nX-Nook-CSRF: "+session.getString("csrf")+"\r\n";
        check(invoke(server,"GET","/api/config",base+auth,"").startsWith("HTTP/1.1 200"),"paired read allowed");
        check(invoke(server,"GET","/api/config",base+auth.replace(session.getString("csrf"),"bad"),"").startsWith("HTTP/1.1 401"),"invalid CSRF rejected");
        String save=new JSONObject().put("expectedRevision",0).put("mutationId","valid-mutation-id").put("config",new JSONObject()).toString();
        check(invoke(server,"POST","/api/config",base+origin+auth,save).startsWith("HTTP/1.1 200")&&host.saves==1,"authenticated mutation");
        check(invoke(server,"POST","/api/config",base+origin+auth,save).startsWith("HTTP/1.1 409")&&host.saves==1,"stale edit conflict");
        check(invoke(server,"POST","/api/config",base+origin+auth,"{bad").startsWith("HTTP/1.1 400"),"malformed JSON error");
        check(invoke(server,"POST","/api/config",base+origin+auth,String.join("",Collections.nCopies(1000001,"x"))).startsWith("HTTP/1.1 413"),"bounded request body");
        check(invoke(server,"GET","/api/config",base+auth+"Transfer-Encoding: chunked\r\n","").startsWith("HTTP/1.1 403"),"ambiguous body framing blocked");
        String raw="0000ftyp0000data",binary=origin.replace("application/json","application/octet-stream")+"X-Nook-Filename: holiday%20video.mov\r\nX-Nook-Upload-Id: 0123456789abcdef0123456789abcdef\r\n";
        check(invoke(server,"GET","/api/media",base+auth,"").startsWith("HTTP/1.1 200"),"authenticated media listing");
        check(invoke(server,"POST","/api/media/upload",base+binary,raw).startsWith("HTTP/1.1 401")&&host.uploads==0,"raw upload authenticates before host body ingestion");
        check(invoke(server,"POST","/api/media/upload",base+binary+auth,raw).startsWith("HTTP/1.1 200")&&host.uploads==1&&host.uploadedBytes==raw.length(),"raw body streamed without JSON parsing");
        check(invoke(server,"POST","/api/media/upload",base+binary.replace("http://192.168.1.2:8765","https://evil.example")+auth,raw).startsWith("HTTP/1.1 403")&&host.uploads==1,"raw upload foreign origin rejected");
        check(invoke(server,"POST","/api/media/upload",base+origin+auth,raw).startsWith("HTTP/1.1 415"),"raw route rejects JSON upload content type");
        check(invoke(server,"POST","/api/media/upload",base+binary+auth,"tiny").startsWith("HTTP/1.1 413"),"raw route validates file size");
        check(invoke(server,"POST","/api/media/upload",base+binary+auth+"Transfer-Encoding: chunked\r\n",raw).startsWith("HTTP/1.1 403"),"raw route rejects ambiguous chunk framing");
        pairing.revoke();check(invoke(server,"POST","/api/media/upload",base+binary+auth,raw).startsWith("HTTP/1.1 401")&&host.uploads==1,"revoked upload blocked");
        check(invoke(server,"POST","/api/config",base+origin+auth,save).startsWith("HTTP/1.1 401")&&host.saves==1,"revoke prevents writes");
        System.out.println("PASS: "+count+" actual editor-handler assertions (in-memory transport; no device/network claim)");
    }
}
