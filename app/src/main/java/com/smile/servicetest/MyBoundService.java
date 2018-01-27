package com.smile.servicetest;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MyBoundService extends Service {

    public static final String ActionName = "com.smile.servicetest.MyBoundService";

    private String TAG = "com.smile.servicetest.MyBoundService";
    private MediaPlayer mediaPlayer = null;
    private Thread backgroundThread = null;

    private IBinder myBinder = new MyBinder();

    public MyBoundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"backgroundThread running");
                playMusic();
            }
        });

        backgroundThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Service started by startService()");
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Thread dummy = backgroundThread;
        backgroundThread = null;
        dummy.interrupt();

        myBinder = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");
        return myBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind called");
        return super.onUnbind(intent);
    }

    public class MyBinder extends Binder {
        MyBoundService getService() {
            return MyBoundService.this;
        }
    }

    public void startPlay() {
        if (mediaPlayer != null) {

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }

    }

    public void pausePlay() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
            }
        }

    }

    private void playMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.music_a);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
    }
}
