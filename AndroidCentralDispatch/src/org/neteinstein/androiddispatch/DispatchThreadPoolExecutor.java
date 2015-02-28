/*
 * Pedro Vicente - neteinstein.org
 */

package org.neteinstein.androiddispatch;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * This is an extension of a ThreadPoolExecutor that implements specific dispatch methods
 */
public class DispatchThreadPoolExecutor extends ThreadPoolExecutor {

    /**
     * DispatchThreadPoolExecutor constructor
     * 
     * @param corePoolSize Pool size
     * @param maxPoolSize Max threads
     * @param keepAlive Keep alive time
     * @param timeUnit Time unit
     * @param threadFactory Thread Factory
     */
    public DispatchThreadPoolExecutor(int corePoolSize, int maxPoolSize, long keepAlive, TimeUnit timeUnit,
            final ThreadFactory threadFactory) {
        super(corePoolSize, maxPoolSize, keepAlive, timeUnit, new LinkedBlockingQueue<Runnable>(), threadFactory);
    }

    @Override
    public void execute(Runnable task) {
        if (task == null) {
            // Log
            return;
        }
        super.execute(task);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        if (task == null) {
            // Log
            return null;
        }
        final RunnableFuture<Object> futureTask = newTaskFor(task, null);
        execute(futureTask);
        return futureTask;
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                Future<?> future = (Future<?>) r;
                if (future.isDone()) {
                    future.get();
                }
                if (r instanceof DispatchRunnable) {
                    DispatchRunnable dispatchRunnable = ((DispatchRunnable) r);
                    dispatchRunnable.signalFinish();
                }
            } catch (CancellationException ce) {
                // LOG
            } catch (InterruptedException ie) {
                // LOG
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                t = e.getCause();
            }
        }

        if (t != null) {
            // LOG
        }
    }

}
