package co.higheraltitude.prizm.handlers;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;

import co.higheraltitude.prizm.adapters.GroupAdapter;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.MessageGroupFragment;
import co.higheraltitude.prizm.models.Group;

/**
 * Created by boonej on 10/15/15.
 */
public class GroupsHandler implements PrizmDiskCache.CacheRequestDelegate {

    private Context mContext;
    private MessageGroupFragment mFragment;
    private Boolean updated = false;

    public GroupsHandler(Context context,
                              MessageGroupFragment fragment) {
        mContext = context;
        mFragment = fragment;
    }

    @Override
    public void cached(String path, Object object) {
        if (object != null && !updated) {
            process(object);
        }
    }
    @Override
    public void cacheUpdated(String path, Object object) {
        updated = true;
        process(object);
    }

    private void process(Object object) {
        try {
            if (object != null) {
                if (object instanceof ArrayList) {
                    ArrayList<Group> obj = (ArrayList<Group>)object;
                    ArrayList<Group> groups = mFragment.getGroups();
                    if (groups == null || groups.size() != obj.size()) {
                        mFragment.setGroups(obj);
                        groups = obj;
                        ArrayList<String> groupList = new ArrayList<>();
                        Iterator i = groups.iterator();
                        while (i.hasNext()) {
                            Group g = (Group)i.next();
                            groupList.add(g.name);
                        }
                        if (groupList.size() > 0) MessageGroupFragment.setGroupNames(groupList);
                        GroupAdapter adapter = mFragment.getAdapter();
                        adapter.clear();
                        adapter.addAll(groups);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.makeText(mContext, "Uh oh! There was a problem loading your groups.", Toast.LENGTH_SHORT).show();
        }
        mFragment.hideProgressBar();
    }

}
