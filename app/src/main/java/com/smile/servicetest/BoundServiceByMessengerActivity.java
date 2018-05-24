package com.smile.servicetest;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BoundServiceByMessengerActivity extends AppCompatActivity {

    private String TAG = "com.smile.servicetest.BoundServiceByMessengerActivity";

    private TextView messageText;
    private Button startBindServiceButton;
    private Button unbindStopServiceButton;
    private Button playButton;
    private Button pauseButton;
    private Button exitBoundService;
    private boolean isServiceBound = false;
    private Messenger sendMessenger;
    private ServiceConnection myServiceConnection;

    private Context context;

    private Messenger clientMessenger = new Messenger(new ClientHandler());
    private class ClientHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MyBoundService.ServiceStopped:
                    Log.i(TAG,"ServiceStopped received");
                    messageText.setText("BoundService stopped.");
                    break;
                case MyBoundService.ServiceStarted:
                    Log.i(TAG,"ServiceStarted received");
                    messageText.setText("BoundService started.");
                    break;
                case MyBoundService.MusicPlaying:
                    Log.i(TAG,"MusicPlaying received");
                    messageText.setText("Music playing.");
                    break;
                case MyBoundService.MusicPaused:
                    Log.i(TAG,"MusicPaused received");
                    messageText.setText("Music paused.");
                    break;
                default:
                    Log.i(TAG,"Unknown message");
                    messageText.setText("Unknown message.");
                    break;
            }
            super.handleMessage(msg);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bound_service_by_messenger);

        context = getApplicationContext();

        messageText = (TextView) findViewById(R.id.messageText);
        startBindServiceButton = (Button) findViewById(R.id.startBindService);
        startBindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start service
                startMusicService();
                doBindToService();
            }
        });
        unbindStopServiceButton = (Button) findViewById(R.id.unbindStopService);
        unbindStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindAndStopMusicService();
            }
        });
        playButton = (Button) findViewById(R.id.playMusic);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sendMessenger != null) && (isServiceBound)) {
                    // play music
                    Message msg = Message.obtain(null, MyBoundService.MusicPlaying, 0, 0);
                    try {
                        msg.replyTo = clientMessenger;
                        sendMessenger.send(msg);
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(true);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        pauseButton = (Button) findViewById(R.id.pauseMusic);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((sendMessenger != null) && (isServiceBound)) {
                    // pause music
                    Message msg = Message.obtain(null, MyBoundService.MusicPaused, 0, 0);
                    try {
                        msg.replyTo = clientMessenger;
                        sendMessenger.send(msg);
                        playButton.setEnabled(true);
                        pauseButton.setEnabled(false);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        exitBoundService = (Button) findViewById(R.id.exitBoundService);
        exitBoundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // finish activity
            }
        });

        // start service
        startMusicService();

        myServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "Bound service connected");
                sendMessenger = new Messenger(service);
                isServiceBound = true;

                // send message to MyBoundService
                Message msg = Message.obtain(null, MyBoundService.ServiceStarted, 0, 0);
                try {
                    msg.replyTo = clientMessenger;
                    sendMessenger.send(msg);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }

                /*
                // set statuses for buttons
                if (myBoundService != null) {
                    pauseButton.setEnabled(myBoundService.isMusicPlaying());
                    playButton.setEnabled(!pauseButton.isEnabled());
                }
                */
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i(TAG, "Bound service disconnected");
                sendMessenger = null;
                isServiceBound = false;
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "BoundServiceActivityByMessenger - onResume - Binding to service");
        doBindToService();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unbind from the service
        Log.i(TAG, "BoundServiceActivityByMessenger - onPause - Unbinding from service");
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Destroying activity .......");
        if (isFinishing()) {
            // stop service as activity being destroyed and we won't use any more
            Log.i(TAG, "Activity is finishing.");
            stopMusicService();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void startMusicService() {
        // start service
        if (!MyBoundService.isServiceRunning(context)) {
            // MyBoundService is not running
            try {
                Intent serviceIntent = new Intent(BoundServiceByMessengerActivity.this, MyBoundService.class);
                // parameters for this Intent
                Bundle extras = new Bundle();
                extras.putInt("BINDER_OR_MESSENGER", MyBoundService.MessengerIPC);
                serviceIntent.putExtras(extras);
                startService(serviceIntent);

                startBindServiceButton.setEnabled(false);
                unbindStopServiceButton.setEnabled(true);
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
                Log.i("startMusicService", "Service started");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void unbindAndStopMusicService() {
        // start service
        try {
            doUnbindService();
            stopMusicService();

            startBindServiceButton.setEnabled(true);
            unbindStopServiceButton.setEnabled(false);
            playButton.setEnabled(false);
            pauseButton.setEnabled(false);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // bind to the service
    private void doBindToService() {

        if (MyBoundService.isServiceRunning(context)) {
            // service is running
            Log.i(TAG, "BoundServiceActivityByMessenger - Binding to service");
            Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show();
            if (!isServiceBound) {
                Intent bindServiceIntent = new Intent(BoundServiceByMessengerActivity.this, MyBoundService.class);

                // parameters for this Intent
                Bundle extras = new Bundle();
                extras.putInt("BINDER_OR_MESSENGER", MyBoundService.MessengerIPC);
                bindServiceIntent.putExtras(extras);

                isServiceBound = bindService(bindServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    // unbind from the service
    private void doUnbindService() {
        if (MyBoundService.isServiceRunning(context)) {
            // service is running
            Log.i(TAG, "BoundServiceActivityByMessenger - Unbinding to service");
            if (isServiceBound) {
                unbindService(myServiceConnection);
                Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "BoundServiceActivityByMessenger --> service unbound.");
                isServiceBound = false;
            }
        }
    }

    private void stopMusicService() {
        if (MyBoundService.isServiceRunning(context)) {
            Log.i(TAG, "BoundServiceActivityByMessenger - Stopping service");
            // Intent serviceStopIntent = new Intent(BoundServiceByMessengerActivity.this, MyBoundService.class);
            // stopService(serviceStopIntent);
            Message msg = Message.obtain(null, MyBoundService.ServiceStopped, 0, 0);
            try {
                msg.replyTo = clientMessenger;
                sendMessenger.send(msg);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            }
        }
    }
}