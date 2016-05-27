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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

/**
 * This class has two modes: one is to add products to a concrete list and another is to edit an existing product.
 */

public class AddItem extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;

    //UI elements
    private EditText Product;
    private EditText Quantity;
    private EditText Price;
    private ImageButton Save;
    private Spinner type;

    //Flags
    private boolean product_added = false;
    private boolean quantity_added = false;

    //Strings
    private String edit;
    private String type_prod;

    /**
     * Override onCreate method.
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_item);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_item);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view_item);
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

        /**[START UI elements]**/
        Product = (EditText)findViewById(R.id.product);
        Quantity = (EditText)findViewById(R.id.quantity);
        Price = (EditText)findViewById(R.id.price);
        Save = (ImageButton)findViewById(R.id.save_item);
        type = (Spinner) findViewById(R.id.type);
        /**[END UI elements]**/


        /**[START Get intent extras]**/
        //Get JSON Strings from the MainActivity
        Bundle extras = getIntent().getExtras();
        try {
            edit = extras.getString("Edit");
            assert edit != null;
            if (edit.equals("True")) {
                String Product_name = extras.getString("Product");
                String Quantity_prod = extras.getString("Quantity");
                String Price_prod = extras.getString("Price");
                type_prod = extras.getString("Type");
                Product.setText(Product_name);
                Quantity.setText(Quantity_prod);
                Price.setText(Price_prod);
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        /**[END Get intent extras]**/


        /**[START Spinner]**/
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.type_of_products, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        type.setAdapter(adapter);

        //In case that we are in edit mode, fix the type of the product
        if (edit.equals("True")) {
            int pos = adapter.getPosition(type_prod);
            type.setSelection(pos);
            type.setEnabled(Boolean.FALSE);
        }
        /**[END Spinner]**/


        /**[START onClickListener]**/
        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent result_data = new Intent();
                result_data.putExtra("product", Product.getText().toString());
                result_data.putExtra("quantity", Quantity.getText().toString());
                result_data.putExtra("price", Price.getText().toString());
                result_data.putExtra("type",get_english_version(type.getSelectedItemPosition()));

                setResult(Items.RESULT_OK, result_data);
                finish();
            }
        });

        //If we are on edit mode, enable the save button from the begining, else disabled it
        if (edit.equals("True")) {
            Save.setEnabled(true);
            Save.setVisibility(View.VISIBLE);
        } else {
            Save.setEnabled(false);
            Save.setVisibility(View.GONE);
        }
        /**[END onClickListener]**/

        /**[START TextChangedListener]**/
        Product.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If we are not in edit mode, enable save button if product and price are filled in
                if (!edit.equals("True")) {
                    if (Product.getText().toString().equals("")) {
                        product_added = false;
                        Save.setVisibility(View.GONE);
                        Save.setEnabled(false);
                    } else {
                        product_added = true;
                    }
                    if (product_added && quantity_added) {
                        start_animation();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //If we are on edit mode, enable save button if product and quantity are filled in
                if (!edit.equals("True")) {
                    if (Quantity.getText().toString().equals("")) {
                        quantity_added = false;
                        Save.setVisibility(View.GONE);
                        Save.setEnabled(false);
                    } else {
                        quantity_added = true;
                    }

                    if (product_added && quantity_added) {
                        start_animation();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        /**[END TextChangedListener]**/

        //Set portrait orientation for phones and landscape for tablets
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }


    /**
     * Override method onStart. It connects the GoogleApiClient.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    /**
     * Override method onBackPressed. If the navigation menu is open, closed it.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_item);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Override method onNavigationItemSelected. Depeding on what the user chooses, start one activity or another.
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(AddItem.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(AddItem.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        }
        else if (id == R.id.nav_home) {
            Intent intent = new Intent(AddItem.this,MainActivity.class);
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

        } else if (id == R.id.nav_logout) {
            signOut();
            Intent intent = new Intent();
            intent.putExtra("Request","finish_activity");
            intent.setAction("broadcast_service");
            sendBroadcast(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_item);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sign out from the Google Account and go to Login activity.
     */

    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(AddItem.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Start animation to display the save button
     */
    private void start_animation(){
        Animation fadein = new AlphaAnimation(0,1);
        fadein.setDuration(1000);
        Save.setEnabled(true);
        Save.setVisibility(View.VISIBLE);
        Save.setAnimation(fadein);
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
     * @param newConfig New Configuration
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

    private String get_english_version(int product){
        String[] types = new String[]{"Meat and Fish", "Vegetables", "Cereal", "Dairy", "Sweet", "Others"};
        Log.v("AddItem","Type: " + types[product]);
        return types[product];
    }
}
