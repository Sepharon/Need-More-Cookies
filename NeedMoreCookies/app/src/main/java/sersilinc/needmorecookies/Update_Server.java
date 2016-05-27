package sersilinc.needmorecookies;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * The aim of this class is to offer a service to send request to our server. It also
 * implements a class to help create JSON objects
 */

public class Update_Server extends Service {

    //URL
    private static final String url = "https://www.tfg.centrethailam.com";

    //Tag for Logs
    private final String TAG = "Update_Server: ";

    //JSONEncoder class instance
    private JSONEncoder jsonEncoderClass = new JSONEncoder();
    private final IBinder mBinder = new LocalBinder();
    private MyReceiver receiver;

    //JSON values
    private final String [] keys = {"Objective","Code","list_name","Update","GoogleAccount","status"};
    private String [] values = new String[6];
    private String [] items = new String[5];
    private final String [] objectives = {"update_item","new_item","delete_item","new_list","delete_list","change_list_name","set_public","add_usr_to_list","add_user", "add_token"};
    private String request_result;

    //Flag
    private boolean got_response = false;

    // Binder initializer
    public class LocalBinder extends Binder {
        Update_Server getService() {
            // Return this instance of LocalService so clients can call public methods
            return Update_Server.this;
        }
    }

    // This function gets called when the service gets binded
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, " Binding");
        return mBinder;
    }

    @Override
    public void onCreate (){
        super.onCreate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        /**[START Intent-filter for receiving Broadcast]**/
        IntentFilter filter = new IntentFilter("Update_Server_Thread");
        receiver = new MyReceiver();
        this.registerReceiver(receiver, filter);
        /**[END Intent-filter for receiving Broadcast]**/

        /**[START JSON Encoder Class]**/
        jsonEncoderClass.create_template();
        /**[END JSON Encoder Class]**/
    }

    // Called when the service dies
    @Override
    public void onDestroy(){
        super.onDestroy();
        // Unregister the receiver
        unregisterReceiver(receiver);
    }

    // Called when the service is ready
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, " Update Server started");
        return START_STICKY;
    }

    // Sets the values for the JSON object
    public boolean set_values(int objective_code,String list_code,String list_name,String update,String status){
        if (objective_code > 9) return false;
        values[0] = objectives[objective_code];
        values[1] = list_code;
        values[2] = list_name;
        values[3] = update;
        values[4] = User_Info.getInstance().getEmail();
        values[5] = status;
        // Create JSON from the values
        set_json(keys,values, 0);
        Log.v(TAG, String.valueOf(jsonEncoderClass.return_json()));
        return true;
    }

    // Sets items for the JSON object
    public void set_items(String Type, String Product_name, String Price, String Quantity, String Code){
        items[0] = Type;
        items[1] = Product_name;
        items[2] = Price;
        items[3] = Quantity;
        items[4] = Code;
        // Create JSON from items
        set_json(keys,items,1);
    }

    // Send a request to the server
    public void send_request (){
        // Check if internet is available
        if (is_network_available()) {
            got_response = false;
            Log.v(TAG, "JSON TO SEND: " + jsonEncoderClass.return_json());
            send_post_request(jsonEncoderClass.return_json());
        }
    }

    // Given an objective returns the position of it
    public int get_objective(String objec){
        for (int i = 0; i < objectives.length; i++) {
            if (objectives[i].equals(objec)) {
                return i;
            }
        }
        return -1;
    }

    // Creates the appropriate JSON based if the values need to go in the main or in Values
    private boolean set_json(String [] key, String[] value,int update_main){
        if (jsonEncoderClass.return_json() == null) return false;
        if (update_main == 0) {
            if (key.length != value.length) return false;
            jsonEncoderClass.set_values(key, value, update_main);
        }
        else jsonEncoderClass.set_values(key, value, update_main);
        return true;
    }

    // Send a request to the server
    private void send_post_request(final JSONObject o){
        // Create new thread so not to block the UI
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                String response = "";
                // Create an HTTPS request
                HttpsURLConnection connection = null;
                try {
                    URL link_url = new URL(url);
                    connection = (HttpsURLConnection)link_url.openConnection();
                    //Set to POST
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "application/json");
                    connection.setReadTimeout(10000);
                    Writer writer = new OutputStreamWriter(connection.getOutputStream());
                    writer.write(o.toString());
                    writer.flush();
                    writer.close();
                    // Read the input from the server
                    Reader in = new InputStreamReader(connection.getInputStream(), "UTF-8");
                    Log.v("Thread", "Sended");
                    StringBuilder sb = new StringBuilder();
                    for (int c; (c = in.read()) >= 0;) sb.append((char) c);
                    in.close();
                    response = sb.toString();
                    Log.v("Thread response: ", response);
                    connection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                finally {
                    // End connection
                    if (connection != null) connection.disconnect();
                    Intent intent = new Intent();
                    intent.putExtra("message",response);
                    intent.setAction("Update_Server_Thread");
                    Log.d("Thread ", "Sending response");
                    sendBroadcast(intent);
                }
            }
        });
        t.start();
    }

    // Returns the result from the connection (server response)
    public String return_result(){
        return request_result;
    }

    // Returns true if we got an answer from the server
    public boolean return_response_status() {return got_response;}

    //Check if network available
    private boolean is_network_available(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Process the message received from the server and checks if it is correct
    private void process_message(String response){
        try {
            JSONObject rsp = new JSONObject(response);
            String usr_email = rsp.getJSONObject("main").getString("GoogleAccount");
            String result = rsp.getJSONObject("Result").getString("result");
            if (usr_email.equals(User_Info.getInstance().getEmail())) request_result = result;
            else request_result = "False";

        } catch (JSONException e) {
            e.printStackTrace();
            Log.v(TAG, "Unable to create JSON from string");
            Log.v(TAG, "Received JSON: " + response);
            request_result = "False";
        } finally {
            got_response = true;
        }
    }

    // Receiver
    public class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Extract data included in the Intent
            String message = intent.getStringExtra("message");
            Log.d(TAG, "Got message: " + message);
            process_message(message);
        }
    }

    // JSONEncoder class
    private class JSONEncoder{
        String TAG = "JSONEncoder";
        JSONObject obj;

        // Create a templeate for the JSON
        public JSONObject create_template(){
            Log.v(TAG, " Started");
            try {
                obj = new JSONObject("{\"main\":{\"status\":\"0\",\"Code\":\"default\",\"list_name\":\"default\",\"Request\":\"Update Server\",\"GoogleAccount\":\"default\", \"Objective\":\"default\"},\"Values\":{}}");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return obj;
        }

        // Put values inside the JSON
        public void set_values(String key [], String value [],int update_main){
            switch (update_main){
                case 0:
                    try {
                        for (int i = 0; i < key.length; i++)
                            obj.getJSONObject("main").put(key[i],value[i]);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        JSONArray items = new JSONArray();
                        JSONArray a = new JSONArray();
                        JSONObject tmp = new JSONObject();
                        a.put(value[0]);
                        a.put(value[1]);
                        items.put(a);
                        items.put(value[2]);
                        items.put(value[3]);
                        items.put(value[4]);
                        tmp.put("Item",items);
                        obj.put("Values",tmp);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
        // Return the JSON object
        public JSONObject return_json(){
            return obj;
        }
    }

}

