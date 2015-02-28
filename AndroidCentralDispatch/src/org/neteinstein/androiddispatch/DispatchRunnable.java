package org.neteinstein.androiddispatch;

/**
 * Created by PVicente on 28/02/2015.
 */
public abstract class DispatchRunnable implements Runnable {

    private AndroidDispatch androidDispatch = null;
    private Class clazz = null;

    AndroidDispatch getAndroidDispatch() {
        return androidDispatch;
    }

    void setAndroidDispatch(AndroidDispatch androidDispatch) {
        this.androidDispatch = androidDispatch;
    }

    void signalFinish() {
        androidDispatch.signalFinish(clazz);
    }

    Class getClazz() {
        return clazz;
    }

    void setClazz(Class clazz) {
        this.clazz = clazz;
    }
}
