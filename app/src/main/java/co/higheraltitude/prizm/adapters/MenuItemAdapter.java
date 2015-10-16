package co.higheraltitude.prizm.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.higheraltitude.prizm.views.MenuItemView;

/**
 * Created by boonej on 10/15/15.
 */
public class MenuItemAdapter extends ArrayAdapter<String> {

    private int mSelectedItem;

    public MenuItemAdapter(Context c, List<String> items) {
        super(c, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MenuItemView itemView = (MenuItemView)convertView;
        if (itemView == null) {
            itemView = MenuItemView.inflate(parent);
        }
        itemView.setText(getItem(position));

        itemView.setItemSelected(position == mSelectedItem);


        return itemView;
    }

    public void setSelectedItem(int position) {
        mSelectedItem = position;
        notifyDataSetChanged();
    }

}
