package com.smile.servicetest;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // private static final int REQUEST_CODE_ASK_PERMISSIONS = 213;
    private BroadcastReceiver receiver = null;
    private TextView statusText = null;
    private String notification = new String("");
    private String packageName = new String("");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageName = getApplicationContext().getPackageName();
        // or
        // packageName = getPackageName();
        notification = packageName;
        // notification = getIntent().getAction();

        statusText = (TextView) findViewById(R.id.textStatus);

        receiver = new broadcastReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // registerReceiver(receiver, new IntentFilter(notification));  // use global broadcast receiver

        // use Local Broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,new IntentFilter(notification));
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregisterReceiver(receiver);    // use global broadcast receiver

        // use Local broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void startDownloadService(View view) {
        Intent intent = new Intent(this,DownloadService.class);
        Bundle extras = new Bundle();
        extras.putString("URL_PATH","http://trpgline.com/index.php");
        extras.putString("FILENAME","index.php");
        extras.putString("NOTIFICATION",notification);
        intent.putExtras(extras);
        startService(intent);

        statusText.setText("Service started !!");
    }

    public void startBindService( View view) {
        statusText.setText("bindService has not been implemented yet !!");

    }

    private class broadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                return;
            }
            String filePath = extras.getString("FILEPATH");
            int result = extras.getInt("RESULT");
            if (result == Activity.RESULT_OK) {
                // download finished successfully
                Toast.makeText(getApplicationContext(),"Download completed. File: "+filePath,Toast.LENGTH_LONG).show();
                statusText.setText("Download completed !!");
            } else {
                // download failed
                Toast.makeText(getApplicationContext(),"Download failed.",Toast.LENGTH_LONG).show();
                statusText.setText("Download failed !!");
            }
        }
    }
}
