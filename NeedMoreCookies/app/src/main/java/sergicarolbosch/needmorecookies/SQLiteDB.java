package sergicarolbosch.needmorecookies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.UUID;

/**
 * The aim of this class is to create the internal DB and to implement some method to help
 * working with it.
 */

public class SQLiteDB extends SQLiteOpenHelper{

    private final String TAG = "SQLiteDB";

    // DataBase information
    private static int DATABASE_VERSION = 1;
    private static String DATABASE_NAME = "Shopping_Lists";

    // Tables names
    public final String Shopping_list_table_name = "Shopping_List";
    public final String Items_table_name = "Items";

    // Table values
    // Shopping List
    public final String KEY_ID_LIST = "ID_List";
    public final String KEY_LIST_NAME = "List_Name";
    public final String KEY_UPDATE = "Last_Update";
    public final String KEY_PUBLIC = "Private";
    public final String KEY_FAVOURITE = "Favourite";

    // Items
    public final String KEY_ID_ITEM = "ID_Item";
    public final String KEY_PRODUCT = "Product";
    public final String KEY_TYPE = "Type";
    public final String KEY_QUANTITY = "Quantity";
    public final String KEY_PRICE = "Price";
    public final String KEY_CODE_LIST = "Code_List";
    public final String KEY_LAST_USER = "Last_User";

    // Others
    public final String KEY_CODE = "Code";
    public final String KEY_FLAG = "Flag";
    public final String KEY_CHANGE_TYPE = "Change_type";

    public SQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Database classes are special, this onCreate method will only be called if there is no
    // database in the device instead of being called every time the app starts
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG,"Creating DB");
        // Prepare query for first table
        String SHOPPING_LIST_TABLE = "CREATE TABLE " + Shopping_list_table_name + " ( " +
                "ID_List INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "List_Name TEXT NOT NULL, " +
                "Last_Update TEXT NOT NULL, " +
                "Code TEXT UNIQUE, " +
                "Private INTEGER, " +
                "Flag INTEGER DEFAULT 1, " +
                "Favourite INTEGER DEFAULT 0, " +
                "Change_type TEXT)";
        // Create table
        db.execSQL(SHOPPING_LIST_TABLE);
        // Prepare query for second table
        String ITEMS_TABLE = "CREATE TABLE " + Items_table_name + " ( " +
                "ID_Item INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Product TEXT NOT NULL, " +
                "Type TEXT NOT NULL, " +
                "Quantity TEXT NOT NULL, " +
                "Price TEXT, " +
                "Code TEXT UNIQUE, " +
                "Flag INTEGER DEFAULT 0, " +
                "Change_type TEXT, "+
                "Code_List TEXT NOT NULL, "+
                "Last_User TEXT," +
                " FOREIGN KEY (Code_List) REFERENCES " + Shopping_list_table_name + " (" + KEY_CODE +
                ") ON DELETE CASCADE);";
        db.execSQL(ITEMS_TABLE);
    }

    // This method is called when we wish to upgrade the DB
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG,"THIS WILL DELETE THE CONTENTS OF THE DB");
        db.execSQL("DROP TABLE IF EXISTS " + Shopping_list_table_name);
        db.execSQL("DROP TABLE IF EXISTS " + Items_table_name);
        onCreate(db);
        DATABASE_VERSION = newVersion;
    }

    // This method is called every time we open the DB, it allows to use the delete cascade keyword
    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            // Enable foreign key constraints
            db.execSQL("PRAGMA foreign_keys=ON;");
        }
    }

    // Add a new list to the DB
    public long add_new_list(String list_name,int Public){
        // Get current date
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        Log.d(TAG,"Creating new list at " + timeStamp);
        Log.d(TAG,"With name: " + list_name);
        // Create a DB connection
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // Put the values
        values.put(KEY_LIST_NAME,list_name);
        values.put(KEY_UPDATE,timeStamp);
        if (User_Info.getInstance().getOffline_mode()) {
            Log.d(TAG,"We are offline");
            values.put(KEY_FLAG, 1);
        }
        else
            values.put(KEY_FLAG,0);
        // We generate a temporary code,
        // this code will be changed once the list is sent to the server
        values.put(KEY_CODE, UUID.randomUUID().toString().replaceAll("-", ""));
        values.put(KEY_PUBLIC,Public);
        values.put(KEY_CHANGE_TYPE,"new_list");
        // Insert values
        // Begin transaction
        db.beginTransaction();
        long newRowID = db.insert(Shopping_list_table_name,null,values);
        // Commit changes
        db.setTransactionSuccessful();
        // End transaction
        db.endTransaction();
        db.close();
        values.clear();
        return newRowID;
    }

    // Update list name, the key_value array contains the key and the value to update from the DB
    public int update_list(String[] key_value,String code){
        Log.d(TAG,"Updating list: " + code);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(key_value[0], key_value[1]);
        db.beginTransaction();
        int count = db.update(
                Shopping_list_table_name,
                values,
                KEY_CODE+" = ?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        values.clear();
        Log.d(TAG,"count: " + count);
        // Return the number of row affected
        return count;
    }

    // Delete list
    public void delete_list(String code){
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        db.delete(Shopping_list_table_name,
                KEY_CODE + "= ?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

    }
    // Reads the values from shopping list table
    public String read_shopping_lists(String query){
        String result;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);

        if (cursor == null) return null;
        cursor.moveToFirst();
        try {
            // Get the result
        result = cursor.getString(0);
        } catch(android.database.CursorIndexOutOfBoundsException e){
            // If the item does not exist return null
            return null;
        } finally {
            // Finally close the cursor
            cursor.close();
            db.close();
        }
        return result;
    }

    // Read multiple values from the DB, return a cursor which contains all the values.
    public Cursor read_multiple_entries(String query){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        if (cursor == null) return null;
        return cursor;
    }

    // Add a new item to the DB
    public long add_new_item(String Product, String Type, String Quantity, String Price, String Code_list, String user){
        long newRowID;

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_CODE, UUID.randomUUID().toString().replaceAll("-", ""));
        values.put(KEY_PRODUCT,Product);
        values.put(KEY_TYPE,Type);
        values.put(KEY_QUANTITY,Quantity);
        values.put(KEY_PRICE,Price);
        values.put(KEY_CODE_LIST, Code_list);
        values.put(KEY_LAST_USER, user);
        values.put(KEY_CHANGE_TYPE,"new_item");
        if (User_Info.getInstance().getOffline_mode()) {
            Log.d(TAG,"We are offline");
            values.put(KEY_FLAG, 1);
        }
        else
            values.put(KEY_FLAG,0);

        db.beginTransaction();
        newRowID = db.insert(Items_table_name,null,values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        values.clear();
        return newRowID;
    }

    // Update an item
    public int update_item(String[] key_value,String code){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        Log.d(TAG,"Updating " + key_value[0] + " with " + key_value[1]);
        values.put(key_value[0],key_value[1]);
        db.beginTransaction();
        int count = db.update(Items_table_name,
                values,
                KEY_CODE + "=?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        values.clear();
        db.close();
        return count;
    }

    // Delete an item
    public void delete_item(String code){
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        db.delete(Items_table_name,
                KEY_CODE + "= ?",
                new String[]{code});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    // Delete all items of a shopping list
    public void delete_all_items(String code_list){
        SQLiteDatabase db = this.getWritableDatabase();

        db.beginTransaction();
        db.delete(Items_table_name,
                KEY_CODE_LIST + "= ?",
                new String[]{code_list});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    // Reads the values from item table
    public String read_item(String query){
        String result;

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query,null);

        if (cursor == null) return null;
        cursor.moveToFirst();
        try {
            result = cursor.getString(0);
        } catch(android.database.CursorIndexOutOfBoundsException e){
            return null;
        } finally {
            cursor.close();
        }
        return result;
    }
}
