package com.android.placechase.fragments;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.placechase.R;
import com.android.placechase.adapters.ContactsAdapter;
import com.android.placechase.model.ContactShareModel;
import com.android.placechase.utils.Constants;
import com.android.placechase.utils.GeneralUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


public class ContactsShareFragment extends Fragment {

    private RecyclerView mContactsListView;
    private ContactsAdapter mAdapter;
    private LoadContact mLoadContacts;
    private String mPlaceId;
    private String mPlaceName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list,container,false);

        mContactsListView = GeneralUtils.initRecyclerViewSettings(getActivity(),
                (RecyclerView)v.findViewById(R.id.recycler_view));

        loadContacts();
        return v;
    }

    /*
   *  For version M and greater, Permissions must be granted by the user to access contacts
   *  content provider.  THis method attempts to gain permissions then loads the contacts
   *  in a background task
   * */
    private void loadContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getActivity(),android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS}, Constants.PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            mLoadContacts = new LoadContact(getActivity().getContentResolver());
            mLoadContacts.execute();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults) {
        if (requestCode == Constants.PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContacts();
            } else {

            }
        }
    }

    /*
   * Create a new instance of the fragment for a given placeId and placeName
   * */
    public static ContactsShareFragment newInstance(String placeId, String placeName){
        ContactsShareFragment fragment = new ContactsShareFragment();
        fragment.setPlaceId(placeId);
        fragment.setPlaceName(placeName);
        return fragment;
    }

    /*
   *  Set place ID for fragment
   * */
    private void setPlaceId(String placeId) {
        mPlaceId = placeId;
    }

    /*
   *  Set place Name for fragment
   * */
    private void setPlaceName(String placeName) {
        mPlaceName = placeName;
    }

    // Load contact data in background as this can be a time consuming task
    class LoadContact extends AsyncTask<Void, Void, Void> {

        private ContentResolver mContentResolver;
        private List mContactList;

        public LoadContact(ContentResolver contentResolver){
            mContentResolver = contentResolver;
            mContactList = new LinkedList<ContactShareModel>();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            /* Get Contact list from Phone content provider
            * */
            LinkedHashMap<String,ContactShareModel> contacts = new LinkedHashMap<>();
            String[] PROJECTION = new String[] {
                    ContactsContract.RawContacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Email.DATA };

            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String filter = ""+ ContactsContract.Contacts.HAS_PHONE_NUMBER + " > 0 and " + ContactsContract.CommonDataKinds.Phone.TYPE +"=" + ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
            String order = ContactsContract.Contacts.DISPLAY_NAME + " ASC";

            Cursor contactCursor = mContentResolver.query(uri, PROJECTION, filter, null, order);

            if (contactCursor != null) {
                Log.e("count",  "" + contactCursor.getCount());
                if (contactCursor.getCount() == 0)
                    Toast.makeText(getActivity(), "No contacts in your contact list.", Toast.LENGTH_LONG).show();

                while (contactCursor.moveToNext()) {
                    ContactShareModel model = new ContactShareModel(contactCursor);
                    String contactId = contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                    Cursor emails = mContentResolver.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                    + " = " + contactId, null, null);
                    while (emails.moveToNext()) {
                        String emailAddress = emails
                                .getString(emails
                                        .getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                        model.setEmail(emailAddress);
                        break;
                    }
                    emails.close();
                    contacts.put(model.getName(),model);
                }
                contactCursor.close();
                mContactList.addAll(contacts.values());
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mAdapter = new ContactsAdapter(getActivity(),mContactList,mPlaceId,mPlaceName);
            mContactsListView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mLoadContacts != null)
            mLoadContacts.cancel(true);
    }


}
