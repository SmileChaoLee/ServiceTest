package com.smile.servicetest;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MyBoundService extends Service {

    public static final String ActionName = "com.smile.servicetest.MyBoundService";
    public static final int ServiceStopped = 0x00;
    public static final int ServiceStarted = 0x01;
    public static final int MusicPlaying = 0x02;
    public static final int MusicPaused = 0x03;

    public static final int BinderIPC = 1;
    public static final int MessengerIPC = 2;

    private String TAG = "com.smile.servicetest.MyBoundService";
    private MediaPlayer mediaPlayer = null;
    private Thread backgroundThread = null;

    // create a Binder for communicate with clients using this Binder
    private IBinder serviceBinder = new ServiceBinder();
    public class ServiceBinder extends Binder {
        MyBoundService getService() {
            return MyBoundService.this;
        }
    }

    // create a Messenger for communicate with clients using this Messenger
    private Messenger serviceMessenger = new Messenger(new ServiceHandler());
    private class ServiceHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // setting responce message
            Message responseMsg = Message.obtain(null, msg.what, 0, 0);

            switch (msg.what) {
                case ServiceStopped:
                    terminateService(MessengerIPC);
                    break;
                case ServiceStarted:
                    // does not do anything
                    break;
                case MusicPlaying:
                    playMusic(MessengerIPC);
                    break;
                case MusicPaused:
                    pauseMusic(MessengerIPC);
                    break;
                default:
                    super.handleMessage(msg);
                    break;
            }

            // send message back to client
            try {
                msg.replyTo.send(responseMsg);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }

        }
    }

    public static boolean isServiceRunning = false;

    public MyBoundService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        backgroundThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"backgroundThread running");
                startMusic();
            }
        });

        backgroundThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG,"Service started by startService()");

        Bundle extras = intent.getExtras();
        int binderOrMessenger = 1;  // default connection is IBinder
        if (extras != null) {
            binderOrMessenger = extras.getInt("BINDER_OR_MESSENGER");
            Log.i(TAG, "BINDER_OR_MESSENGER = " + binderOrMessenger);
        }

        if (binderOrMessenger == BinderIPC) {
            // send broadcast to receiver
            Intent broadcastIntent = new Intent(ActionName);
            extras = new Bundle();
            extras.putInt("RESULT", ServiceStarted);
            broadcastIntent.putExtras(extras);

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
            localBroadcastManager.sendBroadcast(broadcastIntent);
        } else {
            // MessengerIPC -> send message back to client

        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind called");
        Bundle extras = intent.getExtras();
        int binderOrMessenger = 1;  // default connection is IBinder
        if (extras != null) {
            binderOrMessenger = extras.getInt("BINDER_OR_MESSENGER");
            Log.i(TAG, "BINDER_OR_MESSENGER = " + binderOrMessenger);
        }
        if (binderOrMessenger == MessengerIPC) {
            return serviceMessenger.getBinder();
        } else {
            // using IBinder to connect
            return serviceBinder;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "onUnbind() called");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy() called");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Thread dummy = backgroundThread;
        backgroundThread = null;
        dummy.interrupt();

        serviceBinder = null;
        serviceMessenger = null;

        isServiceRunning = false;
    }

    public void playMusic(int binderOrMessenger) {
        if (mediaPlayer != null) {

            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();

                if (binderOrMessenger == BinderIPC) {
                    // send broadcast to receiver
                    Intent broadcastIntent = new Intent(ActionName);
                    Bundle extras = new Bundle();
                    extras.putInt("RESULT", MusicPlaying);
                    broadcastIntent.putExtras(extras);

                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                } else {
                    // send message back to client
                }
            }
        }

    }

    public void pauseMusic(int binderOrMessenger) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();

                if (binderOrMessenger == BinderIPC) {
                    // send broadcast to receiver
                    Intent broadcastIntent = new Intent(ActionName);
                    Bundle extras = new Bundle();
                    extras.putInt("RESULT", MusicPaused);
                    broadcastIntent.putExtras(extras);

                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
                    localBroadcastManager.sendBroadcast(broadcastIntent);
                } else {
                    // send message back to client
                }

            }
        }

    }

    public boolean isMusicPlaying() {

        boolean isMediaPlaying = false;
        if (mediaPlayer != null) {
            isMediaPlaying = mediaPlayer.isPlaying();
        }
        return isMediaPlaying;
    }

    public void terminateService(int binderOrMessenger) {
        stopSelf();
        if (binderOrMessenger == BinderIPC) {
            // send broadcast to receiver
            Intent broadcastIntent = new Intent(ActionName);
            Bundle extras = new Bundle();
            extras.putInt("RESULT", ServiceStopped);
            broadcastIntent.putExtras(extras);

            LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
            localBroadcastManager.sendBroadcast(broadcastIntent);
        } else {
            // send message back to client
        }
    }

    private void startMusic() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.music_a);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.start();
            }
        }
    }
}
