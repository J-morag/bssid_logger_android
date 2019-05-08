package com.example.bssid_logger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private TextView bsssid_view;
    private boolean logging = false;
    private String logname = Logger.logname;
//    private static final String logFiletype = "text/csv";
    private static final String logFiletype = "application/vnd.ms-excel";
    Alarm alarm = new Alarm();
    private static final int loggingIntervalMs = 10*60*1000;



    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        ((TextView)findViewById(R.id.phone_ID)).setText(getDeviceID());

        bsssid_view = findViewById(R.id.BSSID);


//        Intent loggerIntent = new Intent(this, Logger.class);
//        startService(loggerIntent);



    }

    @Override
    protected void onStart() {
        super.onStart();
        if(!logging) {
            System.out.println("starting logger");
            alarm.setAlarm(this, 0, loggingIntervalMs);
            Toast.makeText(this, "started logging", Toast.LENGTH_LONG).show();
            logging = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("restarting logger");
        alarm.setAlarm(this,
                loggingIntervalMs/2 /*use average to avoid shifting timing*/
                , loggingIntervalMs);
        Toast.makeText(this, logging ? "logger is working" : "restarting logger",
                Toast.LENGTH_LONG).show();
        logging = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    public void clickSendLog(View v)
    {

        File file   = null;
        File root   = Environment.getExternalStorageDirectory();
        System.out.println(root.canWrite() ? "writing to external storage" : "missing permission to write to external storage");
        if (root.canWrite()){
            File dir    =   new File (root.getAbsolutePath() + "/bssid_logs");
            dir.mkdirs();
            file = new File(dir, logname);
            FileOutputStream out   =   null;
            try {
                out = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                out.write(logToString().getBytes());
                System.out.println("sending " + logToString().length() + " chars");

            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "BSSID Log - " + android.os.Build.SERIAL);
            sendIntent.putExtra(Intent.EXTRA_EMAIL, "");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "");

            Uri apkURI = FileProvider.getUriForFile(this, this.getApplicationContext()
                            .getPackageName() + ".provider", file);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            sendIntent.setType(logFiletype);

            sendIntent.putExtra(Intent.EXTRA_STREAM, apkURI);
            startActivity(sendIntent);
        }

    }


    public void clickStartStop(View v){
        synchronized (this){
            if(logging){
                alarm.cancelAlarm(this);
                Toast.makeText(this, "stopped logging", Toast.LENGTH_LONG).show();
                logging = false;
            }
            else {
                alarm.setAlarm(this, 0, loggingIntervalMs);
                Toast.makeText(this, "started logging", Toast.LENGTH_LONG).show();
                logging = true;
            }
        }
    }


    public void updateCurrentBssidDisplay(View view) {
        String bssid = CurrentBSSIDWriter.getCurrentBssid(getBaseContext());
        String ssid = CurrentBSSIDWriter.getCurrentSsid(getBaseContext());
        Logger.logOnce(this);
        bsssid_view.setText(bssid);
    }




    private String getDeviceID(){
        return android.os.Build.SERIAL;
    }



    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void updateNumRecords(View view) {

        TextView numRecords = findViewById(R.id.numRecords);

        try {
            BufferedReader logIn = new BufferedReader(new FileReader(getApplicationContext().getFilesDir()+"/"+logname));
//            bsssid_view.setText(logIn.readLine());
            int rows = 0;
            String line = logIn.readLine();
            while(line != null){
                line = logIn.readLine();
                rows++;
                numRecords.setText(""+rows);
            }
            logIn.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "log file not found", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "IO exception", Toast.LENGTH_LONG).show();
        }

//        try {
//            BufferedReader logIn = new BufferedReader(new FileReader(getApplicationContext().getFilesDir()+"/"+logname));
////            bsssid_view.setText(logIn.readLine());
//            int rows = 0;
//            String line = logIn.readLine();
//            while(line != null){
//                bsssid_view.setText(line);
//                line = logIn.readLine();
//                bsssid_view.setText(""+rows);
//            }
//            logIn.close();
//        } catch (FileNotFoundException e) {
//            Toast.makeText(this, "log file not found", Toast.LENGTH_LONG).show();
//        } catch (IOException e) {
//            Toast.makeText(this, "IO exception", Toast.LENGTH_LONG).show();
//        }
    }
    private String logToString() {
//        try {
//            Scanner sc = new Scanner(new File(getApplicationContext().getFilesDir()+"/"+logname));
//            String res = sc.useDelimiter("\\Z").next();
//            sc.close();
//            return res;
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        StringBuilder res = new StringBuilder();
        try {
            BufferedReader in = new BufferedReader(new FileReader(new File(getApplicationContext().getFilesDir()+"/"+logname)));
            String line = in.readLine();
            while(line != null){
                res.append(line);
                res.append("\n");
                line = in.readLine();
            }
            return res.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "";
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

}
