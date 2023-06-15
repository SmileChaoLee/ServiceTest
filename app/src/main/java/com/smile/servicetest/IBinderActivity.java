package com.smile.servicetest;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class IBinderActivity extends AppCompatActivity {

    private final String TAG = "IBinderActivity";

    private TextView messageText;
    private Button bindServiceButton;
    private Button unbindServiceButton;
    private Button playButton;
    private Button pauseButton;
    private Button exitBoundService;
    private BroadcastReceiver receiver;
    private boolean isServiceBound = false;
    private MyBoundService myBoundService;
    private IBinder.DeathRecipient deathRecipient;
    private ServiceConnection myServiceConnection;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bound_service_by_ibinder);

        context = getApplicationContext();
        messageText = findViewById(R.id.messageText);
        bindServiceButton = findViewById(R.id.bindServiceButton);
        bindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bindMusicService();
            }
        });
        unbindServiceButton = findViewById(R.id.unbindServiceButton);
        unbindServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unbindMusicService();
            }
        });
        playButton = findViewById(R.id.playMusic);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((myBoundService != null) && (isServiceBound)) {
                    myBoundService.playMusic();
                }
            }
        });
        pauseButton = findViewById(R.id.pauseMusic);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ( (myBoundService != null) && (isServiceBound) ) {
                    myBoundService.pauseMusic();
                }
            }
        });
        exitBoundService = findViewById(R.id.exitBoundService);
        exitBoundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();   // finish activity
            }
        });

        receiver = new boundServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ServiceName);
        // global registration
        //registerReceiver(receiver, filter);
        // local registration
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(receiver,filter);

        deathRecipient = new IBinder.DeathRecipient() {
            @Override
            public void binderDied() {
                Log.d(TAG, "binderDied");
            }
        };

        myServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected");
                MyBoundService.ServiceBinder myBinder = (MyBoundService.ServiceBinder)service;
                myBoundService = myBinder.getService();
                isServiceBound = true;
                bindServiceButton.setEnabled(false);
                unbindServiceButton.setEnabled(true);
                playButton.setEnabled(false);
                pauseButton.setEnabled(false);
                if (myBoundService.isMusicLoaded()) {
                    playButton.setEnabled(!myBoundService.isMusicPlaying());
                    pauseButton.setEnabled(myBoundService.isMusicPlaying());
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                // This method is not always called
                Log.d(TAG, "onServiceDisconnected");
                myBoundService = null;
                isServiceBound = false;
                bindServiceButton.setEnabled(true);
                unbindServiceButton.setEnabled(false);
            }
        };

        bindMusicService();
    }

    @Override
    protected void onResume() {
        Log.d(TAG,"onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG,"onPause");
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG,"onDestroy");
        unbindMusicService();
        // unregisterReceiver(receiver);    // use global broadcast receiver
        // local un-registration
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void unbindMusicService() {
        Log.d(TAG, "unbindMusicService");
        Toast.makeText(this, "Unbinding ...", Toast.LENGTH_SHORT).show();
        if (isServiceBound) {
            unbindService(myServiceConnection);
            myBoundService = null;
            isServiceBound = false;
            bindServiceButton.setEnabled(true);
            unbindServiceButton.setEnabled(false);
            playButton.setEnabled(false);
            pauseButton.setEnabled(false);
        }
    }

    // bind to the service
    private void bindMusicService() {
        Log.d(TAG, "bindMusicService");
        Toast.makeText(this, "Binding ...", Toast.LENGTH_SHORT).show();
        if (!isServiceBound) {
            Intent bindServiceIntent = new Intent(this, MyBoundService.class);
            // parameters for this Intent
            Bundle extras = new Bundle();
            extras.putInt(Constants.BINDER_OR_MESSENGER_KEY, Constants.BinderIPC);
            bindServiceIntent.putExtras(extras);
            isServiceBound = bindService(bindServiceIntent, myServiceConnection, Context.BIND_AUTO_CREATE);
        }
    }

    private void stopMusicService() {
        Log.d(TAG, "stopMusicService");
        if (myBoundService != null) {
            myBoundService.terminateService();
        }
    }

    private class boundServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }
            String action = intent.getAction();
            if (action.equals(Constants.ServiceName)) {
                Bundle extras = intent.getExtras();
                int result = extras.getInt("RESULT");
                Log.d(TAG,"onReceive.result = " + result);
                switch(result) {
                    case Constants.ServiceStarted:
                        Log.d(TAG, "ServiceStarted received");
                        messageText.setText("BoundService started.");
                        bindServiceButton.setEnabled(true);
                        unbindServiceButton.setEnabled(false);
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(false);
                        break;
                    case Constants.ServiceBound:
                        Log.d(TAG, "ServiceBound received");
                        messageText.setText("BoundService Bound.");
                        bindServiceButton.setEnabled(false);
                        unbindServiceButton.setEnabled(true);
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(false);
                        if (myBoundService!=null && myBoundService.isMusicLoaded()) {
                            playButton.setEnabled(!myBoundService.isMusicPlaying());
                            pauseButton.setEnabled(myBoundService.isMusicPlaying());
                        }
                        break;
                    case Constants.ServiceUnbound:
                        Log.d(TAG,"ServiceUnbound received");
                        messageText.setText("BoundService unbound.");
                        bindServiceButton.setEnabled(true);
                        unbindServiceButton.setEnabled(false);
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(false);
                        break;
                    case Constants.ServiceStopped:
                        Log.d(TAG,"ServiceStopped received");
                        messageText.setText("BoundService stopped.");
                        bindServiceButton.setEnabled(false);
                        unbindServiceButton.setEnabled(false);
                        playButton.setEnabled(false);
                        pauseButton.setEnabled(false);
                        break;
                    case Constants.MusicPlaying:
                        Log.d(TAG,"MusicPlaying received");
                        messageText.setText("Music playing.");
                        if (isServiceBound) {
                            playButton.setEnabled(false);
                            pauseButton.setEnabled(true);
                        }
                        break;
                    case Constants.MusicPaused:
                        Log.d(TAG,"MusicPaused received");
                        messageText.setText("Music paused.");
                        if (isServiceBound) {
                            playButton.setEnabled(true);
                            pauseButton.setEnabled(false);
                        }
                        break;
                    case Constants.MusicStopped:
                        Log.d(TAG,"MusicStopped received");
                        messageText.setText("Music stopped.");
                        if (isServiceBound) {
                            playButton.setEnabled(true);
                            pauseButton.setEnabled(false);
                        }
                        break;
                    case Constants.MusicLoaded:
                        Log.d(TAG,"MusicLoaded received");
                        messageText.setText("Music Loaded.");
                        break;
                    default:
                        Log.d(TAG,"Unknown message.");
                        messageText.setText("Unknown message.");
                        break;
                }
            }
        }
    }
}
