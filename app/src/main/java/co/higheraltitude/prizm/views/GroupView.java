package co.higheraltitude.prizm.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.models.Group;

/**
 * TODO: document your custom view class.
 */
public class GroupView extends RelativeLayout {

    private TextView mTitleTextView;
    private View mCountBadgeView;
    private TextView mCount;


    public static GroupView inflate(ViewGroup parent) {
        GroupView groupView = (GroupView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_view, parent, false);
        return groupView;
    }

    public GroupView(Context context){
        this(context, null);
    }

    public GroupView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.group_view_children, this, true);
        mTitleTextView = (TextView)findViewById(R.id.group_view_title);
        mCountBadgeView = findViewById(R.id.message_count_badge);
        mCount = (TextView)findViewById(R.id.message_count);
    }

    public void setGroup(Group group) {
        if (group instanceof Group) {
            Group g = group;
            String name = "#" + g.name.toLowerCase();
            mTitleTextView.setText(name);
        }
    }

    public void setCount(int count) {
        if (count > 0) {
            mCountBadgeView.setVisibility(VISIBLE);
            mCount.setText(String.valueOf(count));
        }
    }

    public void setTitle(String title) {
        mTitleTextView.setText(title);
    }


}
