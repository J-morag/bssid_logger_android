package com.example.bssid_logger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.SystemClock;
import android.widget.Toast;

public class Alarm extends BroadcastReceiver
{
    private static int fireCounter = 0;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        System.out.println("alarms fired: " + ++fireCounter);
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "bssid_logger::log");
        wl.acquire();

        Logger.logOnce(context);

        wl.release();
    }

    public void setAlarm(Context context,long startDelayMS,  long intervalMS)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(/*"bssid_logger.START_ALARM"*/context, Alarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        //am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 5*1000, interval, pi);
        am.setRepeating(AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + startDelayMS,intervalMS, pi);
    }

    public void cancelAlarm(Context context)
    {
        Intent intent = new Intent(context, Alarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}