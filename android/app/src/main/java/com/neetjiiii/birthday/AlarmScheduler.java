package com.neetjiiii.birthday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.Calendar;

public class AlarmScheduler {

    public static boolean scheduleNextBirthday(Context context) {
        AlarmManager manager =
            (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !manager.canScheduleExactAlarms()) {
            return false;
        }

        Calendar now = Calendar.getInstance();
        Calendar birthday = Calendar.getInstance();

        birthday.set(Calendar.MONTH, Calendar.SEPTEMBER);
        birthday.set(Calendar.DAY_OF_MONTH, 19);
        birthday.set(Calendar.HOUR_OF_DAY, 0);
        birthday.set(Calendar.MINUTE, 0);
        birthday.set(Calendar.SECOND, 0);
        birthday.set(Calendar.MILLISECOND, 0);

        if (!birthday.after(now)) {
            birthday.add(Calendar.YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            1001,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        manager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            birthday.getTimeInMillis(),
            pendingIntent
        );

        return true;
    }
}
