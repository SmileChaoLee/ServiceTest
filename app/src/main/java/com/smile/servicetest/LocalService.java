package com.smile.servicetest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class LocalService extends Service {
    public LocalService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
