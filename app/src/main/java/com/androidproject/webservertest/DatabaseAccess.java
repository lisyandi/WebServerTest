package com.androidproject.webservertest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    Cursor c = null;

    private  DatabaseAccess(Context context){
        this.openHelper = new DatabaseHelper(context);
    }

    public static DatabaseAccess getInstance(Context context){
        if(instance == null){
            instance = new DatabaseAccess(context);
        }
        return instance;
    }

    public void open(){
        this.db = openHelper.getWritableDatabase();
    }

    public void close(){
        if(db != null){
            this.db.close();
        }
    }

    public BranchConfig getConfig(){
        BranchConfig config = new BranchConfig();
        Cursor cursor = db.rawQuery("SELECT * FROM BranchConfig", null);
        if (cursor.moveToFirst()) {
            config.setCode(cursor.getString(0));
            config.setName(cursor.getString(1));
            config.setCheck_process(cursor.getString(2));
            config.setUuid(cursor.getString(3));
        }
        cursor.close();
        return config;
    }

    public boolean addEntryLog(EntryLog log){
        try {
            ContentValues cv = new ContentValues();
            cv.put("id", log.getId());
            cv.put("nric", log.getNric());
            cv.put("process_date", log.getProcess_date());

            long result = db.insert("EntryLog", null, cv);
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        catch (SQLiteException e){
            Log.w("Database Error :", e.getMessage());
            return false;
        }
    }

    public boolean addRequestLog(RequestLog log){
        try {
            ContentValues cv = new ContentValues();
            cv.put("id", log.getId());
            cv.put("request", log.getRequest());

            long result = db.insert("RequestLog", null, cv);
            if (result == -1) {
                return false;
            } else {
                return true;
            }
        }
        catch (SQLiteException e){
            Log.w("Database Error :", e.getMessage());
            return false;
        }
    }
}
