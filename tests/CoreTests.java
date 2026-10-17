package com.ismail.nooktv;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
public final class CoreTests {
    private static int count;
    private static void check(boolean b,String name) { count++; if(!b) throw new AssertionError(name); }
    private static void bad(String url) { boolean rejected=false; try { Targets.https(url); } catch(IllegalArgumentException e) { rejected=true; } check(rejected,"URL rejected: "+url); }
    public static void main(String[] args) {
        AtomicLong now=new AtomicLong(1000); Pairing p=new Pairing(now::get);
        String code=p.begin(); check(code.matches("[0-9]{6}"),"six digits");
        String ticket=p.request(code,"PC"); check(ticket!=null&&ticket.length()==64,"high entropy ticket"); check(p.claim(ticket)==null,"approval required");
        p.approve(); String[] session=p.claim(ticket); check(session!=null,"approved claim"); check(p.authorized(session[0],session[1]),"valid credentials"); check(!p.authorized(session[0],"bad"),"CSRF required"); check(!p.authorized("",session[1]),"token required"); check(p.claim(ticket)==null,"single use ticket");
        p.revoke(); check(!p.authorized(session[0],session[1]),"revoked immediately");
        code=p.begin(); for(int i=0;i<5;i++) p.request("not-code","PC"); check(p.request(code,"PC")==null,"brute force lock");
        code=p.begin(); now.addAndGet(120001); check(p.request(code,"PC")==null,"expiry");
        code=p.begin(); ticket=p.request(code,"PC"); p.approve(); now.addAndGet(120001); check(p.claim(ticket)==null,"claim expiry");
        bad("javascript:alert(1)"); bad("file:///etc/passwd"); bad("http://example.com"); bad("https://user:password@example.com"); bad("https://example.com:8765"); bad("https://");
        check(Targets.youtube("https://youtu.be/test"),"YouTube short host"); check(!Targets.youtube("https://youtube.com.evil.example/watch?v=x"),"spoof rejected");
        Set<String> hosts=new HashSet<>(Arrays.asList("192.168.1.2:8765")); check(Targets.allowedHost("192.168.1.2:8765",hosts),"exact host"); check(!Targets.allowedHost("attacker.example:8765",hosts),"rebinding host");
        System.out.println("PASS: "+count+" core assertions");
    }
}
