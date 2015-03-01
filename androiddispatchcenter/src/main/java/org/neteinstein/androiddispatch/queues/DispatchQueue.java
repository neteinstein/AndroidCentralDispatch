package org.neteinstein.androiddispatch.queues;

import org.neteinstein.androiddispatch.AndroidDispatch;
import org.neteinstein.androiddispatch.DispatchRunnable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by PVicente on 27/02/2015.
 */
public class DispatchQueue {

    private ThreadPoolExecutor mExecutor;

    private final List<DispatchRunnable> mTasks = new LinkedList<DispatchRunnable>();
    private AtomicInteger mAvailableThreadsForQueue = new AtomicInteger(0);
    private AndroidDispatch.DispatchObserverCallBack mCallback;

    public DispatchQueue(ThreadPoolExecutor executor, int classMaxThreads,
                         AndroidDispatch.DispatchObserverCallBack callback) {
        this.mExecutor = executor;
        this.mAvailableThreadsForQueue.set(classMaxThreads);
        this.mCallback = callback;
    }

    public void destroy(boolean mayInterruptThreads) {
        mTasks.clear();
        if (mayInterruptThreads) {
            mExecutor.shutdown();
        } else {
            mExecutor.shutdownNow();
        }
        mExecutor = null;
        mCallback = null;
    }

    public void dispatch(DispatchRunnable task) {
        if (task == null) {
            throw new IllegalArgumentException("Task may not be null");
        }

        synchronized (mTasks) {
            mTasks.add(task);

            reEvaluateQueue();
        }
    }

    public void releaseThread() {
        mAvailableThreadsForQueue.incrementAndGet();
    }

    public Map<String, Future> reEvaluateQueue() {
        final Map<String, Future> futuresOfDispatchs = new HashMap<>();
        int availableThreads = mAvailableThreadsForQueue.get();
        while (availableThreads > 0 && mTasks.size() > 0) {
            DispatchRunnable dispatch = mTasks.remove(0);
            Future future = mExecutor.submit(dispatch);
            futuresOfDispatchs.put(dispatch.getDispatchId(), future);
            availableThreads = mAvailableThreadsForQueue.decrementAndGet();
        }

        return futuresOfDispatchs;
    }

    public boolean cancelDispatch(String dispatchId) {
        boolean hasCancelledDispatch = false;

        Iterator<DispatchRunnable> iterator = mTasks.iterator();
        while (iterator.hasNext()) {
            DispatchRunnable dispatch = iterator.next();
            if (dispatchId.equals(dispatch.getDispatchId())) {
                iterator.remove();
                hasCancelledDispatch = true;
            }

        }
        return hasCancelledDispatch;
    }

    public AndroidDispatch.DispatchObserverCallBack getCallback() {
        return mCallback;
    }
}
