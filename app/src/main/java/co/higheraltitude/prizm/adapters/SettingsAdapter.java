package co.higheraltitude.prizm.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.higheraltitude.prizm.views.MenuItemView;
import co.higheraltitude.prizm.views.SettingsItemView;

/**
 * Created by boonej on 10/15/15.
 */
public class SettingsAdapter extends ArrayAdapter<String> {

    public SettingsAdapter(Context c, List<String> items) {
        super(c, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SettingsItemView itemView = (SettingsItemView)convertView;
        if (itemView == null) {
            itemView = SettingsItemView.inflate(parent);
        }
        itemView.setText(getItem(position));
        itemView.setDisclosure(position < 6);

        return itemView;
    }


}
