package com.android.placechase;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.placechase.fragments.ContactsShareFragment;
import com.android.placechase.utils.Constants;

/**
 * This activity shows a list of contacts form the
 * users contact list for sharing a given place by E-mail or Text message
 */
public class ShareWithActivity extends AppCompatActivity {
	private ContactsShareFragment mContactsActivity;
    private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generic);

        String placeIdToShare = getIntent().getStringExtra(Constants.EXTRA_PLACE_ID);
        String placeNameToShare = getIntent().getStringExtra(Constants.EXTRA_PLACE_NAME);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.activity_title_share_with));
        setContactsFragment(placeIdToShare,placeNameToShare);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

	public void setContactsFragment(String placeId,String placeName) {
        if(mContactsActivity == null)
		    mContactsActivity = ContactsShareFragment.newInstance(placeId,placeName);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.fragmentView, mContactsActivity);
		fragmentTransaction.commit();
	}
}
