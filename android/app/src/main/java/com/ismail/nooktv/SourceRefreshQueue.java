package com.ismail.nooktv;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/** Fetch workers never wait for work queued onto themselves. */
final class SourceRefreshQueue implements AutoCloseable {
    private final ExecutorService workers = new ThreadPoolExecutor(2, 2, 30, TimeUnit.SECONDS,
        new ArrayBlockingQueue<Runnable>(6), r -> { Thread t = new Thread(r, "nook-source-fetch"); t.setDaemon(true); return t; });
    private boolean running, closed;

    synchronized boolean start(List<Runnable> tasks, Runnable completed) {
        if (closed || running || tasks.isEmpty()) return false;
        running = true;
        AtomicInteger remaining = new AtomicInteger(tasks.size());
        Runnable done = () -> {
            if (remaining.decrementAndGet() == 0) {
                synchronized (SourceRefreshQueue.this) {
                    running = false;
                    if (closed) return;
                }
                completed.run();
            }
        };
        for (Runnable task : tasks) {
            try { workers.execute(() -> { try { task.run(); } finally { done.run(); } }); }
            catch (RejectedExecutionException e) { done.run(); }
        }
        return true;
    }

    @Override public synchronized void close() { closed = true; workers.shutdownNow(); }
}
