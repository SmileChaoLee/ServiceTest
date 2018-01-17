package com.smile.servicetest;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.media.audiofx.EnvironmentalReverb;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DownloadService extends IntentService {

    private int result = Activity.RESULT_CANCELED;

    public DownloadService() {
        super("DownloadService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return;
        }

        String urlPath  = extras.getString("URL_PATH");
        String fileName = extras.getString("FILENAME");
        String notification = extras.getString("NOTIFICATION");

        // File output = new File(Environment.getExternalStorageDirectory(),fileName); // does not work on API 23
        System.out.println("Environment.getExternalStorageDirectory()-->"+Environment.getExternalStorageDirectory());
        System.out.println("Environment.getDataDirectory()-->"+Environment.getDataDirectory());
        System.out.println("getFilesDir()-->"+getFilesDir());
        System.out.println("getFilesDir().getPath()-->"+getFilesDir().getPath());

        File output = new File(getFilesDir(),fileName);
        // or
        // File output = new File(getApplicationContext().getFilesDir().getPath(),fileName);

        System.out.println("file name: "+output.getPath());
        if (output.exists()) {
            // if file already exists, then delete the file
            System.out.println("index.php already exist.");
            output.delete();
        } else {
            System.out.println("index.php does not exist.");
        }

        InputStream iStream = null;
        FileOutputStream foStream = null;

        try {
            URL url = new URL(urlPath);
            iStream = url.openConnection().getInputStream();
            InputStreamReader iReader= new InputStreamReader(iStream);
            foStream = new FileOutputStream(output);

            int readBuff = -1;
            while((readBuff=iReader.read()) != -1) {
                foStream.write(readBuff);
            }
            result = Activity.RESULT_OK;  // successfully downloaded
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (foStream != null) {
                try {
                    foStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // publish the result using sendBroadcast
        Intent notificationIntent = new Intent(notification);

        /*
            notificationIntent.putExtra(FILEPATH,output.getAbsolutePath());
            notificationIntent.putExtra(RESULT,result);
            sendBroadcast(notificationIntent);
        */

        Bundle ex = new Bundle();
        ex.putString("FILEPATH",output.getAbsolutePath());
        ex.putInt("RESULT",result);
        notificationIntent.putExtras(ex);
        // sendBroadcast(notificationIntent); // this will work for global broadcast receiver

        // this will work for local broadcast receiver
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.sendBroadcast(notificationIntent);
    }
}
