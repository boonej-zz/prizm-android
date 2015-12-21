package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class MenuItemView extends RelativeLayout {

    private TextView mTextView;
    private ImageView mImageView;
    private TextView mBadgeView;

    private Boolean mSelected = false;

    private String instanceId;

    public static MenuItemView inflate(ViewGroup parent) {
        MenuItemView itemView = (MenuItemView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.menu_item_view, parent, false);
        return itemView;
    }

    public MenuItemView(Context context){
        this(context, null);
    }

    public MenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MenuItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mImageView = (ImageView)findViewById(R.id.menu_icon);
        mTextView = (TextView)findViewById(R.id.menu_item_text);
        mBadgeView = (TextView)findViewById(R.id.menu_badge);
    }

    public void setText(String text) {
        setViews();
        mTextView.setText(text);
        setImage();
        instanceId = UUID.randomUUID().toString();
    }

    public void setItemSelected(boolean selected) {
        super.setSelected(selected);
        mSelected = selected;
        if (mSelected) {
            mTextView.setTextColor(Color.parseColor("#5188B9"));
        } else {
            mTextView.setTextColor(Color.parseColor("#7C7C7C"));
        }
        setImage();
    }

    public void setBadgeCount(int count) {
        if (count > 0) {
            mBadgeView.setVisibility(VISIBLE);
            mBadgeView.setText(String.valueOf(count));
        } else {
            mBadgeView.setVisibility(GONE);
        }
    }

    private int setImage() {
        int item = 0;
        String text = mTextView.getText().toString();
        if (text.equals("Home")) {
            if (mSelected) mImageView.setImageResource(R.drawable.home_icon_selected);
            else mImageView.setImageResource(R.drawable.home_icon);
        } else if (text.equals("Explore")) {
            if (mSelected) mImageView.setImageResource(R.drawable.explorer_icon_selected);
            else mImageView.setImageResource(R.drawable.explore_icon);
        } else if (text.equals("Insight")) {
            if (mSelected) mImageView.setImageResource(R.drawable.insight_icon_selected);
            else mImageView.setImageResource(R.drawable.insight_icon);
        } else if (text.equals("Graph")) {
            if (mSelected) mImageView.setImageResource(R.drawable.graph_icon_selected);
            else mImageView.setImageResource(R.drawable.stats_icon);
        } else if (text.equals("Message")) {
            if (mSelected) mImageView.setImageResource(R.drawable.message_icon_selected);
            else mImageView.setImageResource(R.drawable.message_icon);
        } else if (text.equals("Survey")) {
            if (mSelected) mImageView.setImageResource(R.drawable.survey_icon_selected);
            else mImageView.setImageResource(R.drawable.survey_icon);
        } else if (text.equals("Calendar")) {
            if (mSelected) mImageView.setImageResource(R.drawable.calendar_icon_selected);
            else mImageView.setImageResource(R.drawable.calendar_icon);
        } else if (text.equals("Settings")) {
            if (mSelected) mImageView.setImageResource(R.drawable.settings_icon_selected);
            else mImageView.setImageResource(R.drawable.settings_icon);
        } else if (text.equals("Help & feedback")) {
            if (mSelected) mImageView.setImageResource(R.drawable.help_icon_selected);
            else mImageView.setImageResource(R.drawable.help_icon);
        }

        if (text.equals("Settings")) {
            getRootView().setBackgroundResource(R.drawable.settings_item_background);
        } else {
            getRootView().setBackground(null);
        }

        return item;
    }

}
