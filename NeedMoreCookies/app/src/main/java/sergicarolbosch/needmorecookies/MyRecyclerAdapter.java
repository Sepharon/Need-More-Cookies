package sergicarolbosch.needmorecookies;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergi on 23/05/16.
 */
public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.ViewHolder> {

    private final String TAG = "Recycler";
    private static List<Shopping_List> sl;
    private static ClickListener clickListener;
    private static boolean previous_fav_status = false;

    public MyRecyclerAdapter(List<Shopping_List> shopping_lists) {
        sl = new ArrayList<Shopping_List>();
        sl.addAll(shopping_lists);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public TextView titleText;
        public TextView contentText;
        public CardView card;
        public CheckBox fav_icon;
        public View color_view;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            titleText = (TextView) itemView.findViewById(R.id.title);
            contentText = (TextView) itemView.findViewById(R.id.hexValue);
            card = (CardView) itemView.findViewById(R.id.card);
            fav_icon = (CheckBox) itemView.findViewById(R.id.star);
            //color_view = (View) itemView.findViewById(R.id.back);
            fav_icon.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            // -1 = no change ; 0 = changed to false; 1 = changed to true
            int is_fav_checked = -1;

            if (fav_icon.isChecked() != sl.get(getAdapterPosition()).isFavourite()){
                Log.v("Shopping","STARTING FAV");
                print_sl();
                Log.v("Shopping","Fav icon has changed!");
                Log.v("Shopping","SHopping List: " + sl.toString());
                Log.v("Shopping","List name: " + sl.get(getAdapterPosition()).getList_Name());
                previous_fav_status = fav_icon.isChecked();
                sl.get(getAdapterPosition()).setFavourite(fav_icon.isChecked());
                is_fav_checked = ((fav_icon.isChecked()) ? 1 : 0);
            }
            Log.v("Shopping","Fav checked value: " + is_fav_checked);
            //sl.get(getAdapterPosition()).setFavourite(fav_icon.isChecked());
            clickListener.onItemClick(sl.get(getAdapterPosition()), v, is_fav_checked);
        }

        @Override
        public boolean onLongClick(View v) {
            v.setBackgroundColor(Color.WHITE);
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
        void print_sl(){
            for (int i = 0; i < sl.size(); i++)
                Log.v("Shopping","Name: " + sl.get(i).getList_Name() + " Code: " + sl.get(i).getShopping_List_Code());
        }
    }
    @Override
    public MyRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view, parent, false);
        return new MyRecyclerAdapter.ViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(ViewHolder listViewHolder, int position) {
        Log.v(TAG,"Inside adapter");
        Shopping_List shopping_list = sl.get(position);
        listViewHolder.titleText.setText(shopping_list.getList_Name());
        listViewHolder.contentText.setText(shopping_list.getTimestamp());
        listViewHolder.card.setBackgroundColor(Color.WHITE);
        Log.v(TAG,shopping_list.isFavourite()+ "");
        listViewHolder.fav_icon.setChecked(shopping_list.isFavourite());
        Log.v(TAG,listViewHolder.fav_icon.isChecked() + "");
        previous_fav_status = shopping_list.isFavourite();
        //listViewHolder.color_view.setBackgroundColor(Color.parseColor(shopping_list.getColor()));
    }

    @Override
    public int getItemCount() {
        return sl.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        MyRecyclerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        // Hacky way, we pass the shopping list object instead of the position
        void onItemClick(Shopping_List sl, View v,int icon_status);
        void onItemLongClick(int position, View v);
    }

    public void swap(List<Shopping_List> New_Shopping_List){
        Log.v(TAG,"Swapping contents");
        Log.v(TAG,sl.toString());
        if (sl != null && New_Shopping_List != null) {
            sl.clear();
            Log.v(TAG,sl.toString());
            Log.v(TAG,"Shopping List empty: " + sl.isEmpty());
            sl.addAll(New_Shopping_List);
        }
        else sl = New_Shopping_List;
        notifyDataSetChanged();
    }
}
