package sersilinc.needmorecookies;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * This class overrides the method for ListView adapters in order to get a custom adapter.
 * REFERENCE: http://techlovejump.com/android-multicolumn-listview/
 */

public class ListViewAdapters extends BaseAdapter{

    //Columns of the list view
    private static final String FIRST_COLUMN="First";
    private static final String SECOND_COLUMN="Second";
    private static final String THIRD_COLUMN="Third";
    private static final String FOURTH_COLUMN="Fourth";

    //List
    private ArrayList<HashMap<String, String>> list;

    //UI elements
    private Activity activity;
    private String source;
    private String type;

    /**
     * Class constructor.
     * @param activity activity
     * @param list List
     * @param source Source
     * @param type Public or private
     */
    public ListViewAdapters(Activity activity,ArrayList<HashMap<String, String>> list, String source, String type){
        super();
        this.activity=activity;
        this.list=list;
        this.source=source;
        this.type=type;
    }

    /**
     * Override getCount method.
     * @return Return size
     */
    @Override
    public int getCount() {
        return list.size();
    }

    /**
     * Override getItem method.
     * @param position position
     * @return Return item
     */
    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    /**
     * Override getItemId method.
     * @param position position
     * @return return ID
     */
    @Override
    public long getItemId(int position) {
        return 0;
    }


    /**
     * Override getView method. Depeding on the parameters, the listview will look different.
     * @param position position
     * @param convertView View
     * @param parent parent
     * @return return view
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater=activity.getLayoutInflater();

        if(convertView == null){

            //Check if the List View is the header or the content from the Items layout or its from the MainActivity layout.
            switch(source){
                case "Content":
                    convertView = inflater.inflate(R.layout.row_view, null);
                    break;
                case "Header":
                    convertView = inflater.inflate(R.layout.row_header, null);
                    break;
                case "MainActivity":
                    convertView = inflater.inflate(R.layout.row_main, null);
                    break;
            }

            //If it is the Items layout
            if (!source.equals("MainActivity")) {
                //Get UI elements
                TextView txtFirst = (TextView) convertView.findViewById(R.id.product);
                TextView txtSecond = (TextView) convertView.findViewById(R.id.quantity);
                TextView txtThird = (TextView) convertView.findViewById(R.id.price);

                //If it is a public list, show the user who has added the product, else does not show it.
                if (type.equals("0")) {
                    TextView txtFourth = (TextView) convertView.findViewById(R.id.last_user_entry);
                    TextView last_user = (TextView) convertView.findViewById(R.id.last_user);
                    txtFourth.setVisibility(View.VISIBLE);
                    last_user.setVisibility(View.VISIBLE);
                    //Write to UI elements
                    HashMap<String, String> map = list.get(position);
                    txtFirst.setText(map.get(FIRST_COLUMN));
                    txtSecond.setText(map.get(SECOND_COLUMN));
                    txtThird.setText(map.get(THIRD_COLUMN));
                    txtFourth.setText(map.get(FOURTH_COLUMN));
                } else {
                    //Write to UI elements
                    HashMap<String, String> map = list.get(position);
                    txtFirst.setText(map.get(FIRST_COLUMN));
                    txtSecond.setText(map.get(SECOND_COLUMN));
                    txtThird.setText(map.get(THIRD_COLUMN));

                }


            }
            //If it is the MainActivity layout
            else {
                TextView txtFirst = (TextView) convertView.findViewById(R.id.shop_list);
                TextView txtSecond = (TextView) convertView.findViewById(R.id.last_added);
                //Write to UI elements
                HashMap<String, String> map = list.get(position);
                txtFirst.setText(map.get(FIRST_COLUMN));
                txtSecond.setText(map.get(SECOND_COLUMN));
            }

        }

        return convertView;
    }
}