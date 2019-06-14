package com.celvansystems.projetoamigoanimal.helper;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.provider.Contacts;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.celvansystems.projetoamigoanimal.R;
import com.celvansystems.projetoamigoanimal.model.Animal;

import static android.app.Notification.EXTRA_NOTIFICATION_ID;

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
