package org.neteinstein.androiddispatchcenterproject;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import org.neteinstein.androiddispatch.AndroidDispatch;

/**
 * Created by PVicente on 28/02/2015.
 */
public class UITestClass implements AndroidDispatch.DispatchObserverCallBack {

    private int maxThreads = 3;
    private A a;
    private B b;
    private C c;
    private TextView mTextView;
    private Class[] classList;

    public UITestClass(TextView textView) {
        a = new A();
        b = new B();
        c = new C();
        classList = new Class[] { a.getClass(), b.getClass(), c.getClass() };
        mTextView = textView;
    }

    public void testDispatch() {
        AndroidDispatch androidDispatch = AndroidDispatch.getInstance(maxThreads);
        androidDispatch.register(a.getClass(), a.maxThreads, null);
        androidDispatch.register(b.getClass(), b.maxThreads, null);
        androidDispatch.register(c.getClass(), c.maxThreads, null);

        int pointer = 0;
        int counter= 0;
        for (int i = 0; i < 30; i++) {
            if (pointer > classList.length - 1) {
                pointer = 0;
            }
            final Class clazz = classList[pointer];
            pointer++;

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Log.d(clazz.toString(), "\nStarting " + "from " + clazz);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Log.d(clazz.toString(), "\nRunnable from " + clazz);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            mTextView.append("\nRunnable from " + clazz);
                        }
                    });

                }
            };
            String id = androidDispatch.dispatch(clazz, r);
            Log.d(clazz.toString(), "\nDispatched " + counter + " - " + id + "from " + clazz);

            counter++;
        }

    }

    @Override
    public void dispatchFinished(String dispatchId, Class clazz) {
        Log.d(clazz.toString(), "\nDispatch Finished " + dispatchId + "from " + clazz);
    }

    private class A {
        int maxThreads = 1;
    }

    private class B {
        int maxThreads = 3;
    }

    private class C {
        int maxThreads = 1;
    }
}
