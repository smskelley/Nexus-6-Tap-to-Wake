/* Copyright (c) 2014 Sean Kelley */
package com.codechron.n6doubletapunlock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Created by psion on 11/22/14.
 */
public class PersistAtBootReceiver extends BroadcastReceiver {

    private DoubleTapUnlocker unlocker;

    private Context parentContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()))
        {
            parentContext = context;
            Log.e("N6DoubleTap", "PersistAtBootReceiver was called.");
            (new AsyncTask<Void,Void,Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    unlocker = new DoubleTapUnlocker(parentContext);
                    unlocker.RestoreState();
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                    Log.e("N6DoubleTap", "PersistAtBootReceiver Restored state.");
                }

            }).execute();
        }
    }
}
