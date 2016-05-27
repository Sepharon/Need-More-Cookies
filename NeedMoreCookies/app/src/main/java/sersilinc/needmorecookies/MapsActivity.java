package sersilinc.needmorecookies;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.annotation.NonNull;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


/**
 * This class shows the current location of the user, as well as nearby stores.
 * REFERENCE: http://code.tutsplus.com/tutorials/android-sdk-working-with-google-maps-displaying-places-of-interest--mobile-16145
 */


public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleMap.OnMyLocationButtonClickListener {

    //TAG for Logs
    private final String TAG = "MapsActivity: ";

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    //Location
    //Request code for location permission request.
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    //Flag indicating whether a requested permission has been denied after returning in
    private boolean mPermissionDenied = false;


    // Might be null if Google Play services APK is not available.
    private GoogleMap mMap;

    //Places
    private Marker userMarker;
    private Marker[] placeMarkers;
    private MarkerOptions[] places;
    boolean missingValue=false;
    private int userIcon, foodIcon, storeIcon, shopIcon;

    //URLconnection to send a HTTPS get request
    private static HttpsURLConnection urlConnection;

    /**
     * Override onCreate method.
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        /**[START NAVIGATION MENU]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar3);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view3);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        /**[END NAVIGATION MENU]**/

        /**[START Location]**/
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        /**[END LOCATION]**/

        /**[START GoogleApiClient]**/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        /**[END SIGN OUT]**/

        /**[START GooglePlaces Api]**/
        //Places
        placeMarkers = new Marker[20];
        userIcon = R.drawable.yellow_point;
        foodIcon = R.drawable.red_point;
        storeIcon = R.drawable.blue_point;
        shopIcon = R.drawable.green_point;
        /**[END GooglePlaces Api]**/

        //Set portrait for phones and landscape for tablets
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        //If offline mode is active, it will not be possible to display nearby stores
        if (User_Info.getInstance().getOffline_mode())
            Toast.makeText(MapsActivity.this,R.string.no_connection_maps,Toast.LENGTH_SHORT).show();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnMyLocationButtonClickListener(this);
        enableMyLocation();
    }

    /**
     * Override onResume method.
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Override onBackPressed method.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        try {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                finish();
                super.onBackPressed();
            }
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    /**
     * Override onNavigationItemSelected method.
     * @param item MenuItem
     * @return True
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(MapsActivity.this, MapsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_home) {
            Intent intent = new Intent(MapsActivity.this, MainActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
        } else if (id == R.id.nav_share) {
            Intent mail_intent = new Intent(Intent.ACTION_SEND);
            mail_intent.setType("message/rfc822");
            // Body of mail
            mail_intent.putExtra(Intent.EXTRA_SUBJECT,"Try Need More Cookies!");
            mail_intent.putExtra(Intent.EXTRA_TEXT,"I invite you to try this awesome app! You will be able to write and share shopping lists " +
                    "with your friends! \nDownload it here: test.com \nYour friend: " + User_Info.getInstance().getName());
            Intent final_intent = Intent.createChooser(mail_intent,"Choose mail client");
            final_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Start Mail chooser
            startActivity(final_intent);

        } else if (id == R.id.nav_logout) {
            signOut();
            Intent intent = new Intent();
            intent.setAction("broadcast_service");
            intent.putExtra("Request","finish_activity");
            sendBroadcast(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout3);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sign out from Google Account and go to Login activity.
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(MapsActivity.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Override onStart method.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * Override onStop method.
     */
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Enables the My Location layer if the fine location permission has been granted.
     */
    private void enableMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            // Access to the location has been granted to the app.
            mMap.setMyLocationEnabled(true);
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    /**
     * Override onMyLocationButtonClick method. If the user presses the My Location button and Location is enabled
     * it will show nearby stores as well as the current position of the user.
     * @return False
     */
    @Override
    public boolean onMyLocationButtonClick() {
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else if (mMap != null) {
            LocationManager locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            try {
                Location lastLoc = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                double lat = lastLoc.getLatitude();
                double lng = lastLoc.getLongitude();
                LatLng lastLatLng = new LatLng(lat, lng);
                if (userMarker != null) userMarker.remove();
                userMarker = mMap.addMarker(new MarkerOptions()
                        .position(lastLatLng)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.fromResource(userIcon))
                        .snippet("Your last recorded location"));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(lastLatLng), 3000, null);

                //Send request to receive nearby stores
                updatePlaces(lat, lng);
            } catch (NullPointerException e) {
                Toast.makeText(MapsActivity.this, "Please, enable My Location button", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Override onRequestPermissionsResult method. Checks for permissions.
     * @param requestCode Request Code
     * @param permissions Permissions
     * @param grantResults Grant Results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }
        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    /**
     * Override onResumeFragments method.
     */
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private void showMissingPermissionError() {
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Override onConfigurationChanged method to configure the orientation of the screen
     * @param newConfig New configuration
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    /**
     * Prepare the request and executes an asynchronous task.
     * @param lat latitude
     * @param lng longitude
     */
    private void updatePlaces(double lat, double lng) {
        String placesSearchStr = "https://maps.googleapis.com/maps/api/place/nearbysearch/" +
                "json?location=" + lat + "," + lng +
                "&radius=1000&sensor=true" +
                "&types=food|bakery|store|convenience_store|electronics_store|grocery_or_supermarket|liquor_store|shopping_mall|store" +
                "&key=AIzaSyCluvWCx_dGHIXx0jd1pCEFrM6MkYKRAeA";
        new GetPlaces().execute(placesSearchStr);

    }

    /**
     * This class is an asynchronous task responsible to send the GET request and process the result.
     */
    private class GetPlaces extends AsyncTask<String, Void, String> {
        //fetch and parse place data
        @Override
        protected String doInBackground(String... placesURL) {
            //fetch places
            StringBuilder sb = new StringBuilder();
            //process search parameter string(s)
            for (String placeSearchURL : placesURL) {
                //execute search
                try {
                    Log.v(TAG, "REQUEST: "+placeSearchURL);
                    //Send request
                    URL link_url = new URL(placeSearchURL);
                    urlConnection = (HttpsURLConnection) link_url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setRequestProperty("Content-Type", "application/json");
                    //urlConnection.setRequestProperty("Accept", "application/json");
                    urlConnection.setReadTimeout(5000);
                    urlConnection.connect();
                    Log.v(TAG, "Connected");

                    //Read
                    InputStream stream = urlConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) sb.append(line);
                    bufferedReader.close();

                } catch (MalformedURLException e) {
                    Log.v(TAG, "Malformed");
                    e.printStackTrace();
                } catch (IOException e) {
                    Log.v(TAG, "IOException");
                    e.printStackTrace();
                } finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();

                        Log.v(TAG, "Disconnected");
                    }
                }
            }
            Log.v(TAG, "RESPONSE: "+sb.toString());
            return sb.toString();
        }

        protected void onPostExecute(String result) {
            LatLng placeLL=null;
            String placeName="";
            String vicinity="";
            int currIcon = foodIcon;
            //parse place data returned from Google Places
            if (placeMarkers != null) {
                for (int pm = 0; pm < placeMarkers.length; pm++)
                    if (placeMarkers[pm] != null) placeMarkers[pm].remove();

                try {
                    //parse JSON
                    JSONObject resultObject = new JSONObject(result);
                    JSONArray placesArray = resultObject.getJSONArray("results");
                    places = new MarkerOptions[placesArray.length()];
                    //loop through places
                    for (int p=0; p<placesArray.length(); p++) {
                        //parse each place
                        try{
                            //attempt to retrieve place data values
                            JSONObject placeObject = placesArray.getJSONObject(p);
                            JSONObject loc = placeObject.getJSONObject("geometry").getJSONObject("location");
                            placeLL = new LatLng(
                                    Double.valueOf(loc.getString("lat")),
                                    Double.valueOf(loc.getString("lng")));
                            JSONArray types = placeObject.getJSONArray("types");
                            //food|bakery|store|convenience_store|electronics_store|grocery_or_supermarket|liquor_store|shopping_mall|store"
                            for(int t=0; t<types.length(); t++){
                                //what type is it
                                String thisType=types.get(t).toString();
                                if(thisType.contains("food") || thisType.contains("bakery")){
                                    currIcon = foodIcon;
                                    break;
                                }
                                else if(thisType.contains("store") || thisType.contains("convenience_store") || thisType.contains("electronics_store") || thisType.contains("liquor_store")){
                                    currIcon = storeIcon;
                                    break;
                                }
                                else if(thisType.contains("grocery_or_supermarket") || thisType.contains("shopping_mall")){
                                    currIcon = shopIcon;
                                    break;
                                }
                            }
                            vicinity = placeObject.getString("vicinity");
                            placeName = placeObject.getString("name");
                        }
                        catch(JSONException jse){
                            missingValue=true;
                            jse.printStackTrace();
                        }

                        if(missingValue) places[p]=null;
                        else
                            places[p]=new MarkerOptions()
                                    .position(placeLL)
                                    .title(placeName)
                                    .icon(BitmapDescriptorFactory.fromResource(currIcon))
                                    .snippet(vicinity);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if(places!=null && placeMarkers!=null){
                    for(int p=0; p<places.length && p<placeMarkers.length; p++){
                        //will be null if a value was missing
                        if(places[p]!=null)
                            placeMarkers[p]=mMap.addMarker(places[p]);
                    }
                }
            }
        }
    }
}

