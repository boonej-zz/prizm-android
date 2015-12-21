package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserFollowingAvatarView;

public class FollowActivity extends AppCompatActivity
        implements UserFollowingAvatarView.UserAvatarViewDelegate {

    public static final String EXTRA_VIEW_TYPE = "extra_view_type";
    public static final String EXTRA_USER = "extra_user";
    public static final int VIEW_TYPE_FOLLOWING = 0;
    public static final int VIEW_TYPE_FOLLOWERS = 1;

    private int mViewType;
    private User mUser;

    private ProgressBar mProgressBar;
    private ListView mListView;
    private FollowUserAdapter mUserAdapter;

    private Boolean isUpdating = false;
    private int lastVisibleItem;
    private boolean scrollingDown;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        loadIntent();
        loadActionBar();
        configureViews();
        loadUsers();
    }

    protected void loadIntent() {
        Intent intent = getIntent();
        mViewType = intent.getIntExtra(EXTRA_VIEW_TYPE, 0);
        mUser = intent.getParcelableExtra(EXTRA_USER);
    }

    protected void loadActionBar() {
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        switch (mViewType) {
            case VIEW_TYPE_FOLLOWING:
                setTitle("Following");
                break;
            case VIEW_TYPE_FOLLOWERS:
                setTitle("Followers");
                break;
            default:
                break;
        }
    }

    protected void configureViews() {
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mListView = (ListView)findViewById(R.id.list_view);
        mUserAdapter = new FollowUserAdapter(getApplicationContext(), new ArrayList<User>());
        mListView.setAdapter(mUserAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int firstPosition = view.getFirstVisiblePosition();
                lastVisibleItem = firstPosition;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                scrollingDown = lastVisibleItem < firstVisibleItem;
                if (scrollingDown) {
                    if (view.getLastVisiblePosition() >= view.getCount() - 4) {
                        loadUsers();
                    }
                }
            }
        });
    }

    protected void loadUsers(){
        if (!isUpdating) {
            isUpdating = true;
            showProgressBar();
            int skip = mUserAdapter.getCount();
            switch (mViewType) {
                case VIEW_TYPE_FOLLOWING:
                    User.fetchFollowing(mUser.uniqueID, skip, new FollowingDelegate());
                    break;
                case VIEW_TYPE_FOLLOWERS:
                    User.fetchFollowers(mUser.uniqueID, skip, new FollowingDelegate());
                    break;
                default:
                    break;
            }
        }
    }

    protected void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);

    }

    protected void hideProgressBar() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void avatarViewClicked(UserFollowingAvatarView view) {
        User user = view.getUser();
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }

    @Override
    public void followButtonClicked(User user) {
        if (user.isFollowing) {
            User.unfollowUser(user.uniqueID, new FollowHandler(mUserAdapter));
        } else {
            User.followUser(user.uniqueID, new FollowHandler(mUserAdapter));
        }
    }

    private class FollowUserAdapter extends ArrayAdapter<User> {

        public FollowUserAdapter(Context c, List<User> items){
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserFollowingAvatarView view = (UserFollowingAvatarView)convertView;
            if (view == null) {
                view = UserFollowingAvatarView.inflate(parent);
            }
            view.setDelegate(FollowActivity.this);

            User user = getItem(position);

            view.setUser(user);
            return view;
        }
    }

    private class FollowingDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            update(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            update(object);
            isUpdating = false;
            hideProgressBar();
        }

        private void update(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<User> users = (ArrayList<User>)object;
                int length = mUserAdapter.getCount();
                for (User u : users) {
                    boolean updated = false;
                    for (int i = 0; i != length; ++i) {
                        User o = mUserAdapter.getItem(i);
                        if (o.uniqueID.equals(u.uniqueID)) {
                            updated = true;
                            mUserAdapter.remove(o);
                            mUserAdapter.insert(u, i);
                            break;
                        }
                    }
                    if (!updated) {
                        mUserAdapter.add(u);
                    }
                }
            }
            mUserAdapter.notifyDataSetChanged();
        }
    }

    private static class FollowHandler extends Handler {
        private ArrayAdapter<User> mAdapter;

        public FollowHandler(ArrayAdapter<User> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                if (message.obj instanceof JSONObject) {
                    User u = new User((JSONObject)message.obj);
                    int length = mAdapter.getCount();
                    for (int i = 0; i != length; ++i) {
                        User p = mAdapter.getItem(i);
                        if (p.uniqueID.equals(u.uniqueID)) {
                            p.isFollowing = u.isFollowing;
                            break;
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }
}
