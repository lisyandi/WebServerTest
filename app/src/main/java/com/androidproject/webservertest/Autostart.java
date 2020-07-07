package com.androidproject.webservertest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class Autostart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent arg1) {
        // TODO Auto-generated method stub
        Log.w("log_webservertest", "starting service from boot completed");
        //Toast.makeText(context,"Service Started", Toast.LENGTH_SHORT).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, TestServices.class));
        } else {
            context.startService(new Intent(context, TestServices.class));
        }
    }
}
