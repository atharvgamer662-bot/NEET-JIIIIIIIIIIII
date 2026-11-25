package com.neetjiiii.birthday;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.RemoteViews;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "birthday_alarm";

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager manager =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager == null) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Birthday Alarm",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Birthday wishes and reminders");
            manager.createNotificationChannel(channel);
        }

        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK |
            Intent.FLAG_ACTIVITY_CLEAR_TOP
        );

        PendingIntent contentIntent = PendingIntent.getActivity(
            context,
            1002,
            openApp,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? new Notification.Builder(context, CHANNEL_ID)
                : new Notification.Builder(context);

        builder.setSmallIcon(R.drawable.birthday_notification_icon)
            .setContentTitle("Happy Birthday! 🎂")
            .setContentText("Today is your special day! ❤️")
            .setContentIntent(contentIntent)
            .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            RemoteViews customView = new RemoteViews(
                context.getPackageName(),
                R.layout.notification_birthday
            );

            builder.setStyle(new Notification.DecoratedCustomViewStyle())
                .setCustomContentView(customView)
                .setCustomBigContentView(customView);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(Notification.PRIORITY_HIGH);
            builder.setDefaults(Notification.DEFAULT_ALL);
        }

        if (Build.VERSION.SDK_INT < 33 ||
            context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED) {

            manager.notify(1001, builder.build());
        }
    }
}
