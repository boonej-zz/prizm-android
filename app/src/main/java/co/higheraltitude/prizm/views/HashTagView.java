package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class HashTagView extends RelativeLayout {

    private TextView mTextView;
    private String mHashTag;

    public static HashTagView inflate(ViewGroup parent) {
        HashTagView tagView = (HashTagView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hash_tag_view, parent, false);
        return tagView;
    }

    public HashTagView(Context context){
        this(context, null);
    }

    public HashTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HashTagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mTextView = (TextView)findViewById(R.id.hash_tag);
    }

    public void setHashTag(String hashTag) {
        setViews();
        mHashTag = hashTag;
        mTextView.setText(mHashTag);
    }

    public String getHashTag() {
        return mHashTag;
    }


}
