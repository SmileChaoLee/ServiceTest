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
    private String packageName = new String("");
    private Button buttonIntentService = null;
    private Button buttonStartedService = null;
    private Button buttonBoundServiceByIBinder = null;
    private Button buttonBoundServiceByMessenger = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = (TextView) findViewById(R.id.textStatus);
        buttonIntentService = (Button)findViewById(R.id.buttonIntentService);
        buttonStartedService = (Button)findViewById(R.id.buttonStartedService);
        buttonBoundServiceByIBinder = (Button)findViewById(R.id.buttonBoundServiceByIBinder);
        buttonBoundServiceByMessenger = (Button)findViewById(R.id.buttonBoundServiceByMessenger);

        packageName = getApplicationContext().getPackageName();
        // or
        // packageName = getPackageName();

        receiver = new mainActivityReceiver();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // IntentFilter filter = new IntentFilter();
        // filter.addAction(DownloadIntentService.ActionName);
        // filter.addAction(MyStartedService.ActionName);
        // registerReceiver(receiver, filter);  // use global broadcast receiver

        IntentFilter filter = new IntentFilter();
        filter.addAction(DownloadIntentService.ActionName);
        filter.addAction(MyStartedService.ActionName);
        // registerReceiver(receiver, filter);  // use global broadcast receiver

        // use Local Broadcast receiver
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

        buttonIntentService.setEnabled(false);
        buttonStartedService.setEnabled(false);
        buttonBoundServiceByIBinder.setEnabled(false);
        buttonBoundServiceByMessenger.setEnabled(false);

        statusText.setText("IntentService started !!");

        Intent intent = new Intent(this,DownloadIntentService.class);
        Bundle extras = new Bundle();
        // extras.putString("URL_PATH","http://fescc.ca/Chinese/index.htm");
        extras.putString("URL_PATH","https://trpgline.com/admin");
        extras.putString("FILENAME","admin");
        intent.putExtras(extras);
        startService(intent);

    }

    public void startStartedService( View view) {

        buttonIntentService.setEnabled(false);
        buttonStartedService.setEnabled(false);
        buttonBoundServiceByIBinder.setEnabled(false);
        buttonBoundServiceByMessenger.setEnabled(false);

        statusText.setText("StartedService started !!");

        Intent intent = new Intent(this,MyStartedService.class);
        startService(intent);

    }

    public void startBoundServiceByIBinder( View view) {

        statusText.setText("");

        Intent intent = new Intent(MainActivity.this, BoundServiceByIBinderActivity.class);
        startActivity(intent);
    }


    public void startBoundServiceByMessenger( View view) {

        statusText.setText("");

        Intent intent = new Intent(MainActivity.this, BoundServiceByMessengerActivity.class);
        startActivity(intent);
    }

    private class mainActivityReceiver extends BroadcastReceiver {
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
                        Toast.makeText(context,"Download completed. File: "+filePath,Toast.LENGTH_LONG).show();
                        // or
                        // Toast.makeText(getApplicationContext(),"Download completed. File: "+filePath,Toast.LENGTH_LONG).show();
                        statusText.setText("Download completed !!");
                    } else {
                        // download failed
                        Toast.makeText(context,"Download failed.",Toast.LENGTH_LONG).show();
                        // or
                        // Toast.makeText(getApplicationContext(),"Download failed.",Toast.LENGTH_LONG).show();
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
            buttonBoundServiceByIBinder.setEnabled(true);
            buttonBoundServiceByMessenger.setEnabled(true);
        }
    }
}
