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
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyBoundService extends Service {

    public static final String ActionName = "com.smile.servicetest.MyBoundService";
    public static final int ErrorCode = -1;
    public static final int ServiceStopped = 0;
    public static final int ServiceStarted = 1;
    public static final int ServiceBound = 2;
    public static final int ServiceUnbound = 3;
    public static final int MusicPlaying = 4;
    public static final int MusicPaused = 5;
    public static final int MusicStopped = 6;
    public static final int MusicLoaded = 7;
    public static final int StopService = 101;
    public static final int PlayMusic = 102;
    public static final int PauseMusic = 103;
    public static final int StopMusic = 104;
    public static final int AskStatus = 201;
    public static final int MusicStatus = 202;
    public static final int BinderIPC = 11;
    public static final int MessengerIPC = 12;

    public static final String BINDER_OR_MESSENGER_KEY = "BINDER_OR_MESSENGER";
    public static final String MyBoundServiceChannelName = "com.smile.servicetest.MyBoundService.ANDROID";
    public static final String MyBoundServiceChannelID = "com.smile.servicetest.MyBoundService.CHANNEL_ID";
    public static final int MyBoundServiceNotificationID = 1;

    private static String TAG = "MyBoundService";
    private MediaPlayer mediaPlayer = null;
    private boolean isMusicLoaded = false;
    private boolean isMusicPlaying = false;
    private int binderOrMessenger = BinderIPC;

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
        public ServiceHandler() {
            super(Looper.getMainLooper());
        }
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "ServiceHandler.msg.what = " + msg.what);
            int result = ErrorCode;
            int arg1 = 0, arg2 = 0;
            switch (msg.what) {
                case StopService:
                    result = terminateService();
                    break;
                case PlayMusic:
                    result = playMusic();
                    break;
                case PauseMusic:
                    result = pauseMusic();
                    break;
                case StopMusic:
                    result = stopMusic();
                    break;
                case AskStatus:
                    result = MusicStatus;
                    Log.d(TAG, "ServiceHandler.isMusicLoaded = " + isMusicLoaded);
                    Log.d(TAG, "ServiceHandler.isMusicPlaying = " + isMusicPlaying);
                    arg1 = isMusicLoaded? 1 : 0;
                    arg2 = isMusicPlaying? 1 : 0;
                    Log.d(TAG, "ServiceHandler.arg1 = " + arg1);
                    Log.d(TAG, "ServiceHandler.arg2 = " + arg2);
                default:
                    super.handleMessage(msg);
                    break;
            }
            // setting response message
            Message responseMsg = Message.obtain(null, result, arg1, arg2);
            try {
                // send message back to client
                msg.replyTo.send(responseMsg);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }

    public MyBoundService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG,"onCreate()");
        super.onCreate();
        loadMusic();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand()");
        Bundle extras = intent.getExtras();
        binderOrMessenger = BinderIPC;  // default connection is IBinder
        if (extras != null) {
            binderOrMessenger = extras.getInt(BINDER_OR_MESSENGER_KEY);
            Log.d(TAG, BINDER_OR_MESSENGER_KEY + " = " + binderOrMessenger);
        }
        broadcastResult(ServiceStarted);
        Log.d(TAG, "onStartCommand.binderOrMessenger = " + binderOrMessenger);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        binderOrMessenger = BinderIPC;  // default connection is IBinder
        if (extras != null) {
            binderOrMessenger = extras.getInt(BINDER_OR_MESSENGER_KEY);
            Log.d(TAG, BINDER_OR_MESSENGER_KEY + " = " + binderOrMessenger);
        }
        Log.d(TAG, "onBind.binderOrMessenger = " + binderOrMessenger);
        broadcastResult(ServiceBound);
        if (binderOrMessenger == MessengerIPC) {
            return serviceMessenger.getBinder();
        } else {
            // using IBinder to connect
            return serviceBinder;
        }
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        pauseMusic();
        broadcastResult(ServiceUnbound);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        serviceBinder = null;
        serviceMessenger = null;
        isMusicLoaded = false;
        isMusicPlaying = false;
    }

    public boolean isMusicLoaded() {
        return isMusicLoaded;
    }
    public boolean isMusicPlaying() {
        return isMusicPlaying;
    }
    public int playMusic() {
        int result = ErrorCode;
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                result = MusicPlaying;
                isMusicPlaying = true;
            }
        }
        Log.d(TAG, "playMusic.result = " + result);
        if (binderOrMessenger == BinderIPC) {
            // send broadcast to receiver
            broadcastResult(result);
        } else {
            // send message back to client
            // Implemented in ServiceHandler
        }
        return result;
    }

    public int pauseMusic() {
        int result = ErrorCode;
        if (mediaPlayer != null) {
            Log.d(TAG, "mediaPlayer not null");
            if (mediaPlayer.isPlaying()) {
                Log.d(TAG, "mediaPlayer.isPlaying() is true");
                mediaPlayer.pause();
                result = MusicPaused;
                isMusicPlaying = false;
            }
        }
        Log.d(TAG, "pauseMusic.result = " + result);
        if (binderOrMessenger == BinderIPC) {
            // send broadcast to receiver
            broadcastResult(result);
        } else {
            // send message back to client
            // Implemented in ServiceHandler
        }
        return result;
    }
    public int stopMusic() {
        int result = ErrorCode;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            result = MusicStopped;
            isMusicPlaying = false;
        }
        Log.d(TAG, "stopMusic.result = " + result);
        if (binderOrMessenger == BinderIPC) {
            // send broadcast to receiver
            broadcastResult(result);
        } else {
            // send message back to client
            // Implemented in ServiceHandler
        }
        return result;
    }

    public int terminateService() {
        Log.d(TAG, "terminateService");
        isMusicLoaded = false;
        isMusicPlaying = false;
        stopSelf();
        int result = ServiceStopped;
        if (binderOrMessenger == BinderIPC) {
            // send broadcast to receiver
            broadcastResult(result);
        } else {
            // send message back to client
            // Implemented in ServiceHandler
        }
        return result;
    }

    private int loadMusic() {
        isMusicPlaying = false;
        int result = ErrorCode;
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.music_a);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                result = MusicLoaded;
                isMusicLoaded = true;
            }
        }
        Log.d(TAG, "loadMusic.result = " + result);
        broadcastResult(result);
        return result;
    }

    private void broadcastResult(int result) {
        Log.d(TAG, "broadcastResult");
        Intent broadcastIntent = new Intent(ActionName);
        Bundle extras = new Bundle();
        extras.putInt("RESULT", result);
        broadcastIntent.putExtras(extras);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getBaseContext());
        localBroadcastManager.sendBroadcast(broadcastIntent);
    }

    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;
        if (context != null) {
            Class<?> serviceClass = MyBoundService.class;
            ActivityManager manager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            // ActivityManager.getRunningServices() deprecated at API 26 and higher
            for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                    isRunning = true;
                }
            }
        }
        Log.d(TAG, "isServiceRunning.isRunning = " + isRunning);
        return isRunning;
    }
}
