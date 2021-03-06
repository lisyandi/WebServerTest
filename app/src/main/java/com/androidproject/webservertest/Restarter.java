package com.androidproject.webservertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("Broadcast Listened", "Service tried to stop");
        //Toast.makeText(context, "Service restarted", Toast.LENGTH_SHORT).show();
        //context.startService(new Intent(context, TestServices.class));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, TestServices.class));
        } else {
            context.startService(new Intent(context, TestServices.class));
        }
    }
}
