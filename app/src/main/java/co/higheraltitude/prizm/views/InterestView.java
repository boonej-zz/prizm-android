package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.models.Interest;

/**
 * Created by boonej on 9/24/15.
 */
public class InterestView extends RelativeLayout implements View.OnClickListener {

    private InterestViewDelegate mDelegate;

    private TextView mTextView1;
    private TextView mTextView2;
    private TextView mTextView3;
    private ArrayList<Interest> mInterests;

    public static InterestView inflate(ViewGroup parent) {
        InterestView tagView = (InterestView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.interest_view, parent, false);
        return tagView;
    }

    public InterestView(Context context){
        this(context, null);
    }

    public InterestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InterestView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDelegate(InterestViewDelegate delegate) {
        mDelegate = delegate;
    }

    private void setViews() {
        mTextView1 = (TextView)findViewById(R.id.interest_1);
        mTextView1.setOnClickListener(this);
        mTextView2 = (TextView)findViewById(R.id.interest_2);
        mTextView2.setOnClickListener(this);
        mTextView3 = (TextView)findViewById(R.id.interest_3);
        mTextView3.setOnClickListener(this);
    }

    public void onClick(View view) {
        int position = 0;
        if (view == mTextView1) {
            position = 0;
        } else if (view == mTextView2) {
            position = 1;
        } else if (view == mTextView3) {
            position = 2;
        }
        setSelected(position);
        if (mDelegate != null) {
            mDelegate.interestClicked(mInterests.get(position));
        }
    }

    public void setSelected(int position) {
        Interest interest = mInterests.get(position);
        interest.selected = !interest.selected;
        TextView view = null;
        switch (position) {
            case 0:
                view = mTextView1;
                break;
            case 1:
                view = mTextView2;
                break;
            case 2:
                view = mTextView3;
                break;
            default:
                break;
        }
        layoutSelection(interest, view);

    }

    private void layoutSelection(Interest interest, TextView view) {
        if (view != null) {
            if (interest.selected) {
                view.setBackgroundResource(R.drawable.bkg_interest_selected);
                view.setTextColor(Color.parseColor("#ffffff"));
            } else {
                view.setBackgroundResource(R.drawable.bkg_interest);
                view.setTextColor(Color.parseColor("#334b8f"));
            }
        }
    }

    public void setInterests(ArrayList<Interest> list) {
        setViews();
        mInterests = list;
        for (int i = 0; i != list.size(); ++i) {
            switch(i) {
                case 0:
                    mTextView1.setText(list.get(i).text);
                    layoutSelection(list.get(i), mTextView1);
                    break;
                case 1:
                    mTextView2.setText(list.get(i).text);
                    layoutSelection(list.get(i), mTextView2);
                    break;
                case 2:
                    mTextView3.setText(list.get(i).text);
                    layoutSelection(list.get(i), mTextView3);
                    break;
                default:
                    break;
            }
        }
    }

    public interface InterestViewDelegate {
        void interestClicked(Interest interest);
    }



}
