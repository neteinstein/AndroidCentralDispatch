package org.neteinstein.androiddispatch.threadpools;

import org.neteinstein.androiddispatch.DispatchThreadPoolExecutor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An utils to create thread pools with readable names
 */
public class ThreadPoolUtils {

    public static final AtomicInteger mThreadTotal = new AtomicInteger(0);

    /**
     * Get ThreadPool with fixed number of threads
     * 
     * @param threadPoolName The name of the thread pool
     * @param MAX_THREAD Max number of threads
     * @return Thread Pool
     */
    public static DispatchThreadPoolExecutor getFixedThreadPool(final String threadPoolName, final int CORE_THREADS,
            final int MAX_THREAD) {
        DispatchThreadPoolExecutor threadPoolExecutor = new DispatchThreadPoolExecutor(CORE_THREADS, MAX_THREAD, 0L,
                TimeUnit.MILLISECONDS, new NamedThreadFactory(threadPoolName, MAX_THREAD));
        return threadPoolExecutor;
    }

    /**
     * Factory for threads that provides custom naming
     */
    private static class NamedThreadFactory implements ThreadFactory {

        private final ThreadGroup mGroup;
        private final AtomicInteger mThreadPoolNumber = new AtomicInteger(0);
        private final String mNamePrefix;
        private final int mMaxFactoryThreads;

        /**
         * Default constructor
         * 
         * @param threadPoolName The name of the ThreadPool
         */
        NamedThreadFactory(String threadPoolName, int maxFactoryThreads) {
            SecurityManager s = System.getSecurityManager();
            mGroup = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            mNamePrefix = threadPoolName;
            mMaxFactoryThreads = maxFactoryThreads;
        }

        /**
         * Creates a new named thread
         * 
         * @param r Runnable
         * @return Thread
         */
        public Thread newThread(Runnable r) {

            int threadNumber = mThreadPoolNumber.incrementAndGet();
            final String threadName = mNamePrefix + " [#" + threadNumber + "/" + mMaxFactoryThreads + "]";

            Thread t = new Thread(mGroup, r, threadName, 0);

            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }

            mThreadTotal.incrementAndGet();
            return t;
        }
    }
}
