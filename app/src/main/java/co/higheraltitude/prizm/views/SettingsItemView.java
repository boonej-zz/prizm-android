package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.UUID;

import co.higheraltitude.prizm.R;

/**
 * Created by boonej on 9/24/15.
 */
public class SettingsItemView extends RelativeLayout {

    private TextView mTextView;
    private ImageView mImageView;

    private Boolean mSelected = false;

    private String instanceId;

    public static SettingsItemView inflate(ViewGroup parent) {
        SettingsItemView itemView = (SettingsItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_item_view, parent, false);
        return itemView;
    }

    public SettingsItemView(Context context){
        this(context, null);
    }

    public SettingsItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SettingsItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mImageView = (ImageView)findViewById(R.id.disclosure);
        mTextView = (TextView)findViewById(R.id.menu_item_text);
    }

    public void setText(String text) {
        setViews();
        mTextView.setText(text);
        instanceId = UUID.randomUUID().toString();
    }

    public void setDisclosure(Boolean show) {
        if (show) {
            mImageView.setVisibility(VISIBLE);
        } else {
            mImageView.setVisibility(INVISIBLE);
        }
    }


}
