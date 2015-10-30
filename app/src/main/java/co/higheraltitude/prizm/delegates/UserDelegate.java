package co.higheraltitude.prizm.delegates;

import java.util.ArrayList;

import co.higheraltitude.prizm.adapters.UserAdapter;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 10/27/15.
 */
public class UserDelegate implements PrizmDiskCache.CacheRequestDelegate {

    private UserAdapter mAdapter;
    private FetchUserDelegate mDelegate;
    private boolean emptySet = true;

    public UserDelegate(UserAdapter adapter, FetchUserDelegate delegate) {
        mAdapter = adapter;
        mDelegate = delegate;
    }

    @Override
    public void cached(String path, Object object) {
        processUsers(object);
    }

    @Override
    public void cacheUpdated(String path, Object object) {
        processUsers(object);
    }

    private void processUsers(Object object) {
        ArrayList<User> users = (ArrayList<User>) object;
        if (users != null && users.size() > 0) {
            if (emptySet) {
                mAdapter.clear();
            }
            mAdapter.addAll(users);
            mAdapter.notifyDataSetChanged();
            mDelegate.loadMembers();
        }
    }

    public interface FetchUserDelegate {
        void loadMembers();
    }

}
