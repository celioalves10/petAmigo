package com.celvansystems.projetoamigoanimal.helper;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class NotificationReceiver extends BroadcastReceiver {

    private NotificationManager mNotificationManager;
    private int SIMPLE_NOTFICATION_ID;

    @Override
    public void onReceive(Context context, Intent intent) {
        // assumes WordService is a registered service


            Log.d("INFO12", "receive OK");
            Toast.makeText(context, "receiver OKKK", Toast.LENGTH_LONG).show();


    }
}
