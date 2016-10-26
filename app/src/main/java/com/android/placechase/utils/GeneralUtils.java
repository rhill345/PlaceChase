package com.android.placechase.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

import com.android.placechase.ContactsHistoryActivity;
import com.android.placechase.ShareWithActivity;
import com.android.placechase.MainActivity;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by randy on 10/22/2016.
 */
public class GeneralUtils {

    /**
     * Helper method tp send a browser link intent to any browser.
     *
     * @param context The calling context
     * @param uri the URI to open
     * @return N/A
     */
    public static void openUrl(Context context, Uri uri) {
        if(uri == null) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            // TODO: Handle exception if activity does not exists
        }
    }

    /**
     * Helper method to open the google maps navigation application.
     *
     * @param context The calling context
     * @param latLng The
     * @return N/A
     */
    public static void openNavigation(Context context, LatLng latLng ) {
        if(latLng == null) return;

        String lon = Double.toString(latLng.longitude);
        String lat = Double.toString(latLng.latitude);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?daddr="+lat+","+lon));
        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            // TODO: Handle exception if activity does not exists
        }
    }

    public static void openContactShareActivity(Context context, Place place) {
        Intent intent = new Intent(context, ShareWithActivity.class);
        intent.putExtra(Constants.EXTRA_PLACE_ID,place.getId());
        intent.putExtra(Constants.EXTRA_PLACE_NAME,place.getName());
        try {
            context.startActivity(intent);
        } catch (Exception ex) {
            // TODO: Handle exception if activity does not exists
        }
    }

    /**
     * Helper method to open the Contact history activity.
     *
     * @param context The calling context
     * @return N/A
     */
    public static void openContactHistoryActivity(Context context) {
        Intent myIntent = new Intent(context, ContactsHistoryActivity.class);
        context.startActivity(myIntent);
    }

    /**
     * Helper method to open the Contact history activity. If the home
     * Activity is already open - it should be brought to the from of
     * the call stack
     *
     * @param context The calling context
     * @param url Uri the the calling activity parses to cary the intent data
     * @return N/A
     */
    public static void openHomeActivity(Context context, Uri url) {
        Intent myIntent = new Intent(context, MainActivity.class);
        myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        myIntent.setData(url);
        context.startActivity(myIntent);
    }


    /**
     * Helper method initialize a RecyclerView settings
     *
     * @param context The calling context
     * @param view the RecyclerView to initialize
     * @return Initialized RecyclerView
     */
    public static RecyclerView initRecyclerViewSettings(Context context, RecyclerView view) {
        view.setHasFixedSize(false);
        view.setLayoutManager(new LinearLayoutManager(context));
        view.setClipToPadding(false);
        view.setFadingEdgeLength(0);
        view.setFitsSystemWindows(true);
        view.setScrollBarStyle(ListView.SCROLLBARS_OUTSIDE_OVERLAY);
        return view;
    }


}
