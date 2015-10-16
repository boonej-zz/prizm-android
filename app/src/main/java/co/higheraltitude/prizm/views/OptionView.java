package co.higheraltitude.prizm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.higheraltitude.prizm.R;

/**
 * Created by boonej on 9/24/15.
 */
public class OptionView extends RelativeLayout {

    private TextView mTextView;
    private String mText;

    public static OptionView inflate(ViewGroup parent) {
        OptionView tagView = (OptionView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.option_view, parent, false);
        return tagView;
    }

    public OptionView(Context context){
        this(context, null);
    }

    public OptionView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OptionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mTextView = (TextView)findViewById(R.id.option_text);
    }

    public void setText(String text) {
        setViews();
        mText = text;
        mTextView.setText(text);
    }

    public String getHashTag() {
        return mText;
    }


}
