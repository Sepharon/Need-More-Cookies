package sergicarolbosch.needmorecookies;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * The aim of this class is to help other classes deal with the DB by offering more high end functions
 * rather than the low end functions found in the SQLiteDB class
 */

public class DB_Helper {

    // TAG for the logs
    private final String TAG = "DB_Helper";

    // SQLiteDB connection, this variable stores the connection to the internal DB
    private static SQLiteDB DataBase;

    // Class initializer
    public DB_Helper(Context context) {
        Log.d(TAG,"Initializing DB_Helper");
        DataBase = new SQLiteDB(context);
    }

    // Disables the connection with the SQLiteDB class, only called when the app is closed or killed
    public void destroy_class(){
        DataBase = null;
    }

    // Adds a new Shopping List to the DB and returns its code
    public String add_new_list(String new_list,int type){
        Log.d(TAG,"added new list with name: "+new_list);
        DataBase.add_new_list(new_list,type);
        return get_code_last_list();
    }

    // Updates the name of a Shopping List
    public boolean update_list_name(String new_name,String code){
        int result;
        Log.d(TAG,"Updating name: " + new_name + " "+ code);
        String flag_status;
        // Read the sync flag value
        flag_status = read_shopping_list(2,code);
        // List does not need to synchronize
        if (flag_status.equals("0") && User_Info.getInstance().getOffline_mode()){
            Log.d(TAG,"Flag is 0");
            // Set the change type to "change_list_name" and the sync flag to 1
            DataBase.update_list(new String[]{DataBase.KEY_CHANGE_TYPE, "change_list_name"},code);
            DataBase.update_list(new String[]{DataBase.KEY_FLAG,"1"},code);
        }
        // If the sync flag is set to 1 do not change the "Change Type" key, since it might still
        // be set to "new_list" in which case we just change the name, acting as we created a new list
        // Update the Shopping List name
        result = DataBase.update_list(new String[]{DataBase.KEY_LIST_NAME, new_name},code);
        return result != 0;
    }

    // Updates the Shopping List code
    public boolean update_list_code(String new_code,String code){
        Log.d(TAG,"Updating code");
        int result = DataBase.update_list(new String[]{DataBase.KEY_CODE, new_code},code);
        return result!=0;
    }

    // Update fav icon value
    public boolean update_favourite_value(boolean fav_value, String code){
        Log.d(TAG,"Updating fav value");
        int result = DataBase.update_list(new String[]{DataBase.KEY_FAVOURITE, String.valueOf(fav_value ? 1 : 0 )},code);
        Log.v(TAG,""+result);
        return result!=0;
    }

    // Sets a list to public or private
    public boolean update_list_public(int pub,String code){
        Log.d(TAG,"Updating code");
        int result = DataBase.update_list(new String[]{DataBase.KEY_PUBLIC, String.valueOf(pub)},code);
        return result!=0;
    }

    // Updates the Change Type field
    public boolean update_list_change(String new_change,String code){
        Log.d(TAG,"Updating list change type to: " + new_change);
        int result  = DataBase.update_list(new String[]{DataBase.KEY_CHANGE_TYPE, new_change},code);
        int result2 = DataBase.update_list(new String[]{DataBase.KEY_FLAG,"1"},code);
        return (result&result2)!=0;
    }

    // Updates the timestamp with the current time
    public boolean update_timestamp_android(String code){
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Log.d(TAG,"Changing list name at " + timeStamp);
        int result = DataBase.update_list(new String[] {DataBase.KEY_UPDATE,String.valueOf(timeStamp)},code);
        return result!=0;
    }

    // Updates the timestamp with the server time
    public boolean update_timestamp_server(String code,String time){
        Log.d(TAG,"Changing list name at " + time);
        int result = DataBase.update_list(new String[] {DataBase.KEY_UPDATE,String.valueOf(time)},code);
        return result!=0;
    }

    // Changes the sync flag value
    public boolean set_list_flag(String code, int flag){
        if (!User_Info.getInstance().getOffline_mode() && flag == 1) return false;
        int result = DataBase.update_list(new String[]{DataBase.KEY_FLAG,"" + flag},code);
        return result!=0;
    }

    // Deletes a Shopping List from the server
    public void delete_list(String code){
        Log.d(TAG,"Deleting Shopping List");
        DataBase.delete_list(code);
    }

    // Returns the Shopping List code from the last added Shopping List
    public String get_code_last_list(){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " +DataBase.Shopping_list_table_name +
                " WHERE ID_List=(SELECT MAX(ID_List) FROM " + DataBase.Shopping_list_table_name +")";
        return DataBase.read_shopping_lists(query);
    }

    // Returns the code given the name of a Shopping List
    public String read_code(String list_name){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " + DataBase.Shopping_list_table_name
                + " WHERE " + DataBase.KEY_LIST_NAME + String.format("='%s'",list_name);
        String result = DataBase.read_shopping_lists(query);
        if (result == null) return "Error";
        return result;
    }

    // Reads a field given a Shopping List code
    public String read_shopping_list(int value,String code){
        String table_name = DataBase.Shopping_list_table_name;
        String key,query,result;
        switch(value) {
            case 0:
                key = DataBase.KEY_ID_LIST;
                break;
            case 1:
                key = DataBase.KEY_LIST_NAME;
                break;
            case 2:
                key = DataBase.KEY_FLAG;
                break;
            case 3:
                key = DataBase.KEY_CHANGE_TYPE;
                break;
            case 4:
                key = DataBase.KEY_UPDATE;
                break;
            case 5:
                key = DataBase.KEY_PUBLIC;
                break;
            case 6:
                key = DataBase.KEY_FAVOURITE;
                break;
            default:
                return null;
        }

        query = "SELECT " + key + " FROM " + table_name + " WHERE " + DataBase.KEY_CODE + String.format("='%s'",code);
        result = DataBase.read_shopping_lists(query);
        if (result == null) return "Error";
        return result;
    }

    // Reads all the list from the Shopping List table
    public List<String[]> read_all_lists(){
        String table_name = DataBase.Shopping_list_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name;
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                Log.d(TAG,"type: " + result.getString(3));
                // If the change type is "delete_list" do not returnet, since it will be deleted shortly
                if (!result.getString(6).equals("delete_list")) {
                    // list name,timestamp , code,Public, fav_icon value
                    String[] entry = new String[]{result.getString(1), result.getString(2), result.getString(3),
                            result.getString(4),result.getString(6)};
                    r.add(entry);
                }
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Log.w(TAG,"Empty DB");
        }
        result.close();
        return r;
    }

    // Return all the Shopping Lists with the sync flag set (used to synchronize with the server)
    public List<String[]> read_all_with_flag_set_list(){
        String table_name = DataBase.Shopping_list_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name + " WHERE " +DataBase.KEY_FLAG + " = 1";
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                // list name, code,Public, change type
                String [] entry = new String[]{result.getString(1),result.getString(3),result.getString(4),
                        result.getString(6)};
                r.add(entry);
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Log.d(TAG, "Empty DB");
        }
        Log.d(TAG,"read_all_flag " + r.toString());
        result.close();
        return r;
    }

    // Adds a new item to the database
    public String add_new_item(String Product, String Type, String Quantity, String Price,String shopping_list_code,String user){
        // Add new item
        DataBase.add_new_item(Product, Type, Quantity, Price, shopping_list_code, user);
        update_timestamp_android(shopping_list_code);
        return get_code_last_item();
    }

    // Updates the Change Type field for an Item
    public boolean update_item_change(String new_change, String code_item){
        int result  = DataBase.update_item(new String[]{DataBase.KEY_CHANGE_TYPE, new_change},code_item);
        int result2 = DataBase.update_item(new String[]{DataBase.KEY_FLAG,"1"},code_item);
        return (result&result2)!=0;
    }

    // Edits an item, if the item has the sync flag set and the change type is set to new item,
    // then we do not update the change type
    public boolean update_item_value(String product, String quantity, String price,String code_item){
        String key;
        String change_type="update_item";
        // If sync flag is active do not change type
        if (read_item(5,code_item).equals("1") && read_item(6,code_item).equals("new_item")) change_type = "new_item";
        // Set values
        key= DataBase.KEY_PRODUCT;
        int result1 = DataBase.update_item(new String[]{key,product},code_item);

        key= DataBase.KEY_PRICE;
        int result2 = DataBase.update_item(new String[]{key,price},code_item);

        key= DataBase.KEY_QUANTITY;
        int result3 = DataBase.update_item(new String[]{key,quantity},code_item);

        // Change type
        int result4 = DataBase.update_item(new String[]{DataBase.KEY_CHANGE_TYPE,change_type},code_item);
        return ((result1 > 0) && (result2 > 0)&& (result3 > 0)&& (result4 > 0));
    }

    // Change the code of an item
    public boolean update_item_itemcode(String new_code,String code){
        Log.d(TAG,"Updating code");
        int result = DataBase.update_item(new String[]{DataBase.KEY_CODE, new_code},code);
        return result!=0;
    }

    // Set the sync flag to 1 or 0
    public boolean set_item_flag(String code, int flag){
        int result = DataBase.update_item(new String[]{DataBase.KEY_FLAG,"" + flag},code);
        return result!=0;
    }

    // Returns the code from the last item that has been added
    public String get_code_last_item(){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " +DataBase.Items_table_name +
                " WHERE ID_Item=(SELECT MAX(ID_Item) FROM " + DataBase.Items_table_name +")";
        return DataBase.read_item(query);
    }

    // Deletes an item
    public void delete_item(String code){
        DataBase.delete_item(code);
    }

    // Reads an item
    public String read_item(int value,String code){
        String table_name = DataBase.Items_table_name;
        String key,query,result;

        switch (value){
            case 0:
                key = DataBase.KEY_ID_ITEM;
                break;
            case 1:
                key = DataBase.KEY_PRODUCT;
                break;
            case 2:
                key = DataBase.KEY_TYPE;
                break;
            case 3:
                key = DataBase.KEY_QUANTITY;
                break;
            case 4:
                key = DataBase.KEY_PRICE;
                break;
            case 5:
                key = DataBase.KEY_FLAG;
                break;
            case 6:
                key = DataBase.KEY_CHANGE_TYPE;
                break;
            default:
                Log.d(TAG,"unknown value in read_item");
                return null;
        }
        query = "SELECT " + key + " FROM " + table_name + " WHERE " + DataBase.KEY_CODE + String.format("='%s'",code);
        result = DataBase.read_item(query);
        if (result == null) return "Error";
        return result;
    }

    // Reads all the items
    public List<String[]> read_all_items(String code){
        String table_name = DataBase.Items_table_name;
        String query;
        List<String[]> r = new ArrayList<>();
        Cursor result;
        query = "SELECT * FROM " + table_name+" WHERE "+DataBase.KEY_CODE_LIST+String.format("='%s'",code);
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                Log.d(TAG,"code: " + result.getString(5));
                if (!result.getString(7).equals("delete_item")) {
                    // Product, Quantity, Price, Type, Last_User, Code_item
                    String[] entry = new String[]{result.getString(1), result.getString(3), result.getString(4),
                            result.getString(2), result.getString(9), result.getString(5)};
                    r.add(entry);
                }
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e){
            e.printStackTrace();
            Log.d(TAG,"Empty DB");
        }
        return r;
    }

    // Reads all items with the sync flag set
    public List<String[]> read_all_with_flag_set_item(){
        String table_name = DataBase.Items_table_name;
        String query;
        List<String[]> r = new ArrayList<>();

        Cursor result;
        query = "SELECT * FROM " + table_name + " WHERE " +DataBase.KEY_FLAG + " = 1";
        result = DataBase.read_multiple_entries(query);
        result.moveToFirst();
        try {
            do {
                // Product, Quantity, Price, Type, Last_User, Code_item, change type, code_list
                String[] entry = new String[]{result.getString(1), result.getString(3), result.getString(4),
                        result.getString(2), result.getString(9), result.getString(5), result.getString(7), result.getString(8)};
                r.add(entry);
            } while (result.moveToNext());
        } catch (android.database.CursorIndexOutOfBoundsException e) {
            e.printStackTrace();
            Log.w(TAG, "Empty DB");
        }
        Log.d(TAG,"read_all " + r.toString());
        return r;

    }

    // Deletes all the items from one Shopping List (Called when a SHopping List is deleted)
    public void delete_all_items_of_one_list(String code_list){
        DataBase.delete_all_items(code_list);
    }

    // Reads the code from an specific item
    public String read_code_items(String productName, String quantity, String price, String type){
        String query = "SELECT " + DataBase.KEY_CODE + " FROM " + DataBase.Items_table_name
                + " WHERE " + DataBase.KEY_PRODUCT + String.format("='%s'",productName) + " AND "+DataBase.KEY_QUANTITY + String.format("='%s'",quantity) + " AND " + DataBase.KEY_PRICE + String.format("='%s'",price) + " AND " + DataBase.KEY_TYPE + String.format("='%s'",type);
        String result = DataBase.read_item(query);
        if (result == null) return "Error";
        return result;
    }
}
