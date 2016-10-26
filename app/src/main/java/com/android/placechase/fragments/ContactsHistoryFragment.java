package com.android.placechase.fragments;

import com.android.placechase.adapters.ContactsAdapter;
import com.android.placechase.database.DBOperations;
import java.util.List;

/*
* This class will start loading and list of data by implementing
* the ContactListLoad abstract class.
* THis class query the place history db for a list of contacts
*
* */
public class ContactsHistoryFragment extends ContactListLoadFragment {

    @Override
    protected List getDataLoadList() {
        DBOperations shareDBOperation = new DBOperations(getActivity());
        shareDBOperation.open();
        List sharedContacts = shareDBOperation.getSharedPlacesByContacts();
        shareDBOperation.close();
        return sharedContacts;
    }

    @Override
    protected ContactsAdapter getContactsAdapter(List contactsList) {
        return new ContactsAdapter(getActivity(), contactsList, ContactsAdapter.TYPE_CONTACT_HISTORY_LIST);
    }


    public static ContactsHistoryFragment newInstance(){
        ContactsHistoryFragment fragment = new ContactsHistoryFragment();
        return fragment;
    }

}
