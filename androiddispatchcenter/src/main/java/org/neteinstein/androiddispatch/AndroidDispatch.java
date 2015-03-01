package org.neteinstein.androiddispatch;

import org.neteinstein.androiddispatch.queues.DispatchQueue;
import org.neteinstein.androiddispatch.threadpools.ThreadPoolUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by PVicente on 27/02/2015.
 */
public class AndroidDispatch {
    private static AndroidDispatch ourInstance;
    private int mMaxPoolSize;
    private ConcurrentHashMap<Class, DispatchQueue> queueMap = null;
    private ConcurrentHashMap<String, Future> futureMap = null;
    private DispatchThreadPoolExecutor mExecutor = null;
    private ThreadPoolExecutor mDispatchThread = null;

    public interface DispatchObserverCallBack {
        public void dispatchFinished(final String dispatchId, final Class clazz);
    }

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
        futureMap = new ConcurrentHashMap<String, Future>();
        mDispatchThread = ThreadPoolUtils.getFixedThreadPool("DispatchThread", 1, 1);
        mExecutor = ThreadPoolUtils.getFixedThreadPool("AndroidDispatchTP", maxPoolSize, maxPoolSize);
        mExecutor.setAndroidDispatch(this);
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
        register(classMaxThreads, null);
    }

    public void register(int classMaxThreads, DispatchObserverCallBack callback) {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        Class clazz = stackTraceElements[1].getClass();

        register(clazz, classMaxThreads, callback);
    }

    public void register(Class clazz, int classMaxThreads, DispatchObserverCallBack callback) {
        if (clazz == null || classMaxThreads <= 0) {
            throw new IllegalArgumentException("Invalid register");
        }

        DispatchQueue dispatchQueue = new DispatchQueue(mExecutor, classMaxThreads, callback);
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
            // TODO: Define behaviour
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

    public void dispatch(final Runnable r) throws Exception {
        if (r == null) {
            throw new IllegalArgumentException("Invalid task");
        }

        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        final Class clazz = stackTraceElements[1].getClass();

        dispatch(clazz, r);

    }

    public String dispatch(Class clazz, final Runnable r) {
        if (clazz == null) {
            throw new IllegalArgumentException("Submiting unregisted class tasks");
        }

        final DispatchQueue dispatchQueue = queueMap.get(clazz);
        if (dispatchQueue == null) {
            throw new IllegalArgumentException("Submiting unregisted class tasks");
        }
        final String dispatchId = clazz.getSimpleName() + System.currentTimeMillis();
        final DispatchRunnable runnable = new DispatchRunnable(this, dispatchId, clazz, r);
        dispatchQueue.dispatch(runnable);
        reEvaluateQueue(dispatchQueue);

        return dispatchId;
    }

    public void cancelDispatch(String dispatchId) {
        final StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
        final Class clazz = stackTraceElements[1].getClass();

        cancelDispatch(dispatchId, clazz);
    }

    public void cancelDispatch(String dispatchId, Class clazz) {
        if (dispatchId == null || clazz == null) {
            throw new IllegalArgumentException("Invalid dispatchId or class");
        }

        DispatchQueue dispatchQueue = queueMap.get(clazz);
        if (dispatchQueue != null) {
            if (!dispatchQueue.cancelDispatch(dispatchId)) {
                if (futureMap.containsKey(dispatchId)) {
                    futureMap.get(dispatchId).cancel(true);
                }
            }
        }
    }

    private void reEvaluateQueue(final DispatchQueue dispatchQueue) {
        mDispatchThread.submit(new Runnable() {
            @Override
            public void run() {
                Map<String, Future> newFutures = dispatchQueue.reEvaluateQueue();
                if (newFutures.size() > 0) {
                    futureMap.putAll(newFutures);
                }
            }
        });
    }

    public void dispatchFinished(final String dispatchId, final Class clazz) {
        DispatchQueue dispatchQueue = queueMap.get(clazz);
        if (dispatchQueue != null) {
            dispatchQueue.releaseThread();
            reEvaluateQueue(dispatchQueue);
            final DispatchObserverCallBack callback = dispatchQueue.getCallback();
            if (callback != null) {
                callback.dispatchFinished(dispatchId, clazz);
            }
        }
    }
}
