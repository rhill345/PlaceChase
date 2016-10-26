package com.android.placechase.fragments;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.placechase.R;
import com.android.placechase.enums.EShareType;



/*
* This Fragment is a View pager that holds other
* Places fragments.  The contact fragments and a list of sent places and
* received places
* */
public class PlacePagerHistoryFragment extends Fragment {

    private ViewPager mPager;
    private ViewPagerAdapter mPagerAdapter;
    private TabLayout mTabLayout;
    private String mContactName;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_place_pager,container,false);

        mTabLayout = (TabLayout) v.findViewById(R.id.tabHost);


        mTabLayout.addTab(mTabLayout.newTab().setText(getContext().getString(R.string.pager_sent_title)));
        mTabLayout.addTab(mTabLayout.newTab().setText(getContext().getString(R.string.pager_received_title)));
        mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        mPager = (ViewPager) v.findViewById(R.id.pager);

        // init view pager
        mPagerAdapter = new ViewPagerAdapter(getActivity().getSupportFragmentManager(),mTabLayout.getTabCount());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(2);
        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));


        mTabLayout.setOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(mPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);
                        mPager.setCurrentItem(tab.getPosition());
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {
                        super.onTabUnselected(tab);
                    }

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {
                        super.onTabReselected(tab);
                    }
                }
        );

        return v;
    }

    /*
    * Create a new instance of the pager for a given contact
    * */
    public static PlacePagerHistoryFragment newInstance(String contactName){
        PlacePagerHistoryFragment fragment = new PlacePagerHistoryFragment();
        fragment.setContactName(contactName);
        return fragment;
    }

    private void setContactName(String contactName) {
        mContactName = contactName;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    private class ViewPagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        public ViewPagerAdapter(FragmentManager fm, int numOfTabs) {
            super(fm);
            mNumOfTabs = numOfTabs;
        }
        public Fragment getItem(int num) {
            if(num == 0)
                return PlaceHistoryFragment.newInstance(mContactName, EShareType.SHARE_TYPE_SEND.name());
            else
                return PlaceHistoryFragment.newInstance(mContactName, EShareType.SHARE_TYPE_RECEIVE.name());
        }
        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }


}
