package com.android.placechase.model;

import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Created by randy on 10/22/2016.
 * Class to handle share and Database data
 */
public class ContactShareModel {
    private int    mId;
    private String mName;
    private String mEmail;
    private String mPhone;
    private String mType;
    private String mPlaceId;
    private String mPlaceName;

    public ContactShareModel() {}

    public ContactShareModel(Cursor c) {
        mName  = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        mPhone = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        mEmail = "";
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getPhone() {
        return mPhone;
    }

    public String getType() {
        return mType;
    }

    public void setType(String mType) {
        this.mType = mType;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public void setPlaceId(String mPlaceId) {
        this.mPlaceId = mPlaceId;
    }

    public String getPlaceName() {
        return mPlaceName;
    }

    public void setPlaceName(String mPlaceName) {
        this.mPlaceName = mPlaceName;
    }

}