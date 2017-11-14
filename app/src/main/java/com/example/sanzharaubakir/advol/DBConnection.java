package com.example.sanzharaubakir.advol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by sanzharaubakir on 13.06.17.
 */

public class DBConnection {
    private static final String TAG = "DBConnection";
    ExternalDbOpenHelper dbOpenHelper;
    SQLiteDatabase database;
    Context context;
    String network = "Network";

    public DBConnection(Context ctx) {
        context = ctx;
    }
    private void dbOpen() {
        dbOpenHelper = new ExternalDbOpenHelper(context, "newDB");
        database = dbOpenHelper.getWritableDatabase();
    }
    private void dbClose(){
        database.close();
    }
    public void setMyCurrentNetwork(String networkName)
    {
        dbOpen();
        database.execSQL("CREATE TABLE IF NOT EXISTS "+ network +
                " ("+"ID"+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "name TEXT)");
        ContentValues cv = new ContentValues();
        cv.put("name", networkName);
        database.insert(network, null, cv);
        dbClose();
    }
    public String getMyNetowrk()
    {
        if (!checkExistence(network))
            return "";
        dbOpen();
        Cursor cursor = database.rawQuery("select * from " + network, null);
        if(cursor!=null) {
            return "";
        }
        int nameCI = cursor.getColumnIndex("name");
        return cursor.getString(nameCI);
    }
    private Boolean checkExistence(String tableName)
    {
        dbOpen();
        Cursor cursor = database.rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor!=null) {
            if(cursor.getCount()>0) {
                cursor.close();
                cursor = database.query(tableName, null, null, null, null, null, null);
                if (cursor!= null)
                {
                    if (!cursor.moveToFirst())
                    {
                        return false;
                    }
                }
                return true;
            }
            cursor.close();
        }
        dbClose();
        return false;
    }
    public void clearNetworkTable()
    {
        if (!checkExistence(network))
            return;
        dbOpen();
        database.delete(network, null, null);
        dbClose();
    }
}