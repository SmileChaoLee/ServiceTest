package com.smile.servicetest;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothClass;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class BoundServiceActivity extends AppCompatActivity {

    private String TAG = "com.smile.servicetest.BoundServiceActivity";

    private TextView messageText;
    private Button startBindServiceButton;
    private Button unbindStopServiceButton;
    private Button playButton;
    private Button pauseButton;
    private Button exitBoundService;
    private BroadcastReceiver receiver;
    private boolean isServiceBound = false;
    private MyBoundService myBoundService;
    private ServiceConnection myServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bound_service);

        messageText = (TextView) findViewById(R.id.messageText);
        startBindServiceButton = (Button)findViewById(R.id.startBindService);
        startBindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // start service
                startMusicService();
                doBindToService();
            }
        });
        unbindStopServiceButton = (Button)findViewById(R.id.unbindStopService);
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
                if ((myBoundService != null) && (isServiceBound)) {
                    myBoundService.playMusic();
                    playButton.setEnabled(false);
                    pauseButton.setEnabled(true);
                }
            }
        });
        pauseButton = (Button) findViewById(R.id.pauseMusic);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (myBoundService != null) && (isServiceBound) ) {
                    myBoundService.pauseMusic();
                    playButton.setEnabled(true);
                    pauseButton.setEnabled(false);
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

        receiver = new boundServiceReceiver();

        // start service
        startMusicService();

        myServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "Bound service connected");
                MyBoundService.MyBinder myBinder = (MyBoundService.MyBinder)service;
                myBoundService = myBinder.getService();
                isServiceBound = true;
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
                myBoundService = null;
                isServiceBound = false;
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyBoundService.ActionName);
        // global registration
        //registerReceiver(receiver, filter);

        // local registration
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(receiver,filter);

        /*
        if (isServiceRunning(MyBoundService.class)) {
            Log.i(TAG,"BoundServiceActivity - onResume - Binding to service");
            doBindToService();
        }
        */

        Log.i(TAG,"BoundServiceActivity - onResume - Binding to service");
        doBindToService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregisterReceiver(receiver);    // use global broadcast receiver

        // local registration
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(receiver);

        // unbind from the service
        Log.i(TAG,"BoundServiceActivity - onPause - Unbinding from service");
        doUnbindService();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"Destroying activity .......");
        if (isFinishing()) {
            // stop service as activity being destroyed and we won't use any more
            Log.i(TAG,"Activity is finishing.");
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
        // if (myBoundService == null) {
        if (!MyBoundService.isServiceRunning) {
            // MyBoundService is not running
            try {
                Intent serviceIntent = new Intent(BoundServiceActivity.this, MyBoundService.class);
                startService(serviceIntent);
                startBindServiceButton.setEnabled(false);
                unbindStopServiceButton.setEnabled(true);
                playButton.setEnabled(false);
                pauseButton.setEnabled(true);
                MyBoundService.isServiceRunning = true;
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

        if (MyBoundService.isServiceRunning) {
            // service is running
            Log.i(TAG, "BoundServiceActivity - Binding to service");
            Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show();
            if (!isServiceBound) {
                Intent bindServiceIntent = new Intent(BoundServiceActivity.this, MyBoundService.class);
                isServiceBound = bindService(bindServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    // unbind from the service
    private void doUnbindService() {
        if (MyBoundService.isServiceRunning) {
            // service is running
            Log.i(TAG, "BoundServiceActivity - Unbinding to service");
            Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show();
            if (isServiceBound) {
                unbindService(myServiceConnection);
                isServiceBound = false;
            }
        }
    }

    private void stopMusicService() {
        if (MyBoundService.isServiceRunning) {
            Log.i(TAG, "BoundServiceActivity - Stopping service");
            Intent serviceStopIntent = new Intent(BoundServiceActivity.this, MyBoundService.class);
            stopService(serviceStopIntent);
            // MyBoundService.isServiceRunning = false; // set inside onDestroy() inside MyBoundService.calss
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private class boundServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null) {
                return;
            }

            String action = intent.getAction();

            if (action.equals(MyBoundService.ActionName)) {
                Bundle extras = intent.getExtras();
                int result = extras.getInt("RESULT");

                switch(result) {
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
                        break;
                }
            }
        }
    }
}
