package sersilinc.needmorecookies;

import android.util.Log;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by sergi on 23/05/16.
 */
public class Shopping_List{

    private String[] color_pool = new String[]{
            "#f44336", // Red
            "#673ab7", // Deep Purple
            "#673ab7", // Indigo
            "#2196f3", // Blue
            "#26c6da", // Greenish Blue
            "#0288d1", // Darker Blue
            "#009688", // Teal
            "#8bc34a", // Light Green
            "#00e676", // Lighter Green
            "#ffeb3b", // Yellow
            "#f9a825", // Yellow/Orange
            "#ff9800", // Orange
            "#607d8b"  // Blue Grey
    };

    private String List_Name;
    private String Timestamp;
    private boolean favourite = false;
    private boolean Is_Private;
    private String color;
    private String Shopping_List_Code;

    public Shopping_List(){}

    public Shopping_List(String name,String timestamp,String last_User,boolean fav,boolean is_private,String shopping_List_Code){
        super();
        this.List_Name = name;
        this.Timestamp = timestamp;
        this.favourite = fav;
        this.Is_Private = is_private;
        this.color =  color_pool[new Random().nextInt(12)];
        this.Shopping_List_Code = shopping_List_Code;
        Log.v("Shopping_List_class", "Shopping List added");
    }

    public void setList_Name(String list_name){
        List_Name = list_name;
    }

    public String getList_Name() {
        return List_Name;
    }

    public void setTimestamp(String timestamp) {
        Timestamp = timestamp;
    }

    public String getTimestamp() {
        return Timestamp;
    }

    public void setFavourite(boolean favourite) {
        this.favourite = favourite;
    }

    public boolean isFavourite() {
        return favourite;
    }

    public void setIs_Private(boolean is_Private) {
        Is_Private = is_Private;
    }

    public boolean is_Private() {
        return Is_Private;
    }

    public String getColor() {
        return color;
    }

    public void setShopping_List_Code(String shopping_List_Code) {
        Shopping_List_Code = shopping_List_Code;
    }

    public String getShopping_List_Code() {
        return Shopping_List_Code;
    }

    public String format(){
        return "Name: " + this.List_Name + " Time: " + this.Timestamp;
    }

}
