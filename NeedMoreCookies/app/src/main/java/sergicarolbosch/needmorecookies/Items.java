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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * This class shows the products from a concrete Shopping List, as well as their quantity, price and type.
 */

public class Items extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    //TAG for Logs
    private final String TAG = "Items_Activity: ";

    //GoogleApiClient
    private GoogleApiClient mGoogleApiClient;


    //UI elements
    private Button all_items;
    private Button meat_items;
    private Button vegetables_items;
    private Button cereals_items;
    private Button dairy_items;
    private Button sweet_items;
    private Button others_items;
    private View separator1;
    private View separator2;
    private View separator3;
    private View separator4;
    private View separator5;
    private View separator6;
    private View separator7;

    private ProgressBar loading;

    private CountDownTimer timer;
    private CountDownTimer timer2;

    /**
     * [START ListView]
     **/
    //Header
    private ListViewAdapters adapter_header;
    private ArrayList<HashMap<String, String>> l_header = new ArrayList<>();
    //Content
    private ListView listview_items;
    // Lists
    private ArrayList<HashMap<String, String>> all_items_l = new ArrayList<>();
    private ArrayList<HashMap<String, String>> meat_items_l = new ArrayList<>();
    private ArrayList<HashMap<String, String>> vegetables_items_l = new ArrayList<>();
    private ArrayList<HashMap<String, String>> cereals_items_l = new ArrayList<>();
    private ArrayList<HashMap<String, String>> dairy_items_l = new ArrayList<>();
    private ArrayList<HashMap<String, String>> sweet_items_l = new ArrayList<>();
    private ArrayList<HashMap<String, String>> others_items_l = new ArrayList<>();

    // Adapter
    private ListViewAdapters adapter;

    //Columns
    private static final String FIRST_COLUMN = "First";
    private static final String SECOND_COLUMN = "Second";
    private static final String THIRD_COLUMN = "Third";
    private static final String FOURTH_COLUMN="Fourth";

    //Temporal HashMap to write to the columns
    private HashMap<String, String> temp;
    /**
     * [END ListView]
     **/
    //Preferences
    private String currency;

    // Service
    private Update_Server server_service;
    private boolean is_bound_server = false;
    private boolean is_bound = false;
    private Messenger mService = null;

    //Receiver
    public MyReceiver receiver_items;
    private IntentFilter filter;

    //Database
    DB_Helper db;
    private String old_codes;

    // Info
    private String main = null;
    private String code;
    private String list;
    private String list_type;
    private int current_tab = 1;

    //Selected Item
    private int currentSelection;

    //User info instance
    private User_Info usr_inf;

    /**
     * Override onCreate method
     * @param savedInstanceState Saved instance state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_items);

        /**[START DataBase]**/
        db = new DB_Helper(getApplicationContext());
        /**[END DataBase]**/

        /**[START UI elements]**/
        all_items = (Button) findViewById(R.id.all);
        meat_items = (Button) findViewById(R.id.meat);
        vegetables_items = (Button) findViewById(R.id.vegetables);
        cereals_items = (Button) findViewById(R.id.cereals);
        dairy_items = (Button) findViewById(R.id.dairy);
        sweet_items = (Button) findViewById(R.id.sweet);
        others_items = (Button) findViewById(R.id.others);
        loading = (ProgressBar) findViewById(R.id.progressBar2);

        separator1 = findViewById(R.id.separator_items);
        separator2 = findViewById(R.id.separator2_items);
        separator3 = findViewById(R.id.separator3_items);
        separator4 = findViewById(R.id.separator4_items);
        separator5 = findViewById(R.id.separator5_items);
        separator6 = findViewById(R.id.separator6_items);
        separator7 = findViewById(R.id.separator7_items);

        listview_items = (ListView) findViewById(R.id.list_item);

        ListView listview_header = (ListView) findViewById(R.id.list_header);
        /**[END UI elements]**/

        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        receiver_items = new MyReceiver();
        this.registerReceiver(receiver_items, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START List View]**/
        //Custom adapter
        adapter_header = new ListViewAdapters(this, l_header, "Header", "1");
        listview_header.setAdapter(adapter_header);
        adapter = new ListViewAdapters(this, all_items_l, "Content", list_type);
        listview_items.setAdapter(adapter);

        temp = new HashMap<>();
        temp.put(FIRST_COLUMN, getResources().getString(R.string.product_string));
        temp.put(SECOND_COLUMN, getResources().getString(R.string.quantity_string));
        temp.put(THIRD_COLUMN, getResources().getString(R.string.price_string));
        l_header.add(temp);
        /**[END List View]**/

        /**[START Preferences]**/
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Get currency User's preference
        currency = prefs.getString("currency_list", "€");
        /**[END Preferences]**/

        /**[START Navigation]**/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(0);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        assert drawer != null;
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view2);
        assert navigationView != null;
        navigationView.setNavigationItemSelectedListener(this);
        /**[END Navigation]**/

        /**[START AddItem activity]**/
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab2);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Items.this, AddItem.class);
                intent.putExtra("Edit", "False");
                // Start next activity
                startActivityForResult(intent, 1);
            }
        });
        /**[END AddItem activity]**/

        /**[START User_Info]**/
        usr_inf = User_Info.getInstance();
        /**[END User_Info]**/

        /**[START Get intent extras]**/
        if (!usr_inf.getOffline_mode()) {
            Bundle extras = getIntent().getExtras();
            //Get JSON Strings from the MainActivity
            try {
                main = extras.getString("Main");
                list = extras.getString("List");
                list_type = extras.getString("Type");
                Log.v(TAG,"Main: " + main);
                Log.v(TAG,"List: " + list);
                Log.v(TAG,"Type: " + list_type);
                try {
                    JSONObject rsp = new JSONObject(main);
                    code = rsp.getString("Code");

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else{
            Bundle extras = getIntent().getExtras();
            try {
                code = extras.getString("Code");
                list_type = extras.getString("Type");
                Log.d(TAG, "CODE LIST: "+code);
                List<String[]> list_items = db.read_all_items(code);
                Log.d(TAG, "ITEMS: " + list_items);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
        /**[END Get intent extras]**/

        /**[START Service binding]**/
        Intent intent = new Intent(this, Update_Android.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);
        /**[END Service binding]**/

        /**[START GoogleApiClient]**/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        /**[END GoogleApiClient]**/


        /**[START onClickListeners]**/
        all_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator1.getVisibility() != View.VISIBLE) {
                    current_tab = 1;
                    reload_ui(1);
                }
            }
        });
        meat_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator2.getVisibility() != View.VISIBLE) {
                    current_tab = 2;
                    reload_ui(2);
                }
            }
        });
        vegetables_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator3.getVisibility() != View.VISIBLE) {
                    current_tab = 3;
                    reload_ui(3);
                }
            }
        });
        cereals_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator4.getVisibility() != View.VISIBLE) {
                    current_tab = 4;
                    reload_ui(4);
                }
            }
        });
        dairy_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator5.getVisibility() != View.VISIBLE) {
                    current_tab = 5;
                    reload_ui(5);
                }
            }
        });
        sweet_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator6.getVisibility() != View.VISIBLE) {
                    current_tab = 6;
                    reload_ui(6);
                }
            }
        });
        others_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (separator7.getVisibility() != View.VISIBLE) {
                    current_tab = 7;
                    reload_ui(7);
                }
            }
        });

        /**[END onClickListeners]**/

        //Set portrait for phones and landscape for tablets
        if (!isXLargeTablet(getApplicationContext())){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        /**[START Counter]**/
        //Counter to reload the activity every 2 minutes
        timer = new CountDownTimer(120000, 1000) { //2min
            public void onTick(long millisUntilFinished) {}
            public void onFinish() {
                //Log.d(TAG, "timer");
                if (!usr_inf.getOffline_mode())
                    getAll_products();
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
                        final AlertDialog.Builder alert = new AlertDialog.Builder(Items.this);
                        alert.setTitle(R.string.go_online_alert);
                        alert.setMessage(R.string.go_online_question);
                        alert.setPositiveButton(android.R.string.yes,new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog,int which){
                                Toast.makeText(Items.this,R.string.shutting_down,Toast.LENGTH_SHORT).show();
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
                            public void onClick(DialogInterface dialog, int which) {
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

        //Reload UI to all products view
        reload_ui(1);
    }


    /**
     * Binding Update Android
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "Binding service");
            mService = new Messenger(service);
            is_bound = true;

            //Execute asynchronous task
            new ProgressTask().execute();

        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG,"Update List disconnected");
            mService = null;
            is_bound = false;
        }
    };

    /**
     * Binding Update Server
     */
    private ServiceConnection mConnection2 = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "Binding service");
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Update_Server.LocalBinder binder = (Update_Server.LocalBinder) service;
            server_service = binder.getService();
            is_bound_server = true;
            //If we are online, synchronize the server with the internal database
            if (!usr_inf.getOffline_mode())
                send_unsynced_entries();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            is_bound_server = false;
        }
    };

    /**
     * Override onDestroy method
     */
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
        if (timer2 != null) timer2.cancel();
        timer.cancel();
        unregisterReceiver(receiver_items);
    }

    /**
     * Override onResume method
     */
    @Override
    protected void onResume() {
        super.onResume();
        //Register receiver
        registerReceiver(receiver_items, filter);
        timer.start();
        if (usr_inf.getOffline_mode() && timer2!=null) timer2.start();
        if (!usr_inf.getOffline_mode())
            getAll_products();
        reload_ui(1);
    }

    /**
     * Override onCreateOptionsMenu method
     * @param menu Menu
     * @return Menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_item_activity, menu);
        return true;
    }

    /**
     * Override onOptionsItemsSelected method
     * @param item MenuItem
     * @return Return true
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_update_items:
                //If we are online, get products from server, else get from the internal database
                if (!usr_inf.getOffline_mode()) {
                    usr_inf.setOffline_mode(false);
                    getAll_products();
                    Toast.makeText(Items.this, R.string.update_products, Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getBaseContext(), R.string.offline_update, Toast.LENGTH_SHORT).show();
                    all_items_l.clear();
                    meat_items_l.clear();
                    vegetables_items_l.clear();
                    cereals_items_l.clear();
                    dairy_items_l.clear();
                    sweet_items_l.clear();
                    others_items_l.clear();
                    read_from_internal_DB();
                    adapter.notifyDataSetChanged();
                    reload_ui(1);
                }
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * Override onStart method
     */
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /**
     * Override onStop method
     */
    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
    }

    /**
     * Override onBackPressed method.
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        assert drawer != null;
        //If the navigation menu is opened, closed it
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Override onNavigationItemSelected method
     * @param item Item
     * @return Return true
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_locations) {
            Intent intent = new Intent(Items.this, MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_home) {
            Intent intent = new Intent(Items.this, MainActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else if (id == R.id.nav_manage) {
            Intent intent = new Intent(Items.this, SettingsActivity.class);
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

        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent();
            intent.putExtra("Request","finish_activity");
            intent.setAction("broadcast_service");
            sendBroadcast(intent);
            signOut();
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout2);
        assert drawer != null;
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Sign Out from Google Account
     */
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                Intent intent = new Intent(Items.this, Login.class);
                // Start next activity
                startActivity(intent);
                finish();
            }
        });
    }

    /**
     * Reload UI
     * @param type Type of products to show
     */
    private void reload_ui(int type) {
        separator1.setVisibility(View.INVISIBLE);
        separator2.setVisibility(View.INVISIBLE);
        separator3.setVisibility(View.INVISIBLE);
        separator4.setVisibility(View.INVISIBLE);
        separator5.setVisibility(View.INVISIBLE);
        separator6.setVisibility(View.INVISIBLE);
        separator7.setVisibility(View.INVISIBLE);
        if (type == 1) {
            adapter = new ListViewAdapters(this, all_items_l, "Content", list_type);
            separator1.setVisibility(View.VISIBLE);

        } else if (type == 2) {
            adapter = new ListViewAdapters(this, meat_items_l, "Content", list_type);
            separator2.setVisibility(View.VISIBLE);

        } else if (type == 3) {
            adapter = new ListViewAdapters(this, vegetables_items_l, "Content", list_type);
            separator3.setVisibility(View.VISIBLE);

        } else if (type == 4) {
            adapter = new ListViewAdapters(this, cereals_items_l, "Content", list_type);
            separator4.setVisibility(View.VISIBLE);

        } else if (type == 5) {
            adapter = new ListViewAdapters(this, dairy_items_l, "Content", list_type);
            separator5.setVisibility(View.VISIBLE);

        } else if (type == 6) {
            adapter = new ListViewAdapters(this, sweet_items_l, "Content", list_type);
            separator6.setVisibility(View.VISIBLE);

        } else if (type == 7) {
            adapter = new ListViewAdapters(this, others_items_l, "Content", list_type);
            separator7.setVisibility(View.VISIBLE);

        }
        //Set adapter
        listview_items.setAdapter(adapter);
    }

    /**
     * Add products to the internal database
     * @param list List of products
     */
    private void update_ShoppingList(String list) {
        try {
            int i = 0;
            JSONObject json_obj = new JSONObject(list);
            Iterator<String> keys = json_obj.keys();
            Log.d(TAG, "Deleting all items of one list");
            print_db();
            db.delete_all_items_of_one_list(code);
            print_db();
            while (keys.hasNext()) {
                String type = String.valueOf(keys.next());
                switch (type) {
                    case "Meat and Fish":
                        Log.v(TAG,"Add item meat");
                        Log.v(TAG,json_obj.toString());
                        JSONArray products2 = json_obj.getJSONArray(type);
                        while (i < products2.length()) {
                            JSONArray rec = products2.getJSONArray(i);
                            if (db.read_item(0,rec.getString(3)).equals("Error")) {
                                Log.d(TAG, "Item not found. Adding to DB");
                                Log.d(TAG, "Product: " + rec.getString(0));
                                String old_code = db.add_new_item(rec.getString(0), type, rec.getString(1), rec.getString(2), code, rec.getString(4));
                                db.set_item_flag(rec.getString(3), 0);
                                db.update_item_itemcode(rec.getString(3), old_code);
                            }
                            i++;
                        }
                        i = 0;
                        break;
                    case "Vegetables":
                        JSONArray products3 = json_obj.getJSONArray(type);
                        while (i < products3.length()) {
                            JSONArray rec = products3.getJSONArray(i);
                            if (db.read_item(0,rec.getString(3)).equals("Error")) {
                                Log.d(TAG,"Item not found. Adding to DB");
                                Log.d(TAG,"Product: " + rec.getString(0));
                                String old_code = db.add_new_item(rec.getString(0),type,rec.getString(1),rec.getString(2),code,rec.getString(4));
                                db.set_item_flag(rec.getString(3),0);
                                db.update_item_itemcode(rec.getString(3),old_code);
                            }
                            i++;
                        }
                        i = 0;
                        break;
                    case "Cereal":
                        JSONArray products4 = json_obj.getJSONArray(type);
                        while (i < products4.length()) {
                            JSONArray rec = products4.getJSONArray(i);
                            if (db.read_item(0,rec.getString(3)).equals("Error")) {
                                Log.d(TAG,"Item not found. Adding to DB");
                                Log.d(TAG,"Product: " + rec.getString(0));
                                String old_code = db.add_new_item(rec.getString(0),type,rec.getString(1),rec.getString(2),code,rec.getString(4));
                                db.set_item_flag(rec.getString(3),0);
                                db.update_item_itemcode(rec.getString(3),old_code);
                            }
                            i++;
                        }
                        i = 0;
                        break;
                    case "Dairy":
                        JSONArray products5 = json_obj.getJSONArray(type);
                        while (i < products5.length()) {
                            JSONArray rec = products5.getJSONArray(i);
                            if (db.read_item(0,rec.getString(3)).equals("Error")) {
                                Log.d(TAG,"Item not found. Adding to DB");
                                Log.d(TAG,"Product: " + rec.getString(0));
                                String old_code = db.add_new_item(rec.getString(0),type,rec.getString(1),rec.getString(2),code,rec.getString(4));
                                db.set_item_flag(rec.getString(3),0);
                                db.update_item_itemcode(rec.getString(3),old_code);
                            }
                            i++;
                        }
                        i = 0;
                        break;
                    case "Sweet":
                        JSONArray products6 = json_obj.getJSONArray(type);
                        while (i < products6.length()) {
                            JSONArray rec = products6.getJSONArray(i);
                            if (db.read_item(0,rec.getString(3)).equals("Error")) {
                                Log.d(TAG,"Item not found. Adding to DB");
                                Log.d(TAG,"Product: " + rec.getString(0));
                                String old_code = db.add_new_item(rec.getString(0),type,rec.getString(1),rec.getString(2),code,rec.getString(4));
                                db.set_item_flag(rec.getString(3),0);
                                db.update_item_itemcode(rec.getString(3),old_code);
                            }
                            i++;
                        }
                        i = 0;
                        break;
                    case "Others":
                        JSONArray products7 = json_obj.getJSONArray(type);
                        Log.v(TAG,"others");
                        while (i < products7.length()) {
                            JSONArray rec = products7.getJSONArray(i);
                            Log.v(TAG,"Code: " + rec.getString(3));
                            Log.v(TAG,"Reading: " + db.read_item(0,rec.getString(3)));
                            if (db.read_item(0,rec.getString(3)).equals("Error")) {
                                Log.d(TAG,"Item not found. Adding to DB");
                                Log.d(TAG,"Product: " + rec.getString(0));
                                String old_code = db.add_new_item(rec.getString(0),type,rec.getString(1),rec.getString(2),code,rec.getString(4));
                                db.set_item_flag(rec.getString(3),0);
                                db.update_item_itemcode(rec.getString(3),old_code);
                            }
                            i++;
                        }
                        i = 0;
                        break;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error JSON");
            e.printStackTrace();
        }
        read_from_internal_DB();
        reload_ui(1);
    }

    /**
     * Override onActivityResult method. Get results from the AddItem activity
     * @param requestCode Request Code
     * @param resultCode Result Code
     * @param data Data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Add product
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // New Item
                Log.d(TAG, "Result OK");
                String product = data.getStringExtra("product");
                String quantity = data.getStringExtra("quantity");
                String price = data.getStringExtra("price");
                if (price.equals("")) price = "null";
                String type = data.getStringExtra("type");
                Log.d(TAG, product + quantity + price + type);

                if (!usr_inf.getOffline_mode()) {
                    adapter.notifyDataSetChanged();
                    all_items_l.clear();
                    meat_items_l.clear();
                    vegetables_items_l.clear();
                    cereals_items_l.clear();
                    dairy_items_l.clear();
                    sweet_items_l.clear();
                    others_items_l.clear();
                    old_codes = db.add_new_item(product, type, quantity, price, code, usr_inf.getName());
                    Log.v(TAG,"added new item");
                    print_db();
                    send_request_server("new_item", list_type, code, type, product, price, quantity, usr_inf.getName());
                }

                if (usr_inf.getOffline_mode()) {
                    old_codes = db.add_new_item(product, type, quantity, price, code, usr_inf.getName());
                    adapter.notifyDataSetChanged();
                    all_items_l.clear();
                    meat_items_l.clear();
                    vegetables_items_l.clear();
                    cereals_items_l.clear();
                    dairy_items_l.clear();
                    sweet_items_l.clear();
                    others_items_l.clear();
                    read_from_internal_DB();
                }
                reload_ui(1);
                // New item added
                print_db();
                Log.d(TAG,"Adding new Item");
            }
        }
        // Edit products
        else if (requestCode==2) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Result OK");
                String product = data.getStringExtra("product");
                String quantity = data.getStringExtra("quantity");
                String price = data.getStringExtra("price");
                if (price.equals("")) price = " ";

                String type = data.getStringExtra("type");
                Log.d(TAG, product + quantity + price + type);

                Object item = adapter.getItem(currentSelection);
                String Product = ((HashMap) item).get(FIRST_COLUMN).toString();
                String Quantity = ((HashMap) item).get(SECOND_COLUMN).toString();
                String Price_currency = ((HashMap) item).get(THIRD_COLUMN).toString();
                String Price = Price_currency.split(currency)[0];
                if (Price.equals("-") | Price.equals("")) {
                    Price = "null";
                }

                //Get type of product
                String type_prod = get_Product_Type(adapter.getItem(currentSelection).toString());

                //Get unique code
                String code_item = db.read_code_items(Product, Quantity, Price, type_prod);

                if (!usr_inf.getOffline_mode())
                    send_request_server("update_item", list_type, code, type, product, price, quantity, code_item);

                if (usr_inf.getOffline_mode()){
                    db.update_item_value(product,quantity,price,code_item);
                    db.set_item_flag(code_item,1);
                }
                print_db();
                reload_ui(1);
                all_items_l.clear();
                meat_items_l.clear();
                vegetables_items_l.clear();
                cereals_items_l.clear();
                dairy_items_l.clear();
                sweet_items_l.clear();
                others_items_l.clear();
                adapter.notifyDataSetChanged();
                if (usr_inf.getOffline_mode()) read_from_internal_DB();
            }
        }
    }

    /**
     * Send request to Update Server service
     * @param Objective Objective
     * @param status Type of shopping list
     * @param code Code of shopping list
     * @param type type of product
     * @param product product name
     * @param price price
     * @param quantity quantity
     * @param code_item code of the product
     */
    private void send_request_server(final String Objective, String status, String code, String type, String product, String price, String quantity, String code_item) {
        server_service.set_values(server_service.get_objective(Objective), code, "_", "True", status);
        server_service.set_items(type, product, price, quantity, code_item);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                server_service.send_request();
                //noinspection StatementWithEmptyBody
                while (!server_service.return_response_status()) ;
                String response = server_service.return_result();
                Intent intent = new Intent();
                intent.setAction("broadcast_service");
                intent.putExtra("Main", response);
                intent.putExtra("Request", Objective);
                sendBroadcast(intent);
            }
        });
        t.start();
    }



    /**
     * Delete selected item
     */
    private void delete_item() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Delete the product from the shopping list?");
        alert.setMessage("Do you really want to delete the product?");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Object item = adapter.getItem(currentSelection);
                String Product = ((HashMap) item).get(FIRST_COLUMN).toString();
                String Quantity = ((HashMap) item).get(SECOND_COLUMN).toString();
                String Price_currency = ((HashMap) item).get(THIRD_COLUMN).toString();
                String Price = Price_currency.split(currency)[0];
                if (Price.equals("-")) Price = "null";
                String type = get_Product_Type(adapter.getItem(currentSelection).toString());

                Log.d(TAG, "DELETE: "+Product+Quantity+Price+type);

                String code_item = db.read_code_items(Product, Quantity, Price, type);


                //If we are online, send post request, else add a change type of removal to the product
                if (!usr_inf.getOffline_mode()) {
                    send_request_server("delete_item", list_type, code, type, Product, Price, Quantity, code_item);
                    print_db();
                    db.delete_item(code_item);
                }
                else {
                    db.update_item_change("delete_item", code_item);

                    all_items_l.clear();
                    meat_items_l.clear();
                    vegetables_items_l.clear();
                    cereals_items_l.clear();
                    dairy_items_l.clear();
                    sweet_items_l.clear();
                    others_items_l.clear();
                    adapter.notifyDataSetChanged();
                    read_from_internal_DB();
                    reload_ui(1);
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });

        alert.show();
    }

    /**
     * Get the type of the product
     * @param selection Selection
     * @return Type
     */
    private String get_Product_Type(String selection) {
        String type = "";
        for (int i = 0; i < meat_items_l.size(); i++) {
            if (meat_items_l.get(i).toString().equals(selection)) type = "Meat and Fish";
        }
        for (int i = 0; i < vegetables_items_l.size(); i++) {
            if (vegetables_items_l.get(i).toString().equals(selection)) type = "Vegetables";
        }
        for (int i = 0; i < cereals_items_l.size(); i++) {
            if (cereals_items_l.get(i).toString().equals(selection)) type = "Cereal";
        }
        for (int i = 0; i < dairy_items_l.size(); i++) {
            if (dairy_items_l.get(i).toString().equals(selection)) type = "Dairy";
        }
        for (int i = 0; i < sweet_items_l.size(); i++) {
            if (sweet_items_l.get(i).toString().equals(selection)) type = "Sweet";
        }
        for (int i = 0; i < others_items_l.size(); i++) {
            if (others_items_l.get(i).toString().equals(selection)) type = "Others";
        }
        return type;
    }

    /**
     * Send a essage to the Update_Android service using a Messenger.
     */
    private void getAll_products() {
        if (is_bound) {
            Message msg = Message.obtain(null, Update_Android.MSG_GET_DATA);
            Bundle bundle = new Bundle();
            bundle.putString("request", "one_list");
            bundle.putString("GoogleAccount", usr_inf.getEmail());
            bundle.putString("code_list", code);
            bundle.putString("Activity", "Items");
            msg.setData(bundle);
            //Send message
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * BroadcastReceiver class
     */
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String request_type = intent.getStringExtra("Request");
            String main_receiver = intent.getStringExtra("Main");

            Log.v(TAG, "Received: "+request_type);
            //Check type of request
            switch(request_type){
                case "finish_activity":
                    finish();
                    break;
                case "one_list":
                    String update_product = intent.getStringExtra("Update_Products");
                    if (update_product.equals("True")) {
                        String list_items = intent.getStringExtra("One_list");
                        Log.v(TAG,"Receiver list: " + list_items);
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        update_ShoppingList(list_items);
                    }
                    break;
                case "new_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.add_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        Log.d(TAG, "adding new code: " + main_receiver);
                        Log.d(TAG, "old_code: " + old_codes);
                        db.update_item_itemcode(main_receiver, old_codes);
                        try {
                            db.delete_item(old_codes);
                        } catch (android.database.CursorIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                        db.set_item_flag(main_receiver, 0);
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        reload_ui(1);
                        Log.d(TAG, "Added new product correctly");
                    }
                    break;
                case "delete_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.delete_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else{
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        Log.d(TAG, "Deleted product correctly");
                    }
                    break;
                case "update_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.update_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        adapter.notifyDataSetChanged();
                        all_items_l.clear();
                        meat_items_l.clear();
                        vegetables_items_l.clear();
                        cereals_items_l.clear();
                        dairy_items_l.clear();
                        sweet_items_l.clear();
                        others_items_l.clear();
                        adapter.notifyDataSetChanged();
                        getAll_products();
                        Log.d(TAG, "Product updated correctly");
                    }
                    break;
            }
        }
    }

    /**
     * Edit the product
     */
    private void edit_item(){
        Object item = adapter.getItem(currentSelection);
        String Product = ((HashMap) item).get(FIRST_COLUMN).toString();
        String Quantity = ((HashMap) item).get(SECOND_COLUMN).toString();
        String Price_currency = ((HashMap) item).get(THIRD_COLUMN).toString();
        String Price = Price_currency.split(currency)[0];
        if (Price.equals("-")) Price = null;

        String type = get_Product_Type(adapter.getItem(currentSelection).toString());
        Intent intent = new Intent(Items.this, AddItem.class);
        intent.putExtra("Edit", "True");
        intent.putExtra("Product", Product);
        intent.putExtra("Price", Price);
        intent.putExtra("Quantity", Quantity);
        intent.putExtra("Type", type);
        // Start next activity
        startActivityForResult(intent, 2);
    }

    /**
     * Prints DB entries
     */
    private void print_db(){
        // Product, Quantity, Price, Type, Last_User, Code_item
        List<String[]> entries = db.read_all_items(code);
        Log.d(TAG, "STARTING THE PRINT DB");
        for (int i=0; i<entries.size();i++)
            Log.d(TAG,"Entries: " + entries.get(i)[0] +" " + entries.get(i)[1] +" " + entries.get(i)[2]+" " + entries.get(i)[3] + " " + entries.get(i)[5]);
        Log.d(TAG,"END");
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
     * Read entries from the internal database and build the listview
     */
    private void read_from_internal_DB() {
        Log.d(TAG, "Reading from internal DB");
        // Product, Quantity, Price, Type, Last_User, Code_item
        List<String[]> a = db.read_all_items(code);
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                String[] b = a.get(i);
                temp = new HashMap<>();

                if (b[2].equals("null"))
                    temp.put(THIRD_COLUMN, "-" + currency);
                else
                    temp.put(THIRD_COLUMN, b[2] + currency);

                temp.put(FIRST_COLUMN, b[0]);
                temp.put(SECOND_COLUMN, b[1]);
                if (list_type.equals("0")) temp.put(FOURTH_COLUMN, a.get(i)[4]);
                all_items_l.add(temp);

                switch (b[3]) {
                    case "Meat and Fish":
                        temp = new HashMap<>();
                        if (b[2].equals("null"))
                            temp.put(THIRD_COLUMN, "-" + currency);
                        else
                            temp.put(THIRD_COLUMN, b[2] + currency);

                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);

                        if (list_type.equals("0"))
                            temp.put(FOURTH_COLUMN, a.get(i)[4]);

                        meat_items_l.add(temp);


                        break;
                    case "Vegetables":
                        temp = new HashMap<>();

                        if (b[2].equals("null"))
                            temp.put(THIRD_COLUMN, "-" + currency);
                        else
                            temp.put(THIRD_COLUMN, b[2] + currency);

                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);

                        if (list_type.equals("0"))
                            temp.put(FOURTH_COLUMN, a.get(i)[4]);

                        vegetables_items_l.add(temp);

                        break;
                    case "Cereal":
                        temp = new HashMap<>();
                        if (b[2].equals("null"))
                            temp.put(THIRD_COLUMN, "-" + currency);
                        else
                            temp.put(THIRD_COLUMN, b[2] + currency);

                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);

                        if (list_type.equals("0"))
                            temp.put(FOURTH_COLUMN, a.get(i)[4]);

                        cereals_items_l.add(temp);

                        break;
                    case "Dairy":
                        temp = new HashMap<>();
                        if (b[2].equals("null"))
                            temp.put(THIRD_COLUMN, "-" + currency);
                        else
                            temp.put(THIRD_COLUMN, b[2] + currency);

                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);

                        if (list_type.equals("0"))
                            temp.put(FOURTH_COLUMN, a.get(i)[4]);

                        dairy_items_l.add(temp);


                        break;
                    case "Sweet":
                        temp = new HashMap<>();
                        if (b[2].equals("null"))
                            temp.put(THIRD_COLUMN, "-" + currency);
                        else
                            temp.put(THIRD_COLUMN, b[2] + currency);

                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);

                        if (list_type.equals("0"))
                            temp.put(FOURTH_COLUMN, a.get(i)[4]);

                        sweet_items_l.add(temp);

                        break;
                    case "Others":
                        temp = new HashMap<>();
                        if (b[2].equals("null"))
                            temp.put(THIRD_COLUMN, "-" + currency);
                        else
                            temp.put(THIRD_COLUMN, b[2] + currency);

                        temp.put(FIRST_COLUMN, b[0]);
                        temp.put(SECOND_COLUMN, b[1]);

                        if (list_type.equals("0"))
                            temp.put(FOURTH_COLUMN, a.get(i)[4]);

                        others_items_l.add(temp);

                        break;
                }
            }
        }
    }

    /**
     * Method to synchronize the server with the internal database
     * @return Return true
     */
    private boolean send_unsynced_entries(){
        // Get all items with sync flag set
        List<String[]> entries = db.read_all_with_flag_set_item();
        print_db();
        if (entries == null) return true;
        Log.d(TAG,"Size: "+entries.size());
        for (int i = 0; i< entries.size(); i++) {
            final String entry[] = entries.get(i);
            //If it is from this shopping list
            if (code.equals(entry[7])) {
                db.set_item_flag(entry[5], 0);
                // Product, Quantity, Price, Type, Last_User, Code_item, change type, code_list
                if (entry[6].equals("new_item"))
                    old_codes = entry[5];
                if (entry[6].equals("new_item")){
                    send_request_server(entry[6], list_type, code, entry[3], entry[0], entry[2], entry[1], usr_inf.getName());
                }
                else{
                    send_request_server(entry[6], list_type, code, entry[3], entry[0], entry[2], entry[1], entry[5]);
                }
                // delete list really.
                if (entry[6].equals("delete_item")) db.delete_item(entry[5]);
            }
            // Wait
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        print_db();
        return true;
    }


    /**
     * Asynchronous task to get all the products either by the server or the internal database
     */
    class ProgressTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG,"STARTING EXECUTION OF APP");
            all_items_l.clear();
            meat_items_l.clear();
            vegetables_items_l.clear();
            cereals_items_l.clear();
            dairy_items_l.clear();
            sweet_items_l.clear();
            others_items_l.clear();
            adapter.notifyDataSetChanged();
            listview_items.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            if (!usr_inf.getOffline_mode())
                getAll_products();
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
            listview_items.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to check if there is internet connecion
     * @return Return true if there is internet connection
     */
    private boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

