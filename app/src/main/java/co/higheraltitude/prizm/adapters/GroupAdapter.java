package co.higheraltitude.prizm.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import co.higheraltitude.prizm.fragments.MessageGroupFragment;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.views.GroupView;

/**
 * Created by boonej on 10/15/15.
 */
public class GroupAdapter extends ArrayAdapter<Group> {

    private int [] mCounts;

    public GroupAdapter(Context c, List<Group> items) {
        super(c, 0, items);
    }

    public void setCounts(int[] counts) {
        mCounts = counts;
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GroupView groupView = (GroupView)convertView;
        if (groupView == null) {
            groupView = GroupView.inflate(parent);
        }
        if (position == 0) {
            groupView.setTitle("@direct");
        } else if (position == 1) {
            groupView.setTitle("#all");
        } else {
            groupView.setGroup(getItem(position - 2));
        }
        if (mCounts != null && mCounts.length > position) {
            groupView.setCount(mCounts[position]);
        } else {
            groupView.setCount(0);
        }
        return groupView;
    }

    public View getView(String groupId) {
        int idx = -1;
        for (int i = 0; i != getCount() - 2; ++i) {
            Group group = getItem(i);
            if (group.uniqueID.equals(groupId)) {
                idx = i;
                break;
            }
        }
        View view = null;
        if (idx != -1) {
            view = getView(idx + 2, null, null);
        }
        return view;
    }

    public int indexOf(String groupId) {
        int idx = -1;
        for (int i = 0; i != getCount() - 2; ++i) {
            Group group = getItem(i);
            if (group.uniqueID.equals(groupId)) {
                idx = i;
                break;
            }
        }
        return idx;
    }

    @Override
    public int getCount() {
        return super.getCount() + 2;
    }
}
