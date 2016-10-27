package com.android.placechase.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.SQLException;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.placechase.R;
import com.android.placechase.database.DBOperations;
import com.android.placechase.enums.EShareType;
import com.android.placechase.model.ContactShareModel;
import com.android.placechase.utils.Constants;
import com.android.placechase.utils.GeneralUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * This is the home fragment
 * This fragment handles the place map and all of the map interactions
 */

public class HomeFragment extends Fragment  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final String TAG = HomeFragment.this.getClass().getSimpleName();

    // Default lat and long for the map on load (NYC)
    private final double NYC_LAT = 40.712783699999996;
    private final double NYC_LON = -74.0059413;
    private MapView mMapView;
    private GoogleMap mMap;

    // Google place variables
    private Place mSelectedPlace;
    private GoogleApiClient mGoogleApiClient;

    // place card view that animates to visible when a place is selected
    private View mPlaceDetailsCard;

    // Database connection object for setting place history
    private DBOperations mShareDBOperation;

    // History button to view share history
    private FloatingActionButton mHistoryFab;

   /* We store a history of markers added to the map so we can
    access the place data when clicked
    */
    private Map<Marker,String> mPlaceIdHistoryMap;

    /*
    * The auto complete search may take some time to open,
    * we disable the search button some the user doesnt attempt
    * to open multiple times
    * */
    private boolean mSearchBtnEnabled = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);
        mHistoryFab = (FloatingActionButton)v.findViewById(R.id.history_fab);
        mHistoryFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.openContactHistoryActivity(getActivity());
            }
        });

        mShareDBOperation = new DBOperations(getContext());
        mPlaceDetailsCard = v.findViewById(R.id.place_details_card);
        mPlaceIdHistoryMap= new HashMap<>();

        initMapView(v,savedInstanceState);

        return v;
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_search) {
           /* Don't try to open search of its already being opened
              This will prevent duplicate search widgets from opening
            */
            if(!mSearchBtnEnabled) return true;
            try {
                Intent intent =
                        new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                                .build(getActivity());
                startActivityForResult(intent, Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE);
                mSearchBtnEnabled = false;
            } catch (GooglePlayServicesRepairableException e) {
                mSearchBtnEnabled = true;
            } catch (GooglePlayServicesNotAvailableException e) {
                mSearchBtnEnabled = true;
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void handleLaunchIntent(Intent intent) {
        if(intent == null || intent.getData() == null)
            return;

        //Handle new incoming launch intents from share links
        Uri uri = intent.getData();
        String placeId = uri.getQueryParameter(Constants.EXTRA_PLACE_ID);

        if(placeId == null || placeId.isEmpty())
            return;

        String noShare = uri.getQueryParameter(Constants.EXTRA_NO_SHARE);

        // if no share flag set, dont set name, this will prevent from saving share
        String  name   = (noShare != null ) ? "" : uri.getQueryParameter(Constants.EXTRA_NAME);

        // load place
        loadPlaceById(placeId,name);
        getActivity().setIntent(null);
    }

    /*
    * Methode to store contact and place into share db
    * */
    private void storeContactShare(String user, String placeName, String placeId) {
        ContactShareModel model = new ContactShareModel();
        model.setName(user);
        model.setPlaceName(placeName);
        model.setPlaceId(placeId);
        model.setType(EShareType.SHARE_TYPE_RECEIVE.name());
        mShareDBOperation.addShareModel(model);
    }

    @Override
    public void onResume() {
        mMapView.onResume();
        try {
            mShareDBOperation.open();
        } catch (SQLException e) {
            Log.e(TAG,e.toString());
        }
        handleLaunchIntent(getActivity().getIntent());
        super.onResume();
    }

    @Override
    public void onPause() {
        mShareDBOperation.close();
        mMapView.onPause();
        super.onPause();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                setPlace(place);
            }
            mSearchBtnEnabled = true;
        }
    }

    /*
    *  Sets the place details in the place card and updates map to place location
    * */
    private void setPlace(final Place place) {
        mSelectedPlace = place;

        MarkerOptions mo = new MarkerOptions().position(mSelectedPlace.getLatLng()).title(place.getName().toString());



        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mSelectedPlace.getLatLng(),13);

        //Store marker in map to load details later
        mPlaceIdHistoryMap.put(mMap.addMarker(mo), place.getId());
        mMap.animateCamera(cameraUpdate);
        mPlaceDetailsCard.findViewById(R.id.btn_share_action).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralUtils.openContactShareActivity(getContext(), place);
            }
        });
        ((TextView) mPlaceDetailsCard.findViewById(R.id.place_title)).setText(place.getName());
        ((TextView) mPlaceDetailsCard.findViewById(R.id.place_address)).setText(place.getAddress());
        setPlaceActions(place);
        showPlace();

    }

    /*
    *  Sets the place action buttons for a given place. Non-applicable buttons
    *  are not shown (eg. website)
    * */
    private void setPlaceActions(final Place place) {
        ViewGroup actionContainer = (ViewGroup) mPlaceDetailsCard.findViewById(R.id.action_container);
        //Close View
        setPlaceAction(R.id.place_card_action_close,
                        actionContainer,
                        R.drawable.ic_close_white,
                        getString(R.string.place_close),
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                hidePlace();
                            }
                        },
                true);

        setPlaceAction(R.id.place_card_action_website,
                actionContainer,
                R.drawable.ic_public_white,
                getString(R.string.place_website),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GeneralUtils.openUrl(getActivity(),place.getWebsiteUri());
                    }
                },
                place.getWebsiteUri()!=null);
        setPlaceAction(R.id.place_card_action_share,
                actionContainer,
                R.drawable.ic_directions_white,
                getString(R.string.place_navigate),
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GeneralUtils.openNavigation(getActivity(),place.getLatLng());
                    }
                },
                true);
    }

    /*
    *  Sets an individual place action button
    * */
    private void setPlaceAction(int id, ViewGroup parent,
                                int imageResource, String name, View.OnClickListener listener, boolean show) {
        if(parent.findViewById(id) == null) {
            View action = createCardAction(id,
                    parent,
                    imageResource,
                    name,
                    listener);
            action.setVisibility(show ? View.VISIBLE : View.GONE);
            parent.addView(action);
        } else {
            parent.findViewById(id).setVisibility(show ? View.VISIBLE : View.GONE);
            if(show)
                parent.findViewById(id).setOnClickListener(listener);
        }
    }

    /*
   *  Creates a place card action button given an icon and text.  The listener is
   *  the button action
   * */
    private View createCardAction(int id, ViewGroup parent, int imageResource, String name, View.OnClickListener listener) {
        View action = getActivity().getLayoutInflater().inflate(R.layout.button_card_action, parent, false);
        action.setId(id);
        ((TextView)action.findViewById(R.id.action_text)).setText(name);
        ((ImageView)action.findViewById(R.id.action_image)).setImageResource(imageResource);
        action.setOnClickListener(listener);
        return action;
    }

    /*
    *  Shows the place card with an animation
    * */
    private void showPlace() {
        mHistoryFab.hide();
        mPlaceDetailsCard.setVisibility(View.VISIBLE);
        Animation slideUp = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.slide_up);
        mPlaceDetailsCard.startAnimation(slideUp);
    }

    /*
    *  Hides the place card with an animation
    * */
    private void hidePlace() {
        Animation slideDown = AnimationUtils.loadAnimation(getActivity().getApplicationContext(),
                R.anim.slide_down);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mHistoryFab.show();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        mPlaceDetailsCard.startAnimation(slideDown);
        mPlaceDetailsCard.setVisibility(View.INVISIBLE);
        mSelectedPlace = null;
    }

    private void initMapView(View v, Bundle savedInstanceState) {
        // Gets the MapView from the XML layout and creates it
        mMapView = (MapView) v.findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);

        // Gets to GoogleMap from the MapView and does initialization stuff
        mMap = mMapView.getMap();
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                if(!mPlaceIdHistoryMap.containsKey(marker))
                    return;
                if(mSelectedPlace != null && !mSelectedPlace.getId().equals(mPlaceIdHistoryMap.get(marker)))
                    loadPlaceById(mPlaceIdHistoryMap.get(marker));
            }
        });

        // Update the location and zoom of the MapView.  The default is set to NYC
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(NYC_LAT, NYC_LON), 10);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "Connected to places successfully");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Connection to places suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Connection to places failed");
    }


    /*
    *  Loads a places details given an Id
    * */
    private void loadPlaceById(String placeId){
        loadPlaceById(placeId, null);
    }

    /*
    *  Loads a places details given an Id and sets the name of the person
    *  who shared the place
    * */
    private void loadPlaceById(final String placeId, final String name){
        if(mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient
                    .Builder(getContext())
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), HomeFragment.this)
                    .addOnConnectionFailedListener(HomeFragment.this)
                    .addOnConnectionFailedListener(HomeFragment.this)
                    .build();
        }
        Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        if (places.getStatus().isSuccess() && places.getCount() > 0) {
                            final Place myPlace = places.get(0);
                            if(name != null && !name.isEmpty()) {
                                storeContactShare(name,myPlace.getName().toString(),myPlace.getId());
                            }
                            setPlace(myPlace);
                            Log.i(TAG, "Place found: " + myPlace.getName());
                        } else {
                            Log.e(TAG, "Place not found");
                        }
                    }
                });
    }
}