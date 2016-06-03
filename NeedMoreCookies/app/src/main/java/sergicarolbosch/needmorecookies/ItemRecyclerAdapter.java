package sergicarolbosch.needmorecookies;

import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sergi on 01/06/16.
 */
public class ItemRecyclerAdapter extends RecyclerView.Adapter<ItemRecyclerAdapter.ViewHolder>  {

    private final String TAG = "ItemRecycler";
    private static List<Item> item_lists;
    private static ClickListener clickListener;

    public ItemRecyclerAdapter(List<Item> items){
        item_lists = new ArrayList<>();
        item_lists.addAll(items);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener{
        public TextView product_name;
        public TextView product_price;
        public TextView product_quantity;
        public TextView added_by;
        public View card_item;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
            product_name = (TextView) itemView.findViewById(R.id.product_name);
            product_price = (TextView) itemView.findViewById(R.id.prod_price);
            product_quantity = (TextView) itemView.findViewById(R.id.prod_quantity);
            added_by = (TextView) itemView.findViewById(R.id.added_by);
            card_item = itemView.findViewById(R.id.card_item);
        }
        @Override
        public void onClick(View v) {
            clickListener.onItemClick(item_lists.get(getAdapterPosition()), v);
        }

        @Override
        public boolean onLongClick(View v) {
            v.setBackgroundColor(Color.WHITE);
            clickListener.onItemLongClick(getAdapterPosition(), v);
            return false;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_items, parent, false);
        return new ItemRecyclerAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Item item = item_lists.get(position);
        String currency_price = item.getItem_Price() + " " +User_Info.getInstance().getCurrency();
        holder.product_name.setText(item.getItem_Name());
        holder.product_quantity.setText(item.getItem_Quantity());
        holder.product_price.setText(currency_price);
        holder.added_by.setText(item.getAdded_by());

    }

    @Override
    public int getItemCount() {
        return item_lists.size();
    }

    public void setOnItemClickListener(ClickListener clickListener) {
        ItemRecyclerAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        // Hacky way, we pass the shopping list object instead of the position
        void onItemClick(Item selected_item, View v);
        void onItemLongClick(int position, View v);
    }

    public void swap(List<Item> New_Items){
        Log.v(TAG,"Swapping contents");
        if ( item_lists!= null && New_Items != null) {
            item_lists.clear();
            item_lists.addAll(New_Items);
        }
        else item_lists = New_Items;
        notifyDataSetChanged();
    }
}
