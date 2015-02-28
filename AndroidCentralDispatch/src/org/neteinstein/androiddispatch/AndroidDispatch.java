package org.neteinstein.androiddispatch;

import org.neteinstein.androiddispatch.queues.DispatchQueue;
import org.neteinstein.androiddispatch.threadpools.ThreadPoolUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by PVicente on 27/02/2015.
 */
public class AndroidDispatch {
    private static AndroidDispatch ourInstance;
    private int mMaxPoolSize;
    private ConcurrentHashMap<Class, DispatchQueue> queueMap = null;
    private ThreadPoolExecutor mExecutor = null;
    private ThreadPoolExecutor mDispatchThread = null;

    public synchronized static AndroidDispatch getInstance(int maxPoolSize) {
        if (ourInstance == null) {
            ourInstance = new AndroidDispatch(maxPoolSize);
        }
        return ourInstance;
    }

    private AndroidDispatch() {
    }

    private AndroidDispatch(int maxPoolSize) {
        mMaxPoolSize = maxPoolSize;
        queueMap = new ConcurrentHashMap<Class, DispatchQueue>();
        mDispatchThread = ThreadPoolUtils.getFixedThreadPool("DispatchThread", 1, 1);
        mExecutor = ThreadPoolUtils.getFixedThreadPool("AndroidDispatchTP", maxPoolSize, maxPoolSize);
    }

    public void destroy() {
        mDispatchThread.shutdownNow();
        mExecutor.shutdownNow();
        for (DispatchQueue dispatchQueue : queueMap.values()) {
            dispatchQueue.destroy(true);
        }
        queueMap.clear();
    }

    public void register(int classMaxThreads) {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Class clazz = stackTraceElements[1].getClass();

        DispatchQueue dispatchQueue = new DispatchQueue(mExecutor, classMaxThreads);
        queueMap.put(clazz, dispatchQueue);
    }

    public void register(Class clazz, int classMaxThreads) throws Exception {
        if (clazz == null || classMaxThreads <= 0) {
            throw new IllegalArgumentException("Invalid register");
        }

        DispatchQueue dispatchQueue = new DispatchQueue(mExecutor, classMaxThreads);
        queueMap.put(clazz, dispatchQueue);
    }

    public void unregister() {
        unregister(false);
    }

    public void unregister(boolean interruptPendingTasks) {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Class clazz = stackTraceElements[1].getClass();

        DispatchQueue dispatchQueue = queueMap.remove(clazz);
        if (dispatchQueue != null && interruptPendingTasks) {
            // What should happen here?
            dispatchQueue.destroy(interruptPendingTasks);
        }
    }

    public void unregister(Class clazz, boolean interruptPendingTasks) {
        if (clazz == null) {
            throw new IllegalArgumentException("Invalid unregister");
        }

        DispatchQueue dispatchQueue = queueMap.remove(clazz);
        if (dispatchQueue != null && interruptPendingTasks) {
            // What should happen here?
            dispatchQueue.destroy(interruptPendingTasks);
        }
    }

    public void submitTask(final DispatchRunnable r) throws Exception {
        if (r == null) {
            throw new IllegalArgumentException("Invalid task");
        }

        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        final Class clazz = stackTraceElements[1].getClass();

        final DispatchQueue dispatchQueue = queueMap.get(clazz);
        if (dispatchQueue == null) {
            throw new IllegalArgumentException("Submiting unregisted class tasks");
        }

        r.setAndroidDispatch(this);
        r.setClazz(clazz);

        dispatchQueue.dispatch(r);
        reEvaluateQueue(dispatchQueue);

    }

    public void submitTask(Class clazz, final DispatchRunnable r) throws Exception {
        if (clazz == null) {
            throw new IllegalArgumentException("Submiting unregisted class tasks");
        }

        final DispatchQueue dispatchQueue = queueMap.get(clazz);
        if (dispatchQueue == null) {
            throw new IllegalArgumentException("Submiting unregisted class tasks");
        }

        r.setAndroidDispatch(this);
        r.setClazz(clazz);

        dispatchQueue.dispatch(r);
        reEvaluateQueue(dispatchQueue);

    }

    private void reEvaluateQueue(final DispatchQueue dispatchQueue) {
        mDispatchThread.submit(new Runnable() {
            @Override
            public void run() {
                dispatchQueue.reEvaluateQueue();
            }
        });
    }

    public int getMaxPoolSize() {
        return mMaxPoolSize;
    }

    public void setMaxPoolSize(int mMaxPoolSize) {
        this.mMaxPoolSize = mMaxPoolSize;
    }

    public void signalFinish(Class clazz) {
        DispatchQueue dispatchQueue = queueMap.get(clazz);
        if (dispatchQueue != null) {
            dispatchQueue.releaseThread();
            reEvaluateQueue(dispatchQueue);
        }
    }
}
