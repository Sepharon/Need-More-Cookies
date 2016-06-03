package sergicarolbosch.needmorecookies;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is the main class for the App, this class acts as the main UI interface as well as
 * a connection to most of the other classes, processing the SHopping List, establishing connection
 * to the server and working with the internal DB to synchronize entries.
 */
public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // Main tag for Logs
    private final String TAG = "Shopping_Lists: ";

    // Service elements
    private Messenger mService = null;
    private boolean is_bound = false;
    private Update_Server server_service;
    private boolean is_bound_server = false;

    //Receiver
    private String request_type;
    private String list;
    private String main;
    private String update_product;
    public MyReceiver receiver;
    private IntentFilter filter;

    // UI elements
    private Button private_lists;
    private Button public_lists;
    private View separator1;
    private View separator2;
    private TextView welcome;
    private ProgressBar loading;
    private View first_layout;
    private View third_layout;
    private AdView mAdView;
    private RecyclerView mRecyclerView;
    private View previous_selected_view;

    //Lists
    private String old_codes;
    private List<Shopping_List> shopping_list = new ArrayList<>();

    // Private and public list names
    private ArrayList<HashMap<String, String>> private_list = new ArrayList<>();
    private ArrayList<HashMap<String, String>> public_list = new ArrayList<>();
    HashMap<String, String> temp;

    //Columns
    private static final String FIRST_COLUMN = "First";
    private static final String SECOND_COLUMN = "Second";

    //Google API client
    private GoogleApiClient mGoogleApiClient;

    // Adapter
    MyRecyclerAdapter adapter;

    //User info
    private User_Info usr_inf;

    //Timer
    private CountDownTimer timer,timer2;

    // Selected private or public tab
    private boolean is_private_selected = true;
    private int selected_shopping_list = -1;

    //Database
    DB_Helper db;

    //GCM
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.activity_main);
        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START Bind service Update List]**/
        Intent intent = new Intent(this, Update_Android.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        /**[END Bind service Update List]**/

        /**[START Bind service Update Server]**/
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);
        /**[END Bind service Update Server]**/

        /**[START UI elements]**/
        private_lists = (Button) findViewById(R.id.private_lists);
        public_lists = (Button) findViewById(R.id.public_lists);
        separator1 = findViewById(R.id.separator);
        separator2 = findViewById(R.id.separator2);
        welcome = (TextView) findViewById(R.id.welcome_text);
        loading = (ProgressBar) findViewById(R.id.progressBar);
        first_layout = findViewById(R.id.firstLayout);
        third_layout = findViewById(R.id.thirdLayout);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        /**[END UI elements]**/

        /**[START DataBase]**/
        db = new DB_Helper(getApplicationContext());
        /**[END DataBase]**/

        /**[START List view]**/

        RecyclerView.LayoutManager mLayoutManager;
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        List<Shopping_List> lists = get_private_lists();
        adapter = new MyRecyclerAdapter(lists);
        mRecyclerView.setAdapter(adapter);
        //mRecyclerView.setAdapter(new MyRecyclerAdapter(shopping_list));
        /**[END List view]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            assert toolbar != null;
            toolbar.setElevation(25);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/

        /**[START AddList call]**/
        //Add new list
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddList.class);
                // Start next activity
                startActivityForResult(intent, 1);
            }
        });
        /**[END AddList call]**/

        /**[START GoogleApiClient]**/
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END GoogleApiClient]**/

        /**[START OnClickListeners]**/
        //Change to private or public view
        private_lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator1.getVisibility() != View.VISIBLE) reload_ui(true);

            }
        });
        public_lists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator2.getVisibility() != View.VISIBLE) reload_ui(false);
            }
        });

        adapter.setOnItemClickListener(new MyRecyclerAdapter.ClickListener() {
            @Override
            public void onItemClick(Shopping_List clicked_shopping_list, View v, int is_fav_checked) {
                print_sl();
                String shopping_list_code = clicked_shopping_list.getShopping_List_Code();
                String shopping_list_type = String.valueOf(clicked_shopping_list.is_Private() ? 1 : 0);
                Log.v(TAG,"Fav value: " + db.read_shopping_list(6,shopping_list_code));
                switch (is_fav_checked){
                    // Fav icon not changed
                    case -1:
                        if (usr_inf.getOffline_mode()) {
                            Intent in = new Intent(MainActivity.this, Items.class);
                            in.putExtra("Code", shopping_list_code);
                            in.putExtra("Type", shopping_list_type);
                            startActivity(in);
                        }
                        else{
                            selected_shopping_list = shopping_list.indexOf(clicked_shopping_list);
                            Message msg = Message.obtain(null, Update_Android.MSG_GET_DATA);
                            Bundle bundle = new Bundle();
                            bundle.putString("request", "one_list");
                            bundle.putString("Activity", "MainActivity");
                            bundle.putString("code_list", shopping_list_code);
                            bundle.putString("GoogleAccount", usr_inf.getEmail());
                            msg.setData(bundle);
                            try {
                                mService.send(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    // Fav icon changed to false
                    case 0:
                        Log.v(TAG,"Fav icon set to false");
                        Log.v(TAG,"Name: " + clicked_shopping_list.getList_Name());
                        Log.v(TAG,"SL code: " + shopping_list_code);
                        clicked_shopping_list.setFavourite(false);
                        db.update_favourite_value(false,shopping_list_code);
                        break;
                    // Fav icon changed to true
                    case 1:
                        Log.v(TAG,"Fav icon set to true");
                        Log.v(TAG,"Name: " + clicked_shopping_list.getList_Name());
                        Log.v(TAG,"SL code: " + shopping_list_code);
                        db.update_favourite_value(true,shopping_list_code);
                        clicked_shopping_list.setFavourite(true);
                        print_db();
                        break;
                }
                Log.v(TAG,"Fav value: " + db.read_shopping_list(6,shopping_list_code));
                // Swap old list with new list (ordered by the fav icons)
                read_from_internal_DB();
                inflate_UI(is_private_selected);
            }

            @Override
            public void onItemLongClick(int position, View v) {
                if (previous_selected_view != null)
                    previous_selected_view.setBackgroundColor(Color.WHITE);

                if (position == selected_shopping_list) {
                    v.setBackgroundColor(Color.WHITE);
                    selected_shopping_list = -1;
                }
                else {
                    v.setBackgroundColor(Color.CYAN);
                    selected_shopping_list = position;
                    previous_selected_view = v;
                }
            }
        });

        ItemTouchHelper.SimpleCallback simpleItemCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }
            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                View itemView = viewHolder.itemView;

                Paint p = new Paint();
                // Red intensity 100
                p.setARGB(200, 244, 67, 54);
                c.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                        (float) itemView.getRight(), (float) itemView.getBottom(), p);

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(final RecyclerView.ViewHolder viewHolder, int direction) {
                switch (direction){
                    case ItemTouchHelper.LEFT:
                        Log.v(TAG,"Shopping List swiped left");
                        boolean user_alert_result;
                        user_alert_result = delete_shoppingList(viewHolder.getAdapterPosition());
                        if (user_alert_result) {
                            shopping_list.remove(viewHolder.getAdapterPosition());
                            Log.v(TAG, shopping_list.toString());
                            adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        }
                        else viewHolder.itemView.setVisibility(View.VISIBLE);
                        reload_ui(is_private_selected);
                        break;
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
        /**[END OnClickListeners]**/

        /**[START User_Info]**/
        //Get User info
        usr_inf = User_Info.getInstance();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        usr_inf.setCurrency(sharedPref.getString("currency_list", Currency.getInstance(Locale.getDefault()).getSymbol()));
        Log.v(TAG,"Currency: " + usr_inf.getCurrency());
        /**[END User_Info]**/

        /** [START Advertisements] **/
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
        /** [END Advertisements] **/

        /**[START Counter]**/
        //Counter to reload the MainActivity every 2 minutes
        timer = new CountDownTimer(120000, 1000) { //2min
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                if (!usr_inf.getOffline_mode())
                    getAll_ShoppingLists(usr_inf.getEmail());
                start();
            }
        }.start();
        if (usr_inf.getOffline_mode()){
            // every minute
            Log.v(TAG,"Starting offline counter");
            timer2 = new CountDownTimer(30000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {}
                @Override
                public void onFinish() {
                    if (is_network_available()) {
                        Log.d(TAG,"Internet back");
                        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                        alert.setTitle(R.string.go_online_alert);
                        alert.setMessage(R.string.go_online_question);
                        alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog,int which){
                                Toast.makeText(MainActivity.this,R.string.shutting_down,Toast.LENGTH_SHORT).show();
                                Intent i = getBaseContext().getPackageManager()
                                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                finish();
                                startActivity(i);
                                System.exit(0);
                            }
                        });
                        alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which){
                                timer2.cancel();
                            }
                        });
                        alert.show();
                    }
                    else {
                        Log.d(TAG, "Internet back");
                        usr_inf.setOffline_mode(true);
                        // Start timer again
                        start();
                    }
                }
            }.start();
        }
        /**[END Counter]**/

        /**[START Google Notification Token Registration]**/
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
            }
        };
        // Registering BroadcastReceiver
        registerReceiver();
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent2 = new Intent(this, RegistrationIntentService.class);
            startService(intent2);
        }
        /**[END Google Notification Token Registration]**/

        /**[START Screen Orientation]**/
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        /**[END Screen Orientation]**/

        reload_ui(is_private_selected);
    }

    // This function is called when the app starts, after the onCreate method
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    // This function is called when the app stops
    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        if (timer2 != null) timer2.cancel();
    }

    // This function is called when the app "comes back"
    @Override
    protected void onResume() {
        super.onResume();
        //Restart timers
        timer.start();
        if (timer2 != null && usr_inf.getOffline_mode()) timer2.start();
        //Register receiver
        registerReceiver(receiver, filter);
        registerReceiver();
        db = new DB_Helper(getApplicationContext());
        //Get shopping lists
        if (!usr_inf.getOffline_mode())
            getAll_ShoppingLists(usr_inf.getEmail());
        else {
            read_from_internal_DB();
            reload_ui(is_private_selected);
        }
    }
    // Equivalent of onResume method
    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        isReceiverRegistered = false;
    }

    // Receiver registration
    private void registerReceiver(){
        if(!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    // This function is called when the app is closed or killed
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unbind from the services
        if (is_bound) {
            unbindService(mConnection);
            is_bound = false;
        }
        if (is_bound_server) {
            unbindService(mConnection2);
            is_bound_server = false;
        }
        // Unload DB from memory
        db.destroy_class();
        // Stops timers
        timer.cancel();
        if (timer2 != null) timer2.cancel();
        unregisterReceiver(receiver);
    }

    // Binding Update Android
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            mService = new Messenger(service);
            is_bound = true;
            //Get the shopping lists and displaying a loading progress circle
            new ProgressTask().execute();
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.v(TAG,"Update List disconnected");
            mService = null;
            is_bound = false;
        }
    };

    // Binding Update Server
    private ServiceConnection mConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.v(TAG, "Binding service");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Update_Server.LocalBinder binder = (Update_Server.LocalBinder) service;
            server_service = binder.getService();
            is_bound_server = true;
            // Synchronize internal DB entries with server entries
            if (!usr_inf.getOffline_mode())
                send_unsynced_entries();
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            is_bound_server = false;
        }
    };

    //Receiver from Services
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            request_type = intent.getStringExtra("Request");
            main = intent.getStringExtra("Main");
            Log.v(TAG,"Request: "+ request_type);
            //Check type of request
            switch(request_type){
                case "finish_activity":
                    finish();
                    break;
                // Get one Shopping List from the server
                case "one_list":
                    update_product = intent.getStringExtra("Update_Products");
                    if (!update_product.equals("True")) {
                        list = intent.getStringExtra("One_list");
                        Log.v(TAG, list);
                        changeActivity(main, list);
                    }
                    break;
                // Get all Shopping List from the server
                case "all":
                    Log.v(TAG,"Getting lists");
                    list = intent.getStringExtra("all");
                    update_Users_data(list);
                    break;
                // New Shopping List added in the server, returns the list code
                case "new_list":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this, R.string.add_list_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Log.v(TAG,"adding new code: " + main);
                        Log.v(TAG,"old_code: " + old_codes);
                        // Change the code from the internal DB for the code from the server
                        db.update_list_code(main,old_codes);
                        // Security measure
                        try {
                            db.delete_list(old_codes);
                        } catch (android.database.CursorIndexOutOfBoundsException e){
                            e.printStackTrace();
                        }
                        // Disable sync flag
                        db.set_list_flag(main,0);
                        print_db();
                        // Reload the UI
                        new ProgressTask_Back().execute();
                        reload_ui(is_private_selected);
                        Log.v(TAG, "Added new Shopping List correctly");
                    }
                    break;
                // Name changed for a Shopping List, return True if name changed correctly
                case "change_list_name":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this, R.string.change_list_name_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Toast.makeText(MainActivity.this, R.string.sl_name_change_ok, Toast.LENGTH_SHORT).show();
                        new ProgressTask_Back().execute();
                        Log.v(TAG, "Name changed correctly");
                    }
                    break;
                // Share a Shopping List
                case "add_usr_to_list":
                    if (main.equals("False")){
                        Toast.makeText(MainActivity.this, R.string.error_share_sl, Toast.LENGTH_SHORT).show();
                    } else{
                        Log.v(TAG,"Shared list");
                        Toast.makeText(MainActivity.this, R.string.share_sl_ok, Toast.LENGTH_SHORT).show();
                        new ProgressTask_Back().execute();
                    }
                    break;
                // Shopping List deleted
                case "delete_list":
                    if (main.equals("False"))
                        Toast.makeText(MainActivity.this,R.string.delete_list_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Toast.makeText(MainActivity.this, R.string.delete_sl_ok, Toast.LENGTH_SHORT).show();
                        new ProgressTask_Back().execute();
                    }
                    break;
                // Token for notifications added
                case "Token":
                    String token = intent.getStringExtra("Token");
                    send_request_server("_", "_", "add_token", "_", token);
                    Log.v(TAG, "TOKEN: "+token);
                    break;
            }
        }
    }

    // This method is called when the back button is pressed
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (previous_selected_view != null) {
            ColorDrawable bg_color = (ColorDrawable) previous_selected_view.getBackground();
            if (bg_color.getColor() == Color.CYAN) {
                previous_selected_view.setBackgroundColor(Color.WHITE);
                selected_shopping_list = -1;
            }
        }
        // If the navigation drawer is open, close it
        else if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);

        else {
            // Else, finish the activity
            finish();
            super.onBackPressed();
        }

    }

    // Creates the options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    // This method is called when an option from the menu is clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // If the update button was clicked..
            case R.id.action_update:
                // Check if we are offline or online and update the shopping lists from the server
                // or from the internal DB
                if (!usr_inf.getOffline_mode()) {
                    Log.v(TAG,"update online");
                    Log.v(TAG,"" + usr_inf.getOffline_mode());
                    usr_inf.setOffline_mode(false);
                    getAll_ShoppingLists(usr_inf.getEmail());
                    Toast.makeText(MainActivity.this,R.string.update,Toast.LENGTH_SHORT).show();
                } else {
                    Log.v(TAG,"update offline");
                    Toast.makeText(getBaseContext(), R.string.offline_update, Toast.LENGTH_SHORT).show();
                    Toast.makeText(getBaseContext(),R.string.offline_update_warning,Toast.LENGTH_SHORT).show();
                    read_from_internal_DB();
                    reload_ui(is_private_selected);
                }
                return true;

            case R.id.share:
                if (!usr_inf.getOffline_mode() && selected_shopping_list != -1) {
                    share_shoppingList(shopping_list.get(selected_shopping_list));
                }
                else if (usr_inf.getOffline_mode()){
                    Log.v(TAG,"update offline");
                    Toast.makeText(getBaseContext(), R.string.offline_share, Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getBaseContext(), R.string.select_element, Toast.LENGTH_SHORT).show();
                return true;

            case R.id.edit:
                if (selected_shopping_list != -1)
                    edit_shoppingList(shopping_list.get(selected_shopping_list));
                else
                    Toast.makeText(getBaseContext(), R.string.select_element, Toast.LENGTH_SHORT).show();
                return true;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    // Navigation drawer listener, is called when an item from the navigation drawer is clicked
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        // Supermarkets Item selected
        /*if (id == R.id.nav_locations) {
            Intent intent = new Intent(MainActivity.this,MapsActivity.class);
            // Start next activity
            startActivity(intent);
        }*/
        // Settings Item selected
        if (id == R.id.nav_manage) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            // Start next activity
            startActivity(intent);
        }
        // Share App Item selected
        else if (id == R.id.nav_share) {
            Log.d(TAG,"Sharing shopping");
            // Create a template for an email and let the user choose which mail client to use
            Intent mail_intent = new Intent(Intent.ACTION_SEND);
            mail_intent.setType("message/rfc822");
            // Body of mail
            mail_intent.putExtra(Intent.EXTRA_SUBJECT,"Try Need More Cookies!");
            mail_intent.putExtra(Intent.EXTRA_TEXT,"I invite you to try this awesome app! You will be able to write and share shopping lists " +
                    "with your friends! \nDownload it here: test.com \nYour friend: " + usr_inf.getName());
            Intent final_intent = Intent.createChooser(mail_intent,"Choose mail client");
            final_intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // Start Mail chooser
            startActivity(final_intent);
        }
        // Logout from the system
        else if (id == R.id.nav_logout){
            signOut();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Sign Out from Google Account
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(MainActivity.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

    // Get result from AddList activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Request code is always 1 if it comes from the AddList activity
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // Get the name of the list and the Type (Private or Public)
                String list_name = data.getStringExtra("List_Name");
                String Type = data.getStringExtra("Type");
                //Check which type of list the user wants to add
                switch (Type){
                    // Private
                    case "true":
                        // If we are in offline mode, add the new list to the internal DB
                        if (usr_inf.getOffline_mode())
                            old_codes =db.add_new_list(list_name,1);
                        Log.v(TAG,"name: " + list_name);
                        // Else, send a request to the server to add the new list
                        if (!usr_inf.getOffline_mode())
                            send_request_server(list_name, "1", "new_list", "", "");
                        reload_ui(true);
                        break;
                    // Public
                    case "false":
                        if (usr_inf.getOffline_mode())
                            old_codes = db.add_new_list(list_name,0);
                        reload_ui(false);
                        if (!usr_inf.getOffline_mode())
                            send_request_server(list_name, "0", "new_list","", "");
                        break;
                }
                // If we are in offline mode, reload de UI with the new Shopping List
                if (usr_inf.getOffline_mode()){
                    read_from_internal_DB();
                }
            }
        }
    }

    // Send request to Update Server service
    private void send_request_server(String list_name,String is_private, final String Objective, String code, String set_value){
        Log.v(TAG,"objective: " + server_service.get_objective(Objective));
        // Set the values for the JSON object
        server_service.set_values(server_service.get_objective(Objective), code, list_name, "True", is_private);
        server_service.set_items("_", "_", set_value, "_", "_");
        // Create a new Thread to send and wait for a response
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Send the request
                server_service.send_request();
                // Wait for the server to give an answer back
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status());
                // Read response
                String response = server_service.return_result();
                Log.v("Thread", response);
                // Give the response to the UI Thread to process
                Intent intent = new Intent();
                intent.setAction("broadcast_service");
                intent.putExtra("Main", response);
                intent.putExtra("Request", Objective);
                sendBroadcast(intent);
            }
        });
        t.start();
    }

    //Change the UI either private or public shopping lists
    private void reload_ui(Boolean type){
        if (type){
            is_private_selected = true;
            separator1.setVisibility(View.VISIBLE);
            separator2.setVisibility(View.INVISIBLE);
        }
        else {
            is_private_selected = false;
            separator2.setVisibility(View.VISIBLE);
            separator1.setVisibility(View.INVISIBLE);
        }
        inflate_UI(type);
    }

    // TODO : FIX
    //When a shopping lists is pressed, change to Items activity and send the items
    private void changeActivity(String main, String list){
        Log.v(TAG,main);
        Intent intent = new Intent(this, Items.class);
        intent.putExtra("Main", main);
        intent.putExtra("List", list);
        Log.v(TAG,list);
        Log.v(TAG,main);
        intent.putExtra("Type", "" + ((shopping_list.get(selected_shopping_list).is_Private()) ? 1 : 0));
        startActivity(intent);
    }

    // Send a request to the server to give us all the Shopping Lists that belong to a user.
    private void getAll_ShoppingLists(String GoogleAccount){
        if (is_bound) {
            // Create and send a message to the service, using a supported 'what' value
            Message msg = Message.obtain(null, Update_Android.MSG_GET_DATA);
            Bundle bundle = new Bundle();
            bundle.putString("request", "all");
            bundle.putString("GoogleAccount", GoogleAccount);
            msg.setData(bundle);
            //Send message
            try {
                mService.send(msg);
                Log.v(TAG, "Message sent");
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // Update the UI with all the shopping lists
    private void update_Users_data(String result){
        try {
            boolean name_change_flag = false;
            Log.v(TAG,"UPDATE_USR");
            JSONObject json_obj = new JSONObject(result);
            Iterator<String> keys = json_obj.keys();
            List<String> c = new ArrayList<>();
            // Get the Shopping Lists
            print_db();
            while (keys.hasNext()) {
                //Get list name
                String list_name = String.valueOf(keys.next());
                JSONObject list1 = json_obj.getJSONObject(list_name);
                //Get the Shopping List code
                String code = list1.getString("Code");
                //Get timestamp
                String timestamp = list1.getString("Timestamp");
                int type = list1.getInt("TypeList");
                //Check type of shopping list and store them in the User_Info class
                //The format is the following: [[List_name, Type, Code, Timestamp], [List_name2, Type, Code, Timestamp],...]
                c.add(code);
                Log.v(TAG,"List name: " + list_name);
                switch (type) {
                    case 0:
                       // If the Shopping Lists code is not in the internal DB, add it.
                        if (db.read_shopping_list(0,code).equals("Error")) {
                            Log.v(TAG,"Item not found. Adding to DB");
                            Log.v(TAG,"List name: " + list_name);
                            String old_code = db.add_new_list(list_name, 0);
                            db.set_list_flag(code,0);
                            db.update_list_code(code,old_code);
                        }
                        break;
                    case 1:
                        if (db.read_shopping_list(0,code).equals("Error")) {
                            Log.v(TAG,"Item not found. Adding to DB");
                            Log.v(TAG,"List name: " + list_name);
                            String old_code = db.add_new_list(list_name, 1);
                            db.set_list_flag(code,0);
                            db.update_list_code(code,old_code);
                        }
                        break;
                }
                // If the code is there but the list name is not the same from the server
                // update the lists name in the internal DB
                if (!db.read_shopping_list(1,code).equals(list_name)) {
                    db.update_list_name(list_name, code);
                    name_change_flag = true;
                }
                // Update the timestamp with the timestamp from the server
                db.update_timestamp_server(code, timestamp);
                // Set the sync flag to 0
                db.set_list_flag(code,0);
            }
            // If the name has changed show a small message
            if (name_change_flag)
                Toast.makeText(MainActivity.this,R.string.external_name_change,Toast.LENGTH_SHORT).show();
            // Removes the lists from the internal DB that have been erased from the server.
            remove_deleted_SL(c);
            print_db();
        } catch (JSONException e){
            e.printStackTrace();
        }
        // Since the DB is as updated as the server, read entries from the DB and display them in the UI
        read_from_internal_DB();
        inflate_UI(is_private_selected);
    }

    // This function reads the entries from the internal DB and displays them in the UI
    private void read_from_internal_DB(){
        Log.d(TAG,"Reading from internal DB");
        shopping_list.clear();
        print_db();
        // Read entries from the DB
        List<String[]> a = db.read_all_lists();
        if (a!=null) {
            // Go through them
            print_db();
            for (int i = 0; i < a.size(); i++) {
                String[] b = a.get(i);
                shopping_list.add(new Shopping_List(b[0],b[1],"",b[4].equals("1"),a.get(i)[3].equals("1"),b[2]));
            }
        }
    }

    // Inflates UI
    private void inflate_UI(boolean is_private){
        List<Shopping_List> lists;
        if (is_private) lists = get_private_lists();
        else lists = get_public_lists();
        adapter.swap(lists);
        Log.v(TAG,"Inflating UI");
    }

    // Removes unused lists.
    private void remove_deleted_SL(List<String> c){
        int i;
        boolean deleted_flag = false;
        Log.v(TAG,"Removing lists");
        List<String[]> internal_db_lists;
        // Codes from the internal DB
        List<String> internal_codes = new ArrayList<>();
        // Grab all entries from DB
        internal_db_lists = db.read_all_lists();
        // Add all codes from entries into a list
        for (i = 0;i < internal_db_lists.size();i++)
            internal_codes.add(internal_db_lists.get(i)[2]);
        // If one of the received codes is inside the DB remove it form the list
        for (i = 0;i < c.size(); i++){
            // Compare the codes from the internal DB with the ones from the server
            if (internal_codes.contains(c.get(i))) {
                // If the code is in the internal DB delete it from the list
                internal_codes.remove(c.get(i));
            }
        }
        // In case there are still codes in the DB that are not on the server remove them from the DB
        if (internal_codes.size() > 0){
            for (i = 0;i < internal_codes.size(); i++){
                deleted_flag = true;
                Log.d(TAG,"Removing lists: " + internal_codes.get(i));
                db.delete_list(internal_codes.get(i));
            }
        }
        // If an item was deleted show a message saying so
        if (deleted_flag)
            Toast.makeText(MainActivity.this,R.string.external_delete,Toast.LENGTH_SHORT).show();
    }

    // This class allows to create an synchronous task (it runs in its own thread)
    // that we will use to load the Shopping Lists in the UI as well as show a loading spinner
    class ProgressTask_Back extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            // Clear the lists and set the loading spinner visible and the UI invisible
            loading.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            // This gets executed in its own thread
            if (!usr_inf.getOffline_mode()) {
                // Get the shopping lists from the server
                getAll_ShoppingLists(usr_inf.getEmail());
            }
            else {
                // Or read them from the internal DB
                read_from_internal_DB();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            // Set the loading spinner invisible
            loading.setVisibility(View.GONE);
            // Make the UI visible again with a fade in animation
            final Animation fadein = new AlphaAnimation(0, 1);
            fadein.setDuration(1000);
        }
    }

    // This class is similar to the previous one, but this one gets called when the app first starts
    class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            Log.v(TAG,"STARTING EXECUTION OF APP");
            loading.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mAdView.setVisibility(View.GONE);

        }
        @Override
        protected Void doInBackground(Void... arg0) {
            if (!usr_inf.getOffline_mode())
                getAll_ShoppingLists(usr_inf.getEmail());
            else
                read_from_internal_DB();

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            loading.setVisibility(View.GONE);
            final Animation fadein = new AlphaAnimation(0,1);
            fadein.setDuration(1000);
            Animation fadeOut = new AlphaAnimation(1, 0);
            fadeOut.setStartOffset(2000);
            fadeOut.setDuration(1000);
            // Show the Welcome back text
            if (usr_inf.getOffline_mode()) welcome.setText(R.string.offline_explanation);
            else welcome.setText(getResources().getString(R.string.welcome_display) + usr_inf.getName() + "!");
            welcome.setVisibility(View.VISIBLE);
            welcome.setAnimation(fadein);
            welcome.setAnimation(fadeOut);
            reload_ui(is_private_selected);
            // Make the welcome text fade out and the UI fade in
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }
                @Override
                public void onAnimationEnd(Animation animation) {
                    welcome.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                    mRecyclerView.setAnimation(fadein);
                    first_layout.setVisibility(View.VISIBLE);
                    third_layout.setVisibility(View.VISIBLE);
                    first_layout.setAnimation(fadein);
                    third_layout.setAnimation(fadein);
                    mAdView.setVisibility(View.VISIBLE);
                    mAdView.setAnimation(fadein);
                    // Add the user to the DB, this usually will return false unless is the users
                    // first time ever loging in
                    if (!usr_inf.getOffline_mode()) send_request_server("_", "_", "add_user", "_", usr_inf.getName());
                }
                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
        }
    }



    // This function is called when the user wants to share a Shopping List
    private void share_shoppingList(final Shopping_List share_shopping){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // Create an alert asking the user to write the email of the user he want to share it with
        alert.setTitle(R.string.share_list_msg);
        alert.setMessage(R.string.write_email_msg);

        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
        alert.setView(input);
        // When the user click the Done button this function is called
        alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Validate the input as an email
                Boolean valid = validateEmail(input.getText().toString());
                String tmp_code,list_name,list_type;
                if (!valid) {
                    Context context = getApplicationContext();
                    CharSequence error = String.valueOf(R.string.email_not_valid_msg);
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, error, duration);
                    toast.show();
                    share_shoppingList(share_shopping);
                } else {
                    // Send a request to the server to add the new user to the selected list
                    if (is_bound_server) {
                        tmp_code = share_shopping.getShopping_List_Code();
                        list_name = share_shopping.getList_Name();
                        list_type = String.valueOf((shopping_list.get(selected_shopping_list).is_Private()) ? 1 : 0);
                        db.update_list_public(0,tmp_code);
                        send_request_server(list_name, list_type,
                                "add_usr_to_list", tmp_code, input.getText().toString());
                    }
                }
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    // This function verifies that the input string has the format of a email account
    private boolean validateEmail(String email) {
        Pattern pattern;
        Matcher matcher;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        pattern = Pattern.compile(EMAIL_PATTERN);
        matcher = pattern.matcher(email);
        return matcher.matches();
    }

    //This function deletes a Shopping List
    private boolean delete_shoppingList(final int item_position){
        Log.v(TAG,"Deleting shopping List");
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // WTF
        final boolean[] alert_result = new boolean[1];
        // Ask the user if he wants to delete the list
        alert.setTitle(R.string.delete_list_alert);
        alert.setMessage(R.string.delete_list_msg);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String code_list,is_list_private,list_name;
                alert_result[0] = true;
                // Get the name of the shopping list
                Shopping_List to_delete_list = shopping_list.get(item_position);
                // Get the code for the shopping list
                code_list = to_delete_list.getShopping_List_Code();
                list_name = to_delete_list.getList_Name();
                Log.v(TAG,"Deleting List with name: " + list_name);
                is_list_private = String.valueOf(to_delete_list.is_Private());
                if (!usr_inf.getOffline_mode()) {
                    // Delete the Shopping List
                    send_request_server(list_name, is_list_private, "delete_list", code_list, "_");
                    Log.v(TAG,"code_list: " + code_list);
                    print_db();
                    db.delete_list(code_list);
                }
                else {
                    // Fake delete until synchronize
                    db.update_list_change("delete_list",code_list);
                    read_from_internal_DB();
                    reload_ui(is_private_selected);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                alert_result[0] = false;
            }
        });
        alert.show();
        return alert_result[0];

    }

    // This method changes the name of a Shopping List
    private void edit_shoppingList(final Shopping_List edit_shopping_list){
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);
        // Ask the user to input the new name
        alert.setTitle(R.string.change_name_list);
        alert.setMessage(R.string.set_new_name_msg);
        // Set an EditText view to get user input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (is_bound) {
                    String code,list_type;
                    String list_name = edit_shopping_list.getList_Name();
                    code = edit_shopping_list.getShopping_List_Code();
                    // Change the name of the list in the internal DB
                    db.update_list_name(input.getText().toString(),code);
                    print_db();
                    if (!usr_inf.getOffline_mode()) {
                        db.set_list_flag(code, 0);
                        Log.v(TAG,"Changing list name");
                        list_type = String.valueOf((shopping_list.get(selected_shopping_list).is_Private()) ? 1 : 0);
                        // Send a request to change the name to the server
                        send_request_server(list_name, list_type, "change_list_name", code, input.getText().toString());
                    }
                    else {
                        // Set the sync flag to 1 if in offline mode
                        db.set_list_flag(code, 1);
                        public_list.clear();
                        private_list.clear();
                        adapter.notifyDataSetChanged();
                        read_from_internal_DB();
                        reload_ui(is_private_selected);
                    }
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        alert.show();
    }

    // Send all the unsynchronized entries from the DB to the server.
    // This method gets called when the app first starts
    private boolean send_unsynced_entries(){
        Log.d(TAG,"We are online");
        Log.d(TAG,"Synchronizing entries from DB");
        // Get all items with sync flag set
        List<String[]> entries = db.read_all_with_flag_set_list();
        print_db();
        // If there are no entries to synchronize we are done
        if (entries == null) return true;
        String entry[];
        // Go through all the entries
        for (int i = 0; i< entries.size(); i++){
            entry = entries.get(i);
            Log.d(TAG,"entry: " + Arrays.toString(entry));
            // Set the sync flag to 0
            db.set_list_flag(entry[1],0);
            // If the Chane Type field is "new_list", save the old code because we are going to
            // need to change it when we get the new one from the server
            if (entry[3].equals("new_list")) old_codes = entry[1];
            // Send a request to the server to change the name if the Change Type field was "change_list_name"
            if (entry[3].equals("change_list_name"))
                send_request_server("_",entry[2],entry[3],entry[1],entry[0]);
            // If its not one of the others, it means its delete the Shopping List.
            else
                send_request_server(entry[0],entry[2],entry[3],entry[1],"_");
            // Delete list really.
            if (entry[2].equals("delete_list")) db.delete_list(entry[1]);
            // wait
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        print_db();
        return true;
    }

    // Prints the contents from the Shopping List table from the internal DB
    private void print_db(){
        List<String[]> entries = db.read_all_lists();
        Log.v(TAG,"STARTING THE PRINT DB");
        for (int i=0; i<entries.size();i++)
            Log.v(TAG,"Entries: " + entries.get(i)[0] +" " + entries.get(i)[2] +" " + entries.get(i)[3]
            + " " + entries.get(i)[4]);
        Log.v(TAG,"END");
    }

    private List<Shopping_List> get_private_lists(){

        int i;
        Shopping_List current_list;
        List<Shopping_List> private_shopping_lists = new ArrayList<>();
        for (i = 0; i < shopping_list.size(); i++){
            current_list = shopping_list.get(i);
            Log.v(TAG,"List name: " + current_list.getList_Name());
            Log.v(TAG,"Priv fav: " + current_list.isFavourite());
            if (current_list.is_Private()){
                if (current_list.isFavourite())
                    private_shopping_lists.add(0,current_list);
                else
                    private_shopping_lists.add(current_list);
            }
        }
        print_sl();
        return private_shopping_lists;
    }

    private List<Shopping_List> get_public_lists(){

        int i;
        Shopping_List current_list;
        List<Shopping_List> public_shopping_lists = new ArrayList<>();
        for (i = 0; i < shopping_list.size(); i++){
            current_list = shopping_list.get(i);
            if (!current_list.is_Private()) {
                if (current_list.isFavourite())
                    public_shopping_lists.add(0,current_list);
                else
                    public_shopping_lists.add(current_list);
            }
        }
        return public_shopping_lists;
    }

    void print_sl(){
        for (int i = 0; i < shopping_list.size(); i++)
            Log.v("Shopping","Main, Name: " + shopping_list.get(i).getList_Name() + " Code: " + shopping_list.get(i).getShopping_List_Code());
    }
    // Checks if there is network connection available
    /** http://developer.android.com/training/monitoring-device-state/connectivity-monitoring.html **/
    private boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
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
}
