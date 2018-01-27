package com.smile.servicetest;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;

public class MyStartedService extends Service {

    public static final String ActionName = "com.smile.servicetest.MyStartedService";

    public MyStartedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // return super.onStartCommand(intent, flags, startId);
        Thread thread = new Thread() {
            @Override
            public void run() {
                int sum = 0;    // the sum of 1 ~ 100
                for (int i=1; i<=100; i++) {
                    sum += i;
                }
                stopSelf();

                // sending broadcast
                // String notification = getApplicationContext().getPackageName();
                // String ActionName = getApplicationContext().getClass().getName();

                Intent notificationIntent = new Intent(ActionName);
                Bundle extras = new Bundle();
                extras.putInt("SUM", sum);
                notificationIntent.putExtras(extras);

                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                localBroadcastManager.sendBroadcast(notificationIntent);
            }
        };

        thread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
