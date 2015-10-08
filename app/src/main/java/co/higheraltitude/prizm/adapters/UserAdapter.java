package co.higheraltitude.prizm.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserAvatarView;

/**
 * Created by boonej on 10/7/15.
 */
public class UserAdapter extends ArrayAdapter<User> {
    private UserNameFilter filter = new UserNameFilter();
    public ArrayList<User> baseList;
    private ArrayList<User> userList;
    public UserAdapterDelegate mDelegate;

    public UserAdapter(Context c, List<User> items){
        super(c, 0, items);
        baseList = new ArrayList<>();
        baseList.addAll(items);
        userList = new ArrayList<>();
        userList.addAll(items);
    }

    public UserAdapter(Context c, List<User> items, UserAdapterDelegate delegate){
        super(c, 0, items);
        mDelegate = delegate;
        baseList = new ArrayList<>();
        baseList.addAll(items);
        userList = new ArrayList<>();
        userList.addAll(items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserAvatarView userTagView = (UserAvatarView) convertView;
        if (userTagView == null) {
            userTagView = UserAvatarView.inflate(parent);
        }
        if (mDelegate != null) {
            if (!userTagView.isSelectable()) {
                userTagView.setSelectable(true);
            }
        }

        User user = getItem(position);

        userTagView.setUser(user);
        if (mDelegate != null) {
            userTagView.setSelected(mDelegate.containsUser(user));
        }
        return userTagView;
    }

    public void addUsers(List<User> users){
        addAll(users);
        baseList.addAll(users);
    }

    public User getUser(int position) {
        return getItem(position);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    private class UserNameFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            Iterator<User> iterator = baseList.iterator();
            ArrayList<User> filt = new ArrayList<>();
            if (constraint.length() > 0) {
                while (iterator.hasNext()) {
                    User u = iterator.next();
                    if (u.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filt.add(u);
                    }
                }
            } else {
                filt.addAll(baseList);
            }
            results.values = filt;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            userList = (ArrayList<User>)results.values;
            if (userList == null) {
                userList = new ArrayList<>();
            }
            notifyDataSetChanged();
            clear();
            Iterator<User> iterator = userList.iterator();
            while (iterator.hasNext()) {
                add(iterator.next());
            }
            notifyDataSetInvalidated();
        }
    }

    public interface UserAdapterDelegate {
        boolean containsUser(User u);
    }
}
