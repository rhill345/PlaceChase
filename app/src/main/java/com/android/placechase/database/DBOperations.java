package com.android.placechase.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.android.placechase.model.ContactShareModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by randy on 10/23/2016.
 */
public class DBOperations extends SQLiteOpenHelper {

    private DatabaseWrapper dbHelper;
    private SQLiteDatabase  database;

    public DBOperations(Context context) {
        super(context, DatabaseWrapper.CONTACT_SHARE, null, DatabaseWrapper.DATABASE_VERSION);
        dbHelper = new DatabaseWrapper(context);
    }

    /**
     * Opens a connection to the SQL Light DB
     */
    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Closes an open Connection to the SQL Light DB
     */
    public void close() {
        dbHelper.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Adds send or received contact place data to the share DB
     *
     * @param model the data to add to the db
     * @return N/A
     */
    public void addShareModel(ContactShareModel model) {

        /* Each contact should not be able to share the same thing twice.
           First check if already shared
         */
        SQLiteStatement s = database.compileStatement("SELECT count(*) FROM "
                                  + DatabaseWrapper.CONTACT_SHARE +" WHERE "
                                  + DatabaseWrapper.CONTACT_SHARE_NAME + "="
                                  + DatabaseUtils.sqlEscapeString(model.getName()) + " AND "
                                  + DatabaseWrapper.CONTACT_SHARE_PLACEID
                                  + "='" + model.getPlaceId()+"'");


        if(s.simpleQueryForLong() > 0)
            return;
        ContentValues values = new ContentValues();

        values.put(DatabaseWrapper.CONTACT_SHARE_NAME, model.getName());
        values.put(DatabaseWrapper.CONTACT_SHARE_PLACEID, model.getPlaceId());
        values.put(DatabaseWrapper.CONTACT_SHARE_PLACE_NAME, model.getPlaceName());
        values.put(DatabaseWrapper.CONTACT_SHARE_TYPE, model.getType());

        try {
            long studId = database.insertOrThrow(DatabaseWrapper.CONTACT_SHARE, null, values);
        } catch (Exception e) {
            Log.e("",e.toString());
        }
    }

    /**
     *  Removes send or received contact place data to the share DB
     *
     * @param shareId the id to delete from the db
     * @return N/A
     */
    public void deleteShareModel(long shareId) {
        database.delete(DatabaseWrapper.CONTACT_SHARE, DatabaseWrapper.CONTACT_SHARE_ID
                + " = " + shareId, null);
    }

    public List getSharedPlacesByContacts() {
        List shares = new ArrayList();

        Cursor cursor = database.query(true, DatabaseWrapper.CONTACT_SHARE, new String[] { }, null, null, DatabaseWrapper.CONTACT_SHARE_NAME, null, null, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            ContactShareModel share = parseShare(cursor);
            shares.add(share);
            cursor.moveToNext();
        }

        cursor.close();
        return shares;
    }

    /**
     *  Removes send or received contact place data to the share DB
     *
     * @param user The username(key) of contact
     * @param type The type (SHARE_TYPE_SEND,SHARE_TYPE_RECEIVE) to filter by
     *
     * @return List of sharedplaces for a given contact
     */
    public List getSharedPlacesByContacts(String user, String type) {
        List shares = new ArrayList();
        user = DatabaseUtils.sqlEscapeString(user);
        String query =  "SELECT * FROM " + DatabaseWrapper.CONTACT_SHARE + " WHERE "
                        + DatabaseWrapper.CONTACT_SHARE_NAME + "="+user + " AND "
                        + DatabaseWrapper.CONTACT_SHARE_TYPE + "='"+type+"'";
        Cursor cursor = database.rawQuery(query, null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            ContactShareModel share = parseShare(cursor);
            shares.add(share);
            cursor.moveToNext();
        }

        cursor.close();
        return shares;
    }

    private ContactShareModel parseShare(Cursor cursor) {
        ContactShareModel share = new ContactShareModel();
        share.setId((cursor.getInt(0)));
        share.setName(cursor.getString(1));
        share.setType(cursor.getString(2));
        share.setPlaceId(cursor.getString(3));
        share.setPlaceName(cursor.getString(4));
        return share;
    }
}