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
            try {
                DatabaseAccess databaseAccess = DatabaseAccess.getInstance(getApplicationContext());
                databaseAccess.open();
                Process process = new Process();
                String sUUID = UUID.randomUUID().toString();
                String sNRIC = "";

                Preferences preferences = new Preferences();
                String ProcessF = preferences.getProcessF(getApplicationContext());

                Date date = Calendar.getInstance().getTime();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String sDate = formatter.format(date);

                BranchConfig config = databaseAccess.getConfig();
                String config_code = config.getCode();
                String config_name = config.getCode();
                String config_checkProcess = config.getCheck_process();
                String config_deviceKey = config.getUuid();

                // Get Parameters
                String msg = "";
                Map<String, String> files = new HashMap<String, String>();
                Method method = session.getMethod();
                if (Method.PUT.equals(method) || Method.POST.equals(method)) {
                    try {
                        session.parseBody(files);
                    } catch (IOException ioe) {

                    } catch (ResponseException re) {

                    }
                }

                String param = "";
                Map<String, String> postParameter = session.getParms();
                String sDeviceKey = session.getParms().get("deviceKey");

                if (postParameter.size() != 0) {
                    sNRIC = session.getParms().get("idcardNum");
                    param = convertWithIteration(postParameter);
                    RequestLog requestLog = new RequestLog();
                    requestLog.setId(sUUID);
                    requestLog.setRequest(method.toString() + param +";Date=" + sDate);
                    databaseAccess.addRequestLog(requestLog);
                } else {
                    RequestLog requestLog = new RequestLog();
                    requestLog.setId(sUUID);
                    requestLog.setRequest(method.toString());
                    databaseAccess.addRequestLog(requestLog);
                }

                String action = config_checkProcess == "1" ? "CHECK_IN" : "CHECK_OUT";

                if (config_deviceKey == sDeviceKey) {
                    if (ProcessF == "1") {
                        boolean result = process.Action(action, "", config_code, config_name, sNRIC);

                        if (result) {
                            msg = "Success";
                        } else {
                            msg = "Failed";
                        }

                        String sMaskNRIC = sNRIC.replaceAll("\\w(?=\\w{4})", "*");
                        EntryLog log = new EntryLog();
                        log.setId(sUUID);
                        log.setNric(sMaskNRIC);
                        log.setProcess_date(sDate);
                        databaseAccess.addEntryLog(log);
                        preferences.setProcessF(getApplicationContext(), "0");
                    } else {
                        preferences.setProcessF(getApplicationContext(), "1");
                    }
                }
                databaseAccess.close();
                return newFixedLengthResponse(msg);
            }
            catch (Exception e){
                return newFixedLengthResponse("Error");
            }
        }

        public String convertWithIteration(Map<String, ?> map) {
            StringBuilder mapAsString = new StringBuilder("{");
            for (String key : map.keySet()) {
                if(key == "idcardNum" || key == "deviceKey") {
                    mapAsString.append(key + "=" + map.get(key) + ", ");
                }
            }
            mapAsString.delete(mapAsString.length()-2, mapAsString.length()).append("}");
            return mapAsString.toString();
        }
    }
}
