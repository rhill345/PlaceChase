package com.android.placechase.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by randy on 10/23/2016.
 * Handles Sql setup and connections to the Contact history
 */
public class DatabaseWrapper extends SQLiteOpenHelper {

    public static final String CONTACT_SHARE            = "contact_shares";
    public static final String CONTACT_SHARE_ID         = "_id";
    public static final String CONTACT_SHARE_NAME       = "_name";
    public static final String CONTACT_SHARE_TYPE       = "_share_type";
    public static final String CONTACT_SHARE_PLACEID    = "_place_id";
    public static final String CONTACT_SHARE_PLACE_NAME = "_place_name";

    private static final String DATABASE_NAME           = "ContactShare.db";
    public static final int DATABASE_VERSION            = 1;

    // creation SQLite statement
    private static final String DATABASE_CREATE = "create table " + CONTACT_SHARE
            + "(" + CONTACT_SHARE_ID         + " integer primary key autoincrement, "
            + CONTACT_SHARE_NAME       + " text not null, "
            + CONTACT_SHARE_TYPE       + " text not null, "
            + CONTACT_SHARE_PLACEID    + " text not null, "
            + CONTACT_SHARE_PLACE_NAME + " text not null);";

    public DatabaseWrapper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + CONTACT_SHARE);
        onCreate(db);
    }
}