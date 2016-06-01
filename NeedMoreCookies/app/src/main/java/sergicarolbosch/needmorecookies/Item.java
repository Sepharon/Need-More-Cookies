package sergicarolbosch.needmorecookies;

import android.util.Log;

/**
 * Created by sergi on 01/06/16.
 */
public class Item {

    private final String TAG = "Item_Class";

    private String Item_Name;
    private String Item_Quantity;
    private String Item_Price;
    private String Item_Type;
    private String Added_by;
    private String Item_Code;

    public Item(){}

    public Item(String item_Name, String item_Quantity, String item_Price, String added_by, String item_Type,String item_Code){
        Log.v(TAG,"Added new Item");
        this.Item_Name = item_Name;
        this.Item_Quantity = item_Quantity;
        this.Item_Price = item_Price;
        this.Item_Type = item_Type;
        this.Item_Code = item_Code;
        this.Added_by = added_by;
    }

    public String getItem_Name(){
        return Item_Name;
    }

    public void setItem_Name(String item_Name){
        Item_Name = item_Name;
    }

    public String getItem_Quantity(){
        return Item_Quantity;
    }

    public void setItem_Quantity(String item_Quantity){
        Item_Quantity = item_Quantity;
    }

    public String getItem_Price(){
        return Item_Price;
    }

    public void setItem_Price(String item_Price){
        Item_Price = item_Price;
    }

    public String getItem_Type(){
        return  Item_Type;
    }

    public void setItem_Type(String item_Type){
        Item_Type = item_Type;
    }

    public String getAdded_by(){
        return Added_by;
    }

    public void setAdded_by(String added_by){
        Added_by = added_by;
    }

    public String getItem_Code(){
        return Item_Code;
    }

    public void setItem_Code(String item_Code){
        this.Item_Code = item_Code;
    }
}
