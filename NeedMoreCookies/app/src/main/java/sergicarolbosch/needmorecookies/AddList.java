package sergicarolbosch.needmorecookies;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * This class is used to give the user an UI to add a new Shopping List, the Shopping List and the name
 * are given back the the MainActivity class to process
 */
public class AddList extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    // DB
    DB_Helper db;

    //UI elements
    private ImageButton save;
    private EditText list_name;
    private Switch pub, priv;

    //Others
    private boolean first_time = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_list);
        // Make the Activity start with the software keyboard hidden
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        /**[START UI elements]**/
        list_name = (EditText) findViewById(R.id.list_name);
        priv = (Switch) findViewById(R.id.private_switch);
        pub = (Switch) findViewById(R.id.public_switch);
        save = (ImageButton) findViewById(R.id.save);
        /**[END UI elements]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_list);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_list);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/

        /**[START GoogleApiClient]**/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END GoogleApiClient]**/
        db = new DB_Helper(getApplicationContext());
        /**[START onCLickListeners]**/
        // If save is clicked save data to server and return it
        save.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Save list name, and if its public or private
            if (!list_name.getText().toString().equals("")) {
                // Check if a Shopping List with this name already exists
                if (!db.read_code(list_name.getText().toString()).equals("Error"))
                    Toast.makeText(AddList.this, R.string.list_name_error, Toast.LENGTH_SHORT).show();
                // Check that wither pub or piv is checked
                else if (pub.isChecked() || priv.isChecked()) {
                    Log.d("AddList: ", "Returning data");
                    // Return data to the MainActivity.java
                    Intent result_data = new Intent();
                    // Send the name of the Shopping List
                    result_data.putExtra("List_Name", list_name.getText().toString());
                    // And the type (Public or Private)
                    result_data.putExtra("Type", "" + priv.isChecked());
                    // Send result
                    setResult(MainActivity.RESULT_OK, result_data);
                    // Finish activity
                    finish();
                } else
                    Toast.makeText(AddList.this, "You must choose a public or private list", Toast.LENGTH_LONG).show();
            } else
                Toast.makeText(AddList.this, R.string.no_name_toast, Toast.LENGTH_LONG).show();
        }
        });

        // Public toggle button
        pub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If the pub toggle is not set nor is the priv toggle, make the save button invisible
                if (!pub.isChecked() && !priv.isChecked()){
                    first_time = true;
                    save.setVisibility(View.GONE);
                }
                // If the last time both toggles were not set and pub is toggled, play an animation
                if (pub.isChecked() && first_time) start_animation();
                // If pub is checked and priv is checked, toggle the value of priv
                if (pub.isChecked() && priv.isChecked()) priv.toggle();

            }
        });
        // Private toggle button
        priv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pub.isChecked() && !priv.isChecked()){
                    first_time = true;
                    save.setVisibility(View.GONE);
                }
                if (priv.isChecked() && first_time) start_animation();
                if (priv.isChecked() && pub.isChecked()) pub.toggle();

            }
        });
        /**[END onClickListeners]**/

        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }
    // This function is called when the app starts, after the onCreate method
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    // This method is called when the back button is pressed
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) drawer.closeDrawer(GravityCompat.START);
        else super.onBackPressed();

    }

    // Navigation drawer listener, is called when an item from the navigation drawer is clicked
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_locations) {
            Intent intent = new Intent(AddList.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(AddList.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(AddList.this,MainActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
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
            finish();

        } else if (id == R.id.nav_logout){
            signOut();
            Intent intent = new Intent();
            intent.putExtra("Request","finish_activity");
            intent.setAction("broadcast_service");
            sendBroadcast(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_list);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Start the fade in in animation
    private void start_animation(){
        first_time = false;
        Animation fadein = new AlphaAnimation(0,1);
        fadein.setDuration(1000);
        save.setVisibility(View.VISIBLE);
        save.setAnimation(fadein);
    }

    //Sign Out from Google Account
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(AddList.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
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
     * @param newConfig
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
}
