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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    // private static final int REQUEST_CODE_ASK_PERMISSIONS = 213;
    private BroadcastReceiver receiver = null;
    private TextView statusText = null;
    private String notification = new String("");
    private String packageName = new String("");
    private Button buttonIntentService = null;
    private Button buttonStartedService = null;
    private Button buttonBoundService = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.textStatus);
        buttonIntentService = (Button)findViewById(R.id.buttonIntentService);
        buttonStartedService = (Button)findViewById(R.id.buttonStartedService);
        buttonBoundService = (Button)findViewById(R.id.buttonBoundService);

        packageName = getApplicationContext().getPackageName();
        // or
        // packageName = getPackageName();
        notification = packageName;
        // notification = getIntent().getAction();

        receiver = new broadcastReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // registerReceiver(receiver, new IntentFilter(notification));  // use global broadcast receiver

        // use Local Broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadIntentService.ActionName);
        filter.addAction(MyStartedService.ActionName);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver,filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // unregisterReceiver(receiver);    // use global broadcast receiver

        // use Local broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    public void startIntentService(View view) {
        Intent intent = new Intent(this,DownloadIntentService.class);
        Bundle extras = new Bundle();
        // extras.putString("URL_PATH","http://fescc.ca/Chinese/index.htm");
        extras.putString("URL_PATH","https://trpgline.com/admin");
        extras.putString("FILENAME","admin");
        extras.putString("NOTIFICATION",notification);
        intent.putExtras(extras);
        startService(intent);

        buttonIntentService.setEnabled(false);
        buttonStartedService.setEnabled(false);
        buttonBoundService.setEnabled(false);

        statusText.setText("IntentService started !!");
    }

    public void startStartedService( View view) {
        Intent intent = new Intent(this,MyStartedService.class);
        startService(intent);
        statusText.setText("StartedService started !!");
    }

    public void startBoundService( View view) {
        statusText.setText("BoundService has not been implemented yet !!");

    }

    private class broadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context,Intent intent) {

            String action = intent.getAction();
            System.out.println("Action name ---> " + action);

            Bundle extras = null;
            switch (action) {
                case DownloadIntentService.ActionName:
                    extras = intent.getExtras();
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
                    break;
                case MyStartedService.ActionName:
                    extras = intent.getExtras();
                    int sum = extras.getInt("SUM");
                    statusText.setText("Calculated (" + sum + ") !!");
                    break;
                default:
                    break;
            }

            buttonIntentService.setEnabled(true);
            buttonStartedService.setEnabled(true);
            buttonBoundService.setEnabled(true);
        }
    }
}
