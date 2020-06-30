package com.androidproject.webservertest;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String PREFERENCES_NAME = "SAFE_ENTRY";
    private static final String KEY_LAST_NRIC = "LAST_NRIC";
    private static final String PROCESSF = "LAST_NRIC";

    public void setLastNRIC(Context context, String Value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_LAST_NRIC, Value);
        editor.commit();
    }

    public String getLastNRIC(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(KEY_LAST_NRIC, "");
    }

    public void setProcessF(Context context, String Value) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(PROCESSF, Value);
        editor.commit();
    }

    public String getProcessF(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        return sharedPref.getString(PROCESSF, "");
    }
}
