package co.higheraltitude.prizm.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import co.higheraltitude.prizm.FullBleedPostActivity;
import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.PostsActivity;
import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.SearchActivity;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.delegates.UserDelegate;
import co.higheraltitude.prizm.helpers.MixpanelHelper;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HashTagCountView;
import co.higheraltitude.prizm.views.SingleGridImageView;
import co.higheraltitude.prizm.views.UserFollowingAvatarView;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchResultsFragment extends Fragment
        implements UserFollowingAvatarView.UserAvatarViewDelegate {
    public static final int SEARCH_TYPE_USER = 0;
    public static final int SEARCH_TYPE_HASHTAG = 1;

    public static String ARGUMENT_TYPE = "search_type";

    private HashTagAdapter mHashTagAdapter;
    private FollowUserAdapter mUserAdapter;
    private int mType;
    private String mSearchText = "";

    private Boolean isUpdating = false;



    ListView mListView;


    private ProgressBar mProgressBar;

    public SearchResultsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(ARGUMENT_TYPE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_results, container, false);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mListView = (ListView)view.findViewById(R.id.list_view);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                if (mType == SEARCH_TYPE_HASHTAG) {
                    Intent intent = new Intent(getContext(), PostsActivity.class);
                    String tag = mHashTagAdapter.getItem(position).get("tag").substring(1);
                    String count = mHashTagAdapter.getItem(position).get("count");
                    intent.putExtra(PostsActivity.EXTRA_HASHTAG, tag);
                    intent.putExtra(PostsActivity.EXTRA_COUNT, count);
                    startActivityForResult(intent, 20);
                } else if (mType == SEARCH_TYPE_USER) {
                    Intent intent = new Intent(getContext(), ProfileActivity.class);
                    User u = mUserAdapter.getItem(position);
                    intent.putExtra(LoginActivity.EXTRA_PROFILE, u);
                    startActivity(intent);
                }
            }
        });

        mHashTagAdapter = new HashTagAdapter(getContext(), new ArrayList<Map<String, String>>());
        mUserAdapter = new FollowUserAdapter(getContext(), new ArrayList<User>());
        if (mType == SEARCH_TYPE_HASHTAG)
            mListView.setAdapter(mHashTagAdapter);
        else if (mType == SEARCH_TYPE_USER)
            mListView.setAdapter(mUserAdapter);
        hideProgressBar();
        configureScroll();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mType == SEARCH_TYPE_HASHTAG)
            fetchItems(false);
    }

    @Override
    public void avatarViewClicked(UserFollowingAvatarView view) {
        User user = view.getUser();
        Intent intent = new Intent(getContext(), ProfileActivity.class);
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

    private void configureScroll() {
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            private int mLastVisibleItem = 0;
            private boolean mScrollingDown = false;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int firstPosition = view.getFirstVisiblePosition();

                if (mLastVisibleItem > firstPosition) {
                    mScrollingDown = false;
                } else if (mLastVisibleItem < firstPosition) {
                    mScrollingDown = true;
                }
                if (view.getLastVisiblePosition() >= view.getCount() - 5) {
                    fetchItems(true);
                }

                mLastVisibleItem = firstPosition;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    private void fetchItems(Boolean append) {
        if (!isUpdating) {
            showProgressBar();
            isUpdating = true;
            if (!append) {
                mHashTagAdapter.clear();
                mUserAdapter.clear();
                String track = mType == SEARCH_TYPE_HASHTAG?"Searched Hashtags":"Searched Users";
                MixpanelHelper.getTracker().track(track);
            }
            switch (mType) {
                case SEARCH_TYPE_HASHTAG:
                    Post.searchHashtags(mSearchText, true, mHashTagAdapter.getCount(),
                            new HashTagDelegate(false));
                    break;
                case SEARCH_TYPE_USER:
                    if (mSearchText != null && mSearchText.length() > 0) {
                        User.searchUsers(mSearchText, mUserAdapter.getCount(), new FollowingDelegate());
                    } else {
                        mUserAdapter.clear();
                        hideProgressBar();
                    }
                    break;
                default:
                    break;
            }
        }
    }


    private void showProgressBar() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);

    }

    private class HashTagDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private Boolean mAppend;
        public  HashTagDelegate(Boolean append) {
            mAppend = append;
        }

        @Override
        public void cached(String path, Object object) {
            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            process(object);
            isUpdating = false;
            hideProgressBar();
        }

        private void process(Object object) {

            if (object instanceof ArrayList) {
                ArrayList<Map<String, String>> tags = (ArrayList<Map<String, String>>) object;
                if (!mAppend) {
                    mHashTagAdapter.addAll(tags);
                } else {
                    for (Map<String, String> tag : tags) {
                        int length = mHashTagAdapter.getCount();
                        Boolean updated = false;
                        for (int i = 0; i != length; ++i) {
                            Map<String, String> item = mHashTagAdapter.getItem(i);
                            if (tag.get("tag").equals(item.get("tag"))) {
                                mHashTagAdapter.remove(item);
                                mHashTagAdapter.insert(tag, i);
                                updated = true;
                            }
                        }
                        if (!updated) {
                            mHashTagAdapter.add(tag);
                        }
                    }
                }


                //                mListView.setOnItemClickListener(mHashListener);
            }

        }


    }

    public void searchText(String string) {
        mSearchText = string;
        fetchItems(false);
    }

    private static class HashTagAdapter extends ArrayAdapter<Map<String, String>> {


        public HashTagAdapter(Context c, List<Map<String, String>> hashTags) {
            super(c, 0, hashTags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HashTagCountView view = (HashTagCountView)convertView;
            if (view == null) {
                view = HashTagCountView.inflate(parent);
            }
            view.setHashTag(this.getItem(position));
            return view;
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
            view.setDelegate(SearchResultsFragment.this);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 20) {
            if (resultCode == Activity.RESULT_CANCELED) {
                getActivity().finish();
            }
        }
    }



}
