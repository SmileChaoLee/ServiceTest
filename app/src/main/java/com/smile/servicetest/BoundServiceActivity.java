package com.smile.servicetest;

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
        playButton = (Button) findViewById(R.id.playMusic);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (myBoundService != null) && (isServiceBound) ) {
                    myBoundService.startPlay();
                }
            }
        });
        pauseButton = (Button) findViewById(R.id.pauseMusic);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (myBoundService != null) && (isServiceBound) ) {
                    myBoundService.pausePlay();
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
        Intent serviceIntent = new Intent(BoundServiceActivity.this, MyBoundService.class);
        startService(serviceIntent);

        myServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.i(TAG, "Bound service connected");
                MyBoundService.MyBinder myBinder = (MyBoundService.MyBinder)service;
                myBoundService = myBinder.getService();
                isServiceBound = true;
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

        // bind to the service
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
            Intent serviceStopIntent = new Intent(BoundServiceActivity.this, MyBoundService.class);
            stopService(serviceStopIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    // bind to the service
    private void doBindToService() {

        Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show();
        if (!isServiceBound) {
            Intent bindServiceIntent = new Intent(BoundServiceActivity.this,MyBoundService.class);
            isServiceBound = bindService(bindServiceIntent,myServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    // unbind from the service
    private void doUnbindService() {

        Toast.makeText(this,"Unbinding ...", Toast.LENGTH_SHORT).show();
        if (isServiceBound) {
            unbindService(myServiceConnection);
            isServiceBound = false;
        }
    }

    private class boundServiceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

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
