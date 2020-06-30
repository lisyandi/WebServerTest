package com.androidproject.webservertest;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fi.iki.elonen.NanoHTTPD;

public class TestServices extends Service {

    private WebServer server;
    public int counter=0;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "SafeEntry";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

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
            Log.w("Httpd", "Web server initialized.");
        } catch(IOException ioe) {
            Log.w("Httpd", "The server could not start.");
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (server != null)
            server.stop();
        //Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
        Log.w("Httpd", "Web server stopped");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    private class WebServer extends NanoHTTPD {

        public WebServer()
        {
            super(8080);
        }

        @Override
        public Response serve(IHTTPSession session) {
            DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
            databaseAccess.open();
            Process process = new Process();
            String deviceID = process.getDeviceID();
            String msg = "<html><body><h1>Hello server</h1>\n";
            Map<String, String> files = new HashMap<String, String>();
            Method method = session.getMethod();
            if (Method.PUT.equals(method) || Method.POST.equals(method)) {
                try {
                    session.parseBody(files);
                } catch (IOException ioe) {
                    //return new newFixedLengthResponse (Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
                } catch (ResponseException re) {
                    //return new Response(re.getStatus(), MIME_PLAINTEXT, re.getMessage());
                }
            }
            //String postBody = session.getQueryParameterString();
            String param = "";
            Map<String, String> postParameter = session.getParms();
            if(postParameter.size() != 0) {
                param = convertWithIteration(postParameter);
                RequestLog requestLog = new RequestLog();
                requestLog.setId(UUID.randomUUID().toString());
                requestLog.setRequest(method.toString() + param +";DeviceKey:"+deviceID);
                databaseAccess.addRequestLog(requestLog);
            }
            else{
                RequestLog requestLog = new RequestLog();
                requestLog.setId(UUID.randomUUID().toString());
                requestLog.setRequest(method.toString()+";DeviceKey:"+deviceID);
                databaseAccess.addRequestLog(requestLog);
            }

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

        public String convertWithIteration(Map<String, ?> map) {
            StringBuilder mapAsString = new StringBuilder("{");
            for (String key : map.keySet()) {
                mapAsString.append(key + "=" + map.get(key) + ", ");
            }
            mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
            return mapAsString.toString();
        }
    }
}
