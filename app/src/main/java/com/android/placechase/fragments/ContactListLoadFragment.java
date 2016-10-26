package com.android.placechase.fragments;

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
import com.android.placechase.model.ContactShareModel;
import com.android.placechase.utils.GeneralUtils;

import java.util.LinkedList;
import java.util.List;

/*
* This abstract class will start loading and list of data
* and set the data to an adapter.  The adapter and list
* must be implemented by the implementing class
*
* */
public abstract class ContactListLoadFragment extends Fragment {

    private RecyclerView      mContactsListView;
    protected ContactsAdapter mAdapter;
    private LoadListDataTask  mLoadDataTask;
    protected TextView        mMessageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_contact_list,container,false);

        mMessageView      = (TextView)v.findViewById(R.id.message_view);
        mContactsListView = GeneralUtils.initRecyclerViewSettings(getActivity(),
                                (RecyclerView)v.findViewById(R.id.recycler_view));

        // Start contact loading task on launch
        mLoadDataTask     = new LoadListDataTask();
        mLoadDataTask.execute();
        return v;
    }

    // Implementing classes must return a list of "Data" to display
    protected abstract List getDataLoadList();

    //The adapter must be init by calling class
    protected abstract ContactsAdapter getContactsAdapter(List contactsList);



    class LoadListDataTask extends AsyncTask<Void, Void, Integer> {
        private List mDataList;

        public LoadListDataTask(){
            mDataList = new LinkedList<>();
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            mDataList.addAll(getDataLoadList());
            return mDataList.size();
        }

        @Override
        protected void onPostExecute(Integer count) {
            super.onPostExecute(count);
            if(count > 0) {
                clearMessages();
                mAdapter = getContactsAdapter(mDataList);
                mAdapter.setFragmentChangeListener((IContactFragmentChangeListener) getActivity());
                mContactsListView.setAdapter(mAdapter);
            } else {
                showMessage(getActivity().getString(R.string.history_error_empty));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mLoadDataTask != null)
            mLoadDataTask.cancel(true);
    }


    private void showMessage(String message) {
        mMessageView.setVisibility(View.VISIBLE);
        mMessageView.setText(message);
    }

    private void clearMessages() {
        mMessageView.setVisibility(View.INVISIBLE);
    }
}
