package org.neteinstein.androiddispatch.queues;

import org.neteinstein.androiddispatch.DispatchRunnable;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by PVicente on 27/02/2015.
 */
public class DispatchQueue {

    private ThreadPoolExecutor executor;

    private final Queue<DispatchRunnable> tasks = new LinkedList<DispatchRunnable>();
    private AtomicInteger availableThreadsForQueue = new AtomicInteger(0);

    public DispatchQueue(ThreadPoolExecutor executor, int classMaxThreads) {
        this.executor = executor;
        availableThreadsForQueue.set(classMaxThreads);
    }

    public void destroy(boolean mayInterruptThreads) {
        tasks.clear();
        if (mayInterruptThreads) {
            executor.shutdown();
        } else {
            executor.shutdownNow();
        }
        executor = null;
    }

    public void dispatch(DispatchRunnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task may not be null");
        }

        synchronized (tasks) {
            tasks.add(task);

            reEvaluateQueue();
        }
    }

    public void releaseThread() {
        availableThreadsForQueue.incrementAndGet();
    }

    public void reEvaluateQueue() {
        int availableThreads = availableThreadsForQueue.get();
        while (availableThreads > 0) {
            executor.submit(tasks.poll());
            availableThreads = availableThreadsForQueue.decrementAndGet();
        }
    }
}
