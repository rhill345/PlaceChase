package com.android.placechase.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.placechase.R;
import com.android.placechase.adapters.ContactsAdapter;
import com.android.placechase.database.DBOperations;
import com.android.placechase.model.ContactShareModel;
import com.android.placechase.utils.GeneralUtils;

import java.util.LinkedList;
import java.util.List;

public class PlaceHistoryFragment extends ContactListLoadFragment {
    private String mContactName;
    private String mType;

    @Override
    protected List getDataLoadList() {
        DBOperations shareDBOperation = new DBOperations(getActivity());
        shareDBOperation.open();
        List sharedContacts = shareDBOperation.getSharedPlacesByContacts(mContactName,mType);
        shareDBOperation.close();
        return sharedContacts;
    }

    @Override
    protected ContactsAdapter getContactsAdapter(List contactsList) {
        return new ContactsAdapter(getActivity(), contactsList, ContactsAdapter.TYPE_PLACE_HISTORY_LIST);
    }

    public static PlaceHistoryFragment newInstance(String contact, String type){
        PlaceHistoryFragment fragment = new PlaceHistoryFragment();
        fragment.setType(type);
        fragment.setContactName(contact);
        return fragment;
    }

    public void setType(String type) {
        mType = type;
    }

    public void setContactName(String contactName) {
        mContactName = contactName;
    }

}
