package com.android.placechase;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.android.placechase.fragments.ContactsHistoryFragment;
import com.android.placechase.fragments.IContactFragmentChangeListener;
import com.android.placechase.fragments.PlacePagerHistoryFragment;

/**
 * This activity handles all fragments in the Contact history UI Flow
 */
public class ContactsHistoryActivity extends AppCompatActivity implements IContactFragmentChangeListener {
	private ContactsHistoryFragment mContactsFragment;
	private PlacePagerHistoryFragment mPlaceFragment;
    private Toolbar mToolbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_generic);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle(getString(R.string.activity_title_share_history));
		setContactsHistoryFragment();
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

	public void setContactsHistoryFragment() {
        if(mContactsFragment == null)
			mContactsFragment = ContactsHistoryFragment.newInstance();
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.fragmentView, mContactsFragment);
		fragmentTransaction.commit();
	}

	public void setPlaceHistoryFragment(String contactName) {
		mPlaceFragment = PlacePagerHistoryFragment.newInstance(contactName);

		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction fragmentTransaction = fm.beginTransaction();
		fragmentTransaction.replace(R.id.fragmentView, mPlaceFragment,PlacePagerHistoryFragment.class.getName());
		fragmentTransaction.addToBackStack(PlacePagerHistoryFragment.class.getName());
		fragmentTransaction.commit();
	}

	@Override
	public void OnSetPlaceHistoryForUser(String contactName) {
		setPlaceHistoryFragment(contactName);
	}

	@Override
	public void OnFinishActivity() {
		finish();
	}

	@Override
	public void onBackPressed() {
		int count = getFragmentManager().getBackStackEntryCount();
		if (count == 0) {
			super.onBackPressed();
		} else {
			getFragmentManager().popBackStack();
		}
	}
}
