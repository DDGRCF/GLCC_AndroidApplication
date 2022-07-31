package com.glcc.client;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MDBOpenHelper extends SQLiteOpenHelper {
    public MDBOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MDBConstants.create_user_table_command);
        db.execSQL(MDBConstants.create_device_table_command);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
class MDBConstants {
    static String mdb_name = "glccclient.db";
    static int mdb_version = 1;
    static String create_user_table_command = "CREATE TABLE IF NOT EXISTS User(username VARCHAR(20) NOT NULL unique, " +
            "password VARCHAR(20) NOT NULL, nickname VARCHAR(20) NOT NULL, PRIMARY KEY (username));";
    static String create_device_table_command = "CREATE TABLE IF NOT EXISTS VideoDevice(room_key VARCHAR(50) NOT NULL UNIQUE, " +
            "username VARCHAR(20) NOT NULL, room_name VARCHAR(50), video_url VARCHAR(50) NOT NULL, PRIMARY KEY (room_key), FOREIGN KEY (username) REFERENCES User(usrname))";
}

