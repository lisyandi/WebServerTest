package com.androidproject.webservertest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;

public class TestServices extends Service {

    private WebServer server;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        server = new WebServer();
        try {
            server.start();
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        Log.w("Httpd", "Web server initialized.");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.stop();
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            String msg = "<html><body><h1>Hello server</h1>\n";
            Map<String, String> parms = session.getParms();

            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
            databaseAccess.open();
            Preferences preferences = new Preferences();
            //String LastNRIC = preferences.getLastNRIC(getApplicationContext());
            String ProcessF = preferences.getProcessF(getApplicationContext());
            BranchConfig config = databaseAccess.getConfig();
            Date date = Calendar.getInstance().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String sDate = formatter.format(date);
            String sNRIC = "s9046831f";
            String sMaskNRIC = sNRIC.replaceAll("\\w(?=\\w{4})", "X");

            if(ProcessF == "1") {
                Process process = new Process();
                boolean result = process.Action("CHECK_IN", "", "067591","Quran Learning Centre", "s9046831f");
                if (result) {
                    msg ="<p>Success</p>";
                } else {
                    msg ="<p>Failed</p>";
                }

                EntryLog log = new EntryLog();
                log.setId(UUID.randomUUID().toString());
                log.setNric(sMaskNRIC);
                log.setProcess_date(sDate);
                databaseAccess.addEntryLog(log);
                preferences.setLastNRIC(getApplicationContext(), "0");
            }
            else{
                preferences.setLastNRIC(getApplicationContext(), "1");
            }

            databaseAccess.close();

            return newFixedLengthResponse(msg + "</body></html>\n");
        }
    }
}
