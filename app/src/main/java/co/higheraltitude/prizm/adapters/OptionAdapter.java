package co.higheraltitude.prizm.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.OptionView;

/**
 * Created by boonej on 10/15/15.
 */
public class OptionAdapter extends ArrayAdapter<String> {

    public OptionAdapter(Context c, List<String> items){
        super(c, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        OptionView view = (OptionView) convertView;
        if (view == null) {
            view = OptionView.inflate(parent);
        }


        String text = getItem(position);

        view.setText(text);
        return view;
    }

}
