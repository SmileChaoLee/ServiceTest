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
    private static String TAG = "MyBoundService";
    private MediaPlayer mediaPlayer = null;
    private boolean isMusicLoaded = false;
    private boolean isMusicPlaying = false;
    private int binderOrMessenger = Constants.BinderIPC;

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
            int result = Constants.ErrorCode;
            int arg1 = 0, arg2 = 0;
            switch (msg.what) {
                case Constants.StopService:
                    result = terminateService();
                    break;
                case  Constants.PlayMusic:
                    result = playMusic();
                    break;
                case Constants.PauseMusic:
                    result = pauseMusic();
                    break;
                case Constants.StopMusic:
                    result = stopMusic();
                    break;
                case Constants.AskStatus:
                    result = Constants.MusicStatus;
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
        binderOrMessenger = Constants.BinderIPC;  // default connection is IBinder
        if (extras != null) {
            binderOrMessenger = extras.getInt(Constants.BINDER_OR_MESSENGER_KEY);
            Log.d(TAG, Constants.BINDER_OR_MESSENGER_KEY + " = " + binderOrMessenger);
        }
        broadcastResult(Constants.ServiceStarted);
        Log.d(TAG, "onStartCommand.binderOrMessenger = " + binderOrMessenger);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Bundle extras = intent.getExtras();
        binderOrMessenger = Constants.BinderIPC;  // default connection is IBinder
        if (extras != null) {
            binderOrMessenger = extras.getInt(Constants.BINDER_OR_MESSENGER_KEY);
            Log.d(TAG, Constants.BINDER_OR_MESSENGER_KEY + " = " + binderOrMessenger);
        }
        Log.d(TAG, "onBind.binderOrMessenger = " + binderOrMessenger);
        broadcastResult(Constants.ServiceBound);
        if (binderOrMessenger == Constants.MessengerIPC) {
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
        broadcastResult(Constants.ServiceUnbound);
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
        int result = Constants.ErrorCode;
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                result = Constants.MusicPlaying;
                isMusicPlaying = true;
            }
        }
        Log.d(TAG, "playMusic.result = " + result);
        if (binderOrMessenger == Constants.BinderIPC) {
            // send broadcast to receiver
            broadcastResult(result);
        } else {
            // send message back to client
            // Implemented in ServiceHandler
        }
        return result;
    }

    public int pauseMusic() {
        int result = Constants.ErrorCode;
        if (mediaPlayer != null) {
            Log.d(TAG, "mediaPlayer not null");
            if (mediaPlayer.isPlaying()) {
                Log.d(TAG, "mediaPlayer.isPlaying() is true");
                mediaPlayer.pause();
                result = Constants.MusicPaused;
                isMusicPlaying = false;
            }
        }
        Log.d(TAG, "pauseMusic.result = " + result);
        if (binderOrMessenger == Constants.BinderIPC) {
            // send broadcast to receiver
            broadcastResult(result);
        } else {
            // send message back to client
            // Implemented in ServiceHandler
        }
        return result;
    }
    public int stopMusic() {
        int result = Constants.ErrorCode;
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            result = Constants.MusicStopped;
            isMusicPlaying = false;
        }
        Log.d(TAG, "stopMusic.result = " + result);
        if (binderOrMessenger == Constants.BinderIPC) {
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
        int result = Constants.ServiceStopped;
        if (binderOrMessenger == Constants.BinderIPC) {
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
        int result = Constants.ErrorCode;
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.music_a);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                result = Constants.MusicLoaded;
                isMusicLoaded = true;
            }
        }
        Log.d(TAG, "loadMusic.result = " + result);
        broadcastResult(result);
        return result;
    }

    private void broadcastResult(int result) {
        Log.d(TAG, "broadcastResult");
        Intent broadcastIntent = new Intent(Constants.ServiceName);
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
