package org.neteinstein.androiddispatch;

/**
 * Created by PVicente on 28/02/2015.
 */
public final class DispatchRunnable implements Runnable {

    private AndroidDispatch mAndroidDispatch = null;
    private String mDispatchId;
    private Class mClazz = null;
    private Runnable mRunnable;

    public DispatchRunnable(AndroidDispatch androidDispatch, String id, Class clazz, Runnable runnable) {
        mAndroidDispatch = androidDispatch;
        mDispatchId = id;
        mClazz = clazz;
        mRunnable = runnable;
    }

    @Override
    public void run() {
        mRunnable.run();
        mAndroidDispatch.dispatchFinished(mDispatchId, mClazz);
    }

    public String getDispatchId() {
        return mDispatchId;
    }

    public Class getClazz() {
        return mClazz;
    }
}
