package com.smile.servicetest;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    // private static final int REQUEST_CODE_ASK_PERMISSIONS = 213;
    private BroadcastReceiver receiver = null;
    public TextView statusText = null;
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
        filter.addAction(MyStartedService2.ActionName);
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

        Intent intent = new Intent(this,MyStartedService2.class);
        startService(intent);

    }

    public void startBoundServiceByIBinder( View view) {

        statusText.setText("");

        Intent intent = new Intent(MainActivity.this, IBinderActivity.class);
        // for testing activity crash
        // intent = null;
        startActivity(intent);
    }


    public void startBoundServiceByMessenger( View view) {

        statusText.setText("");

        Intent intent = new Intent(MainActivity.this, MessengerActivity.class);
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
                    int result = extras.getInt(Constants.Result);
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
                case MyStartedService2.ActionName:
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

    public static class MyStartedService2 extends Service {

        public MyStartedService2() {

        }

        public static final String ActionName = "MyStartedService2";

        @Override
        public void onCreate() {
            Log.d(TAG, "MyStartedService2.onCreate");
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            // return super.onStartCommand(intent, flags, startId);
            Log.d(TAG, "MyStartedService2.onStartCommand");
            Thread thread = new Thread() {
                @Override
                public void run() {

                    int sum = 0;    // the sum of 1 ~ 100
                    for (int i=1; i<=100; i++) {
                        sum += i;
                    }
                    stopSelf();

                    // sending broadcast
                    // String notification = getApplicationContext().getPackageName();
                    // String ActionName = getApplicationContext().getClass().getName();

                    Intent notificationIntent = new Intent(ActionName);
                    Bundle extras = new Bundle();
                    extras.putInt("SUM", sum);
                    notificationIntent.putExtras(extras);

                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
                    localBroadcastManager.sendBroadcast(notificationIntent);
                }
            };

            thread.start();

            return START_STICKY;
        }

        @Override
        public void onDestroy() {
            Log.d(TAG, "MyStartedService2.onDestroy");
            super.onDestroy();
        }

        @Override
        public IBinder onBind(Intent intent) {
            // TODO: Return the communication channel to the service.
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

}
