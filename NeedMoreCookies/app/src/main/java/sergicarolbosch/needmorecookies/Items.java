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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
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

    // Item class
    private List<Item> item = new ArrayList<>();

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
    private RecyclerView mRecyclerView;
    private ProgressBar loading;
    private View previous_selected_view;

    private CountDownTimer timer;
    private CountDownTimer timer2;

    /**
     * [START ListView]
     **/
    //Heade<String, String>> l_header = new ArrayList<>();

    private ItemRecyclerAdapter item_adapter;

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
    private int selected_item = -1;
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
        mRecyclerView = (RecyclerView) findViewById(R.id.my_item_recycler_view);

        ListView listview_header = (ListView) findViewById(R.id.list_header);
        /**[END UI elements]**/

        /**[START Intent-filter for receiving Broadcast]**/
        filter = new IntentFilter("broadcast_service");
        receiver_items = new MyReceiver();
        this.registerReceiver(receiver_items, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START Service binding]**/
        Intent intent = new Intent(this, Update_Android.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        Intent in = new Intent(this, Update_Server.class);
        bindService(in, mConnection2, Context.BIND_AUTO_CREATE);
        /**[END Service binding]**/

        /**[START List View]**/
        RecyclerView.LayoutManager mLayoutManager;
        mRecyclerView.setHasFixedSize(true);
        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);
        item_adapter = new ItemRecyclerAdapter(item);
        mRecyclerView.setAdapter(item_adapter);
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

        item_adapter.setOnItemClickListener(new ItemRecyclerAdapter.ClickListener() {
            @Override
            public void onItemClick(Item selected_item, View v) {

            }

            @Override
            public void onItemLongClick(int position, View v) {
                if (previous_selected_view != null)
                    previous_selected_view.setBackgroundColor(Color.WHITE);

                if (position == selected_item) {
                    v.setBackgroundColor(Color.WHITE);
                    selected_item = -1;
                }
                else {
                    v.setBackgroundColor(Color.CYAN);
                    selected_item = position;
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
                        Log.v(TAG,"Item swiped left");
                        delete_item(viewHolder.getAdapterPosition());
                        item.remove(viewHolder.getAdapterPosition());
                        Log.v(TAG, item.toString());
                        item_adapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        reload_ui(current_tab);
                        break;
                }
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
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
                    reload_ui(1);
                }
                return true;
            case R.id.edit_item:
                if (selected_item != -1)
                    edit_item(selected_item);
                else
                    Toast.makeText(getBaseContext(), R.string.select_element, Toast.LENGTH_SHORT).show();
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
        if (previous_selected_view != null) {
            ColorDrawable bg_color = (ColorDrawable) previous_selected_view.getBackground();
            if (bg_color.getColor() == Color.CYAN) {
                previous_selected_view.setBackgroundColor(Color.WHITE);
                selected_item = -1;
            }
        }
        //If the navigation menu is opened, closed it
        else if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();

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

        /*if (id == R.id.nav_locations) {
            Intent intent = new Intent(Items.this, MapsActivity.class);
            // Start next activity
            startActivity(intent);
            finish();
        } else*/
        if (id == R.id.nav_home) {
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
        List<Item> current_items = new ArrayList<>();
        read_from_internal_DB();
        separator1.setVisibility(View.INVISIBLE);
        separator2.setVisibility(View.INVISIBLE);
        separator3.setVisibility(View.INVISIBLE);
        separator4.setVisibility(View.INVISIBLE);
        separator5.setVisibility(View.INVISIBLE);
        separator6.setVisibility(View.INVISIBLE);
        separator7.setVisibility(View.INVISIBLE);
        // All
        if (type == 1) {
            separator1.setVisibility(View.VISIBLE);
            current_items = item;
        } else if (type == 2) {
            separator2.setVisibility(View.VISIBLE);
            current_items = get_items_with_type("Meat and Fish");
        } else if (type == 3) {
            separator3.setVisibility(View.VISIBLE);
            current_items = get_items_with_type("Vegetables");
        } else if (type == 4) {
            current_items = get_items_with_type("Cereal");
            separator4.setVisibility(View.VISIBLE);
        } else if (type == 5) {
            current_items = get_items_with_type("Dairy");
            separator5.setVisibility(View.VISIBLE);
        } else if (type == 6) {
            current_items = get_items_with_type("Sweet");
            separator6.setVisibility(View.VISIBLE);
        } else if (type == 7) {
            current_items = get_items_with_type("Others");
            separator7.setVisibility(View.VISIBLE);
        }
        current_tab = type;
        // Unselect previous selected
        if (previous_selected_view != null)
            previous_selected_view.setBackgroundColor(Color.WHITE);
        item_adapter.swap(current_items);
    }

    /**
     * Add products to the internal database
     * @param list List of products
     */

    // WTF IS GOING ON HERE
    private void update_ShoppingList(String list) {
        List<String> c = new ArrayList<>();
        try {
            int i = 0;
            JSONObject json_obj = new JSONObject(list);
            Iterator<String> keys = json_obj.keys();
            print_db();
            while (keys.hasNext()) {
                String type = String.valueOf(keys.next());
                JSONArray products2 = json_obj.getJSONArray(type);
                while (i < products2.length()) {
                    JSONArray rec = products2.getJSONArray(i);
                    c.add(rec.getString(3));
                    if (db.read_item(0, rec.getString(3)).equals("Error")) {
                        Log.d(TAG, "Item not found. Adding to DB");
                        Log.d(TAG, "Product: " + rec.getString(0));
                        String old_code = db.add_new_item(rec.getString(0), type, rec.getString(1), rec.getString(2), code, rec.getString(4));
                        db.set_item_flag(rec.getString(3), 0);
                        db.update_item_itemcode(rec.getString(3), old_code);
                    }
                    else if (!db.read_item(1,rec.getString(3)).equals(rec.getString(0)) || !db.read_item(3,rec.getString(3)).equals(rec.getString(1)) || !db.read_item(4,rec.getString(3)).equals(rec.getString(2))){
                        db.update_item_value(rec.getString(0),rec.getString(1),rec.getString(2),rec.getString(3));
                    }
                    i++;
                }
                i = 0;
            }
        } catch (JSONException e) {
            Log.d(TAG, "Error JSON");
            e.printStackTrace();
        }
        remove_deleted_SL(c);
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
                    old_codes = db.add_new_item(product, type, quantity, price, code, usr_inf.getName());
                    Log.v(TAG,"added new item");
                    print_db();
                    send_request_server("new_item", list_type, code, type, product, price, quantity, usr_inf.getName());
                }

                if (usr_inf.getOffline_mode()) {
                    old_codes = db.add_new_item(product, type, quantity, price, code, usr_inf.getName());
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
                String code_item = item.get(selected_item).getItem_Code();
                String type = data.getStringExtra("type");
                Log.d(TAG, product + quantity + price + type);

                if (!usr_inf.getOffline_mode())
                    send_request_server("update_item", list_type, code, type, product, price, quantity, code_item);

                if (usr_inf.getOffline_mode()){
                }
                print_db();
                reload_ui(1);
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
    private void delete_item(final int position) {

        Item current_item = item.get(position);
        String Product = current_item.getItem_Name();
        String Quantity = current_item.getItem_Quantity();
        String Price = current_item.getItem_Price();
        if (Price.equals("-")) Price = "null";
        String type = current_item.getItem_Type();
        Log.d(TAG, "DELETE: "+Product+Quantity+Price+type);
        String code_item = current_item.getItem_Code();
        //If we are online, send post request, else add a change type of removal to the product
        if (!usr_inf.getOffline_mode()) {
            send_request_server("delete_item", list_type, code, type, Product, Price, Quantity, code_item);
            print_db();
            db.delete_item(code_item);
        }
        else {
            db.update_item_change("delete_item", code_item);
            read_from_internal_DB();
            reload_ui(1);
        }
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
                        update_ShoppingList(list_items);
                    }
                    break;
                case "new_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.add_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
                        db.update_item_itemcode(main_receiver, old_codes);
                        try {
                            db.delete_item(old_codes);
                        } catch (android.database.CursorIndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                        db.set_item_flag(main_receiver, 0);
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
                        getAll_products();
                        Toast.makeText(Items.this,R.string.item_deleted,Toast.LENGTH_LONG).show();
                        Log.d(TAG, "Deleted product correctly");
                    }
                    break;
                case "update_item":
                    if (main_receiver.equals("False"))
                        Toast.makeText(Items.this,R.string.update_item_error,Toast.LENGTH_SHORT)
                                .show();
                    else {
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
    private void edit_item(int position){
        Item edit_item = item.get(position);
        String Product = edit_item.getItem_Name();
        String Quantity = edit_item.getItem_Quantity();
        String Price = edit_item.getItem_Price();
        if (Price.equals("-")) Price = null;
        String type = edit_item.getItem_Type();
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
        item.clear();
        String price, addedby;
        if (a != null) {
            for (int i = 0; i < a.size(); i++) {
                String[] b = a.get(i);
                if (b[2].equals("null")) price = "-";
                else price = b[2];
                if (list_type.equals("0")) addedby = String.format(getResources().getString(R.string.last_mod_by),b[4]);
                else addedby = "";
                item.add(new Item(b[0], b[1], price, addedby, b[3], b[5]));
            }
        }
        print_items(item);
    }

    private List<Item> get_items_with_type(String item_type){
        List<Item> items_with_type = new ArrayList<>();
        print_items(item);
        for (int i = 0; i < item.size(); i++){
            Item current_item = item.get(i);
            Log.v("Printing","Item type: " + current_item.getItem_Type());
            if (current_item.getItem_Type().equals(item_type)) items_with_type.add(current_item);
        }
        print_items(items_with_type);
        return items_with_type;
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
            mRecyclerView.setVisibility(View.GONE);
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
            mRecyclerView.setVisibility(View.VISIBLE);
            loading.setVisibility(View.GONE);
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

    void print_items(List<Item> items_to_print){
        for (int i = 0; i < items_to_print.size(); i++)
            Log.v("Printing"," Name: " + items_to_print.get(i).getItem_Name());
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
        internal_db_lists = db.read_all_items(code);
        // Add all codes from entries into a list
        for (i = 0;i < internal_db_lists.size();i++)
            internal_codes.add(internal_db_lists.get(i)[4]);
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
                db.delete_item(internal_codes.get(i));
            }
        }
    }
}

