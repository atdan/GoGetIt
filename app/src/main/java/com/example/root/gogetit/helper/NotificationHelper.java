package com.example.root.gogetit.helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import com.example.root.gogetit.R;

public class NotificationHelper extends ContextWrapper {

    private static final String CHANNEL_ID = "com.example.root.gogetit.ATUMA";
    private static final String CHANNEL_NAME = "Eat It";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createChannel();
        }
    }

    private void createChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);

            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            
            getManager().createNotificationChannel(notificationChannel);
        }
    }

    public NotificationManager getManager() {
        if (manager == null){
            manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getEatItChannelNotification(String title, String body, PendingIntent contentIntent,
                                                            Uri soundUri){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setContentTitle(title)
                .setContentText(body)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setSound(soundUri)
                .setAutoCancel(false);

    }


}
