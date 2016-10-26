package com.android.placechase;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.placechase.fragments.HomeFragment;
import com.android.placechase.utils.GeneralUtils;

import java.util.regex.Pattern;


/**
 * This activity handles all fragments in the Contact history UI Flow
 */

public class MainActivity extends AppCompatActivity {

    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private HomeFragment mHomeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Initializing Toolbar and setting it as the actionbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.blackTransparent));



        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);


        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {


                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.history:
                        GeneralUtils.openContactHistoryActivity(MainActivity.this);
                        return true;
                    case R.id.settings:
                        Toast.makeText(getApplicationContext(),"Created by Randy Hill",Toast.LENGTH_SHORT).show();
                        return true;

                }
                return false;
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();

        // Set the home fragment
        setHomeFragment();

        initNaveHeader(navigationView.getHeaderView(0));

    }

    /* The header will have the app owner email and email id.
    *  */
    private void initNaveHeader(View v) {
        String email = getAccountEmail();
        TextView usernameView  = (TextView) v.findViewById(R.id.username);
        TextView emailView  = 	(TextView) v.findViewById(R.id.email);
        if (!email.isEmpty()) {
            emailView.setText(email);
            if(email.contains("@")) {
                usernameView.setText(email.split("@")[0]);
            }
        }
    }

    /*  The method grabs the account details from the Account manger
     *  to displace in the Nave header
     */
    private String getAccountEmail() {
        String possibleEmail = "";
        Pattern emailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
                break;
            }
        }
        return possibleEmail;
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setHomeFragment() {
        if(mHomeFragment == null)
            mHomeFragment = new HomeFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,mHomeFragment);
        fragmentTransaction.commit();
    }
}
