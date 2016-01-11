package co.higheraltitude.prizm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Map;

import co.higheraltitude.prizm.R;

/**
 * Created by boonej on 9/24/15.
 */
public class HashTagCountView extends RelativeLayout {

    private TextView mTextView;
    private TextView mCountView;
    private String mHashTag;
    private String mCount;

    public static final String COUNT_KEY = "count";
    public static final String TAG_KEY = "tag";

    public static HashTagCountView inflate(ViewGroup parent) {
        HashTagCountView tagView = (HashTagCountView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.hash_tag_count_view, parent, false);
        return tagView;
    }

    public HashTagCountView(Context context){
        this(context, null);
    }

    public HashTagCountView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HashTagCountView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mTextView = (TextView)findViewById(R.id.hash_tag);
        mCountView = (TextView)findViewById(R.id.hash_count);
    }

    public void setHashTag(Map<String, String> map) {
        setViews();
        mHashTag = map.get(TAG_KEY);
        mCount = map.get(COUNT_KEY);
        mTextView.setText(mHashTag);
        mCountView.setText(mCount);

    }

    public String getHashTag() {
        return mHashTag;
    }


}
