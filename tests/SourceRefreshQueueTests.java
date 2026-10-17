package com.ismail.nooktv;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class SourceRefreshQueueTests {
    private static int count;
    private static void check(boolean value,String message){count++;if(!value)throw new AssertionError(message);}
    public static void main(String[] args)throws Exception{
        SourceRefreshQueue queue=new SourceRefreshQueue();
        CountDownLatch started=new CountDownLatch(2),release=new CountDownLatch(1),finished=new CountDownLatch(1);
        AtomicInteger active=new AtomicInteger(),peak=new AtomicInteger(),completed=new AtomicInteger();
        List<Runnable> tasks=new ArrayList<>();
        for(int i=0;i<6;i++)tasks.add(()->{int n=active.incrementAndGet();peak.accumulateAndGet(n,Math::max);started.countDown();try{release.await(3,TimeUnit.SECONDS);}catch(InterruptedException e){Thread.currentThread().interrupt();}finally{active.decrementAndGet();completed.incrementAndGet();}});
        check(queue.start(tasks,finished::countDown),"six source jobs accepted");
        check(started.await(2,TimeUnit.SECONDS),"both fetch workers run without a waiting coordinator");
        check(!queue.start(tasks,()->{}),"overlapping refresh rejected");
        release.countDown();
        check(finished.await(3,TimeUnit.SECONDS),"six sources finish promptly instead of timing out behind coordinator");
        check(completed.get()==6&&peak.get()==2,"all six jobs execute with at most two active fetches");
        CountDownLatch again=new CountDownLatch(1);
        check(queue.start(Collections.singletonList(()->{}),again::countDown),"next refresh available after completion");
        check(again.await(2,TimeUnit.SECONDS),"next refresh completes");
        queue.close();
        check(!queue.start(tasks,()->{}),"destroyed manager cannot restart workers");
        System.out.println("PASS: "+count+" source refresh scheduling assertions");
    }
}
