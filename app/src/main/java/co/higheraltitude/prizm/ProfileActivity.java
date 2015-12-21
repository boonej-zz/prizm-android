package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HomePostView;
import co.higheraltitude.prizm.views.ProfileHeaderView;
import co.higheraltitude.prizm.views.TriPostView;

public class ProfileActivity extends AppCompatActivity implements PrizmDiskCache.CacheRequestDelegate,
         TriPostView.TriPostViewDelegate, ProfileHeaderView.ProfileHeaderViewDelegate, HomePostView.HomePostViewDelegate {
    private ProgressBar progressBar = null;
    private User mUser = null;
    private ListView mPostsList;
    private ProfilePostAdapter mPostAdapter;
    private LinearLayout mMainLayout;
    private ProfileHeaderView mHeaderView;
    private int lastVisibleItem = 0;
    private boolean isUpdating = false;
    private boolean shouldSlide = false;
    private boolean scrollingDown = false;
    private boolean isLocationFiltering = false;
    private boolean isTagFiltering = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);


        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));

        Intent intent = getIntent();
        mUser = intent.getParcelableExtra(LoginActivity.EXTRA_PROFILE);
        User.fetchUserCore(mUser, this);

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mMainLayout = (LinearLayout)findViewById(R.id.main_layout);
        mPostsList = (ListView)findViewById(R.id.post_list);

        mHeaderView = ProfileHeaderView.inflate(mPostsList);
        mHeaderView.setFragmentManager(getSupportFragmentManager());
        mHeaderView.setDelegate(this);
        mPostsList.addHeaderView(mHeaderView);
        mPostsList.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                        fetchOlderPosts();
                    }
                }
            }
        });
        configureViews();
        loadPosts();
    }

    protected void onResume() {
        super.onResume();
        configureViews();
    }

    private void showProgressBar() {
        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }

    private boolean listIsAtTop() {
        if (mPostsList != null) {
            if (mPostsList.getChildCount() == 0) return true;
            return mPostsList.getChildAt(0).getTop() == 0;
        }
        return false;
    }


    private void configureViews() {

        mHeaderView.setUser(mUser);

    }

    public void loadPosts() {
        mPostAdapter = new ProfilePostAdapter(getApplicationContext(), new ArrayList<Post>());
        mPostsList.setAdapter(mPostAdapter);
        showProgressBar();
        isUpdating = true;
        Post.fetchProfileFeed(mUser.uniqueID, null, null, new PostDelegate());
    }

    private void fetchOlderPosts()
    {
        if (! isUpdating) {
            String date = "";
            if (mPostAdapter.getCount() > 0) {
                Post lastPost = mPostAdapter.baseList.get(mPostAdapter.baseList.size() - 1);
                date = lastPost.createDate;
            }
            isUpdating = true;
            showProgressBar();
            Post.fetchProfileFeed(mUser.uniqueID, date, null,  new PostDelegate());
        }
    }

    private class PostDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            update(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            update(object);
            if (object instanceof ArrayList) {
                if (((ArrayList<?>)object).size() > 0) {
                    fetchOlderPosts();
                }
            }
        }



        private void update(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<Post> posts = (ArrayList<Post>)object;
                for (Post post : posts) {
                    boolean inserted = false;
                    for (Post o : mPostAdapter.baseList) {
                        if (post.uniqueId.equals(o.uniqueId)) {
                            inserted = true;
                        }
                    }
                    if (!inserted) {
                        mPostAdapter.addPost(post);
                    }
                }
                isUpdating = false;
            }
            mPostAdapter.notifyDataSetChanged();

        }
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void cached(String path, Object object) {

    }

    @Override
    public void cacheUpdated(String path, Object object) {
        fillProfile(object);
    }

    private void fillProfile(Object object) {
        mUser = (User)object;

        configureViews();
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }





    @Override
    public void postClicked(Post post) {
        String pathString = PrizmDiskCache.getInstance(
                getApplicationContext()).bestAvailableImage(post.filePath);
        Intent intent = new Intent(getApplicationContext(), FullBleedPostActivity.class);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST_IMAGE, pathString);
        startActivity(intent);
    }

    @Override
    public void gridViewClicked() {
        mPostAdapter.setViewType(mPostAdapter.VIEW_TYPE_GRID);
        mPostAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void fullViewClicked() {
        mPostAdapter.setViewType(mPostAdapter.VIEW_TYPE_FULL);
        mPostAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void avatarButtonClicked(Post post) {
        User user = new User();
        user.uniqueID = post.creatorId;
        user.profilePhotoURL = post.creatorProfilePhotoUrl;
        user.type = post.creatorType;
        user.subtype = post.creatorSubtype;
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }

    @Override
    public void likeButtonClicked(Post post) {
        if (post.ownPost) {
            Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
            intent.putExtra(LikesActivity.EXTRA_POST, post);
            startActivity(intent);
        } else {
            if (post.isLiked) {
                Post.unlikePost(post, new LikeHandler(mPostAdapter, mPostAdapter.getPosition(post)));
            } else {
                Post.likePost(post, new LikeHandler(mPostAdapter, mPostAdapter.getPosition(post)));
            }
        }
    }

    @Override
    public void editProfileClicked() {
        Intent intent = new Intent(getApplicationContext(), EditProfile.class);
        startActivity(intent);
    }

    @Override
    public void tagsFilterClicked(boolean selected) {
        isTagFiltering = selected;
        updateFilters();
    }

    @Override
    public void followUser(){
        User.followUser(mUser.uniqueID, new Handler());
    }

    @Override
    public void unfollowUser(){
        User.unfollowUser(mUser.uniqueID, new Handler());
    }

    @Override
    public void locationFilterClicked(boolean selected) {
        isLocationFiltering = selected;
        updateFilters();
    }

    @Override
    public void postButtonClicked() {
        Intent intent = new Intent(getApplicationContext(), PostsActivity.class);
        intent.putExtra(PostsActivity.EXTRA_USER, mUser);
        startActivity(intent);
    }

    private void updateFilters() {
        String filter;
        if (isLocationFiltering && isTagFiltering) {
            filter = mPostAdapter.FILTER_TYPE_ALL;
        } else {
            if (isLocationFiltering) {
                filter = mPostAdapter.FILTER_TYPE_LOCATION;
            } else if (isTagFiltering) {
                filter = mPostAdapter.FILTER_TYPE_TAGS;
            } else {
                filter = mPostAdapter.FILTER_TYPE_NONE;
            }
        }
        mPostAdapter.getFilter().filter(filter);
    }

    @Override
    public void postImageClicked(Post post) {
        Intent intent = new Intent(getApplicationContext(), FullBleedPostActivity.class);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);

        startActivity(intent);
    }

    @Override
    public void followingClicked() {
        Intent intent = new Intent(getApplicationContext(), FollowActivity.class);
        intent.putExtra(FollowActivity.EXTRA_USER, mUser);
        intent.putExtra(FollowActivity.EXTRA_VIEW_TYPE, FollowActivity.VIEW_TYPE_FOLLOWING);
        startActivity(intent);
    }

    @Override
    public void followersClicked() {
        Intent intent = new Intent(getApplicationContext(), FollowActivity.class);
        intent.putExtra(FollowActivity.EXTRA_USER, mUser);
        intent.putExtra(FollowActivity.EXTRA_VIEW_TYPE, FollowActivity.VIEW_TYPE_FOLLOWERS);
        startActivity(intent);
    }

    private static class LikeHandler extends Handler
    {
        private ProfilePostAdapter mAdapter;
        private int mPosition;
        public LikeHandler(ProfilePostAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }
        @Override
        public void handleMessage(Message message) {
            if (message.obj != null && message.obj instanceof  Post) {
                Post p = (Post)message.obj;
                Post o = mAdapter.getItem(mPosition);
                mAdapter.remove(o);
                mAdapter.insert(p, mPosition);
            }
        }
    }



    private class ProfilePostAdapter extends ArrayAdapter<Post> {
        public final int VIEW_TYPE_GRID = 0;
        public final int VIEW_TYPE_FULL = 1;
        public final String FILTER_TYPE_NONE = "filter_none";
        public final String FILTER_TYPE_TAGS = "filter_tags";
        public final String FILTER_TYPE_LOCATION = "filter_location";
        public final String FILTER_TYPE_ALL = "filter_all";
        private int mViewType = 0;
        public ArrayList<Post> baseList;
        private ProfileFilter mFilter = new ProfileFilter();
        private String mCurrentFilter = FILTER_TYPE_NONE;

        public ProfilePostAdapter(Context c, ArrayList<Post> items) {
            super(c, 0, items);
            baseList = new ArrayList<>();
            baseList.addAll(items);
        }

        public void setViewType(int viewType) {
            mViewType = viewType;
        }

        public void addAllPosts(Collection<? extends  Post> collection) {
            super.addAll(collection);
            baseList.addAll(collection);
        }

        public void addPost(Post object){
            baseList.add(object);
            if (mCurrentFilter == FILTER_TYPE_ALL) {
                if (mFilter.testTags(object) && mFilter.testLocation(object)) {
                    add(object);
                }
            } else if (mCurrentFilter.equals(FILTER_TYPE_TAGS)) {
                if (mFilter.testTags(object)) {
                    add(object);
                }
            } else if (mCurrentFilter.equals(FILTER_TYPE_LOCATION)) {
                if (mFilter.testOwnPost(object) && mFilter.testLocation(object)) {
                    add(object);
                }
            } else {
                if (mFilter.testOwnPost(object)) {
                    add(object);
                }
            }

        }

        public void insertPost(Post object, int index) {
            baseList.add(index, object);
            insert(object, index);

        }

        public void clearPosts() {
            super.clear();
            baseList.clear();
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View returnView = convertView;
            if (mViewType == VIEW_TYPE_GRID) {
                ArrayList<Post> items = new ArrayList<>();
                int start = position * 3;
                int stop = start + 3;
                if (stop > super.getCount()) {
                    stop = super.getCount();
                }
                for (int i = start; i != stop; ++i) {
                    items.add(getItem(i));
                }
                TriPostView itemView = null;
                if (convertView != null && convertView instanceof TriPostView) {
                    itemView = (TriPostView) convertView;

                } else {
                    itemView = TriPostView.inflate(parent);
                }
                itemView.setPosts(items);
                itemView.setDelegate(ProfileActivity.this);


                returnView =  itemView;
            } else if (mViewType == VIEW_TYPE_FULL) {
                Post post = getItem(position);
                HomePostView itemView = null;
                if (convertView != null && convertView instanceof HomePostView) {
                    itemView = (HomePostView) convertView;
                } else {
                    itemView = HomePostView.inflate(parent);
                }
                itemView.setPost(post);
                itemView.setDelegate(ProfileActivity.this);
                returnView =  itemView;
            }
            return returnView;
        }

        public int getActualCount() {
            return super.getCount();
        }

        @Override
        public int getCount() {
            int count = 0;
            if (mViewType == VIEW_TYPE_GRID) {
                count = super.getCount() / 3;
                if (super.getCount() % 3 != 0) {
                    count += 1;
                }
            } else if (mViewType == VIEW_TYPE_FULL) {
                count = super.getCount();
            }
            return count;
        }

        private class ProfileFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                mCurrentFilter = constraint.toString();
                FilterResults results = new FilterResults();

                ArrayList<Post> filt = new ArrayList<>();
                String filter = constraint.toString();
                Iterator<Post> iterator = baseList.iterator();

                while (iterator.hasNext()) {
                    Post p = iterator.next();
                    if (filter.equals(FILTER_TYPE_LOCATION)) {
                        if (testLocation(p) && testOwnPost(p)) {
                            filt.add(p);
                        }
                    } else if (filter.equals(FILTER_TYPE_TAGS)) {
                        if (testTags(p)) {
                            filt.add(p);
                        }
                    } else if (filter.equals(FILTER_TYPE_ALL)) {
                        if (testLocation(p) && testTags(p)) {
                            filt.add(p);
                        }
                    } else {
                        if (testOwnPost(p)) {
                            filt.add(p);
                        }
                    }
                }

                results.values = filt;
                return results;
            }

            protected boolean testLocation(Post post) {
                boolean tested =  post.locationLatitude != 0 || post.locationLongitude != 0;
                return tested;
            }

            protected boolean testTags(Post post) {
                if (post.text != null && !post.text.isEmpty()) {
                    return post.text.toLowerCase().contains(mUser.uniqueID.toLowerCase());
                }
                return false;
            }

            protected boolean testOwnPost(Post post) {
                return post.creatorId.equals(mUser.uniqueID);
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                ArrayList<Post> filtered = (ArrayList<Post>)results.values;
                if (filtered == null) {
                    filtered = new ArrayList<>();
                }

                notifyDataSetChanged();
                clear();
                addAll(filtered);
                notifyDataSetInvalidated();
            }
        }
    }

}
