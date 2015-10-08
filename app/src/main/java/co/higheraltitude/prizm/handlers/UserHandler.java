package co.higheraltitude.prizm.handlers;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;

import co.higheraltitude.prizm.adapters.UserAdapter;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 10/7/15.
 */
public class UserHandler extends Handler {

    private UserAdapter mAdapter;
    private UserHandlerDelegate mDelegate;

    public UserHandler(UserAdapter adapter, UserHandlerDelegate delegate) {
        mAdapter = adapter;
        mDelegate = delegate;
    }

    @Override
    public void handleMessage(Message msg){
        ArrayList<User> u = (ArrayList<User>)msg.obj;
        if (u.size() > 0) {
            mAdapter.addUsers(u);
            mAdapter.notifyDataSetInvalidated();
            mAdapter.notifyDataSetChanged();
            mDelegate.loadMembers();
        }
    }

    public interface UserHandlerDelegate {
        void loadMembers();
    }
}