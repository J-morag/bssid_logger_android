package com.example.bssid_logger;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.CursorIndexOutOfBoundsException;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Logger {

    static final String logname = "log.csv";
    private static final String logHeader = "time, BSSID, SSID\n";
    private TimerTask worker;
    private static final int maxLinesWritten = 10000;
    private static int linesWritten = 0;

//
//    public void onCreate()
//    {
//        super.onCreate();
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId)
//    {
//        return START_STICKY;
//    }
//
//    @Override
//    public void onStart(Intent intent, int startId)
//    {
//
//    }
//
//    @Override
//    public IBinder onBind(Intent intent)
//    {
//        return null;
//    }
//
//
//    @Override
//    public void onDestroy() {
//        Toast.makeText(this, "logger service stopped", Toast.LENGTH_LONG).show();
//    }

    static void logOnce(Context context){
        initLogFile(context);
        if (linesWritten < maxLinesWritten) {
            makeLogLine(context);
            linesWritten++;
        }
    }

    private static void initLogFile(Context context) {
        File file = new File(context.getFilesDir()+"/"+logname);
        if(!file.exists()){
            try {
                FileOutputStream outputStream = context.openFileOutput(logname, Context.MODE_APPEND);
                outputStream.write(logHeader.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    private static void makeLogLine(Context context){
        String tuple = getNewLogTuple(context);
        System.out.println("logging line " + tuple);
        if(tuple.equals("")) return;
        FileOutputStream outputStream;
//        FileOutputStream outputStream2 =

        try {
            outputStream = context.openFileOutput(logname, Context.MODE_APPEND);
            outputStream.write(tuple.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getNewLogTuple(Context context) {
        Date currentTime = Calendar.getInstance().getTime();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateTime = df.format(currentTime);
        String bssid = CurrentBSSIDWriter.getCurrentBssid(context);
        String ssid = CurrentBSSIDWriter.getCurrentSsid(context);
        return bssid.equals("") ? "" : (dateTime + "," + bssid +  "," + ssid + "\n");
    }






}
