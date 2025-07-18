package com.csl.cs108ademoapp;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncTaskA {
    protected void onPreExecute() { }

    protected String doInBackground(Void... a) {
        return null;
    }

    protected void onCancelled() { }

    protected void onPostExecute(String result) { }

    public enum Status {
        PENDING,
        RUNNING,
        FINISHED,
    }

    Status status = Status.PENDING;
    public Status getStatus() {
        return status;
    }

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnableBackground = new Runnable() {
        @Override
        public void run() {
            status = Status.RUNNING;
            String string = doInBackground();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onPostExecute(string);
                }
            });
            status = Status.FINISHED;
        }
    };
    public void execute() {
        onPreExecute();
        executor.execute(runnableBackground);
    }

    boolean mCancelled = false;
    public boolean isCancelled() {
        return mCancelled;
    }
    public boolean cancel(boolean doit) {
        if (doit) {
            MainActivity.csLibrary4A.appendToLog("AsyncA.cancel");
            handler.removeCallbacks(runnableBackground);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onCancelled();
                }
            });
            status = Status.FINISHED;
            mCancelled = true;
        }
        return true;
    }

    protected void onProgressUpdate(String... output) { }
    protected void publishProgress(String... vv) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                onProgressUpdate(vv);
            }
        });
    }
}
