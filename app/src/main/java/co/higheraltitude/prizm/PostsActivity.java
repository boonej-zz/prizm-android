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
import android.widget.Filter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HomePostView;
import co.higheraltitude.prizm.views.SingleGridImageView;
import co.higheraltitude.prizm.views.TriPostView;

public class PostsActivity extends AppCompatActivity
        implements SingleGridImageView.SingleGridDelegate,
        HomePostView.HomePostViewDelegate
{

    public static final String EXTRA_USER = "extra_user";
    public static final String EXTRA_HASHTAG = "extra_hashtag";
    public static final String EXTRA_COUNT = "extra_count";
    private boolean isUpdating = false;
    private String mHashtag;

    private User mUser;

    private ImageButton mPrivateFilterButton;
    private GridView mGridView;
    private ListView mListView;
    private ProgressBar mProgressBar;

    private PostAdapter mAdapter;
    private int lastVisibleItem;
    private boolean scrollingDown;

    private View mFilterToolbar;
    private View mHashtagToolbar;
    private Boolean displayingCards = false;
    private ImageButton mCloseButton;
    private TextView mCountLabel;
    private String mCount;
    private ImageView mCardButton;
    private ImageView mGridButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);
        loadIntentData();
        configureToolbar();
        configureViews();
        loadPosts();
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        mUser = intent.getParcelableExtra(EXTRA_USER);
        mHashtag = intent.getStringExtra(EXTRA_HASHTAG);
        mCount = intent.getStringExtra(EXTRA_COUNT);
    }

    private void configureToolbar() {
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);

        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        if (mUser != null) {
            setTitle(mUser.name);
        } else if (mHashtag != null) {
            setTitle("#" + mHashtag);
            mCloseButton = (ImageButton)findViewById(R.id.action_close_button);
            mCloseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            mCloseButton.setVisibility(View.VISIBLE);

        }
    }

    private void configureViews() {
        mPrivateFilterButton = (ImageButton)findViewById(R.id.private_filter_button);
        mGridView = (GridView)findViewById(R.id.grid_view);
        mListView = (ListView)findViewById(R.id.list_view);
        mListView.setVisibility(View.GONE);
        mAdapter = new PostAdapter(getApplicationContext(), new ArrayList<Post>());
        mGridView.setAdapter(mAdapter);
        mListView.setAdapter(mAdapter);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mFilterToolbar = findViewById(R.id.filter_toolbar);
        mHashtagToolbar = findViewById(R.id.hashtag_toolbar);
        mCountLabel =(TextView)findViewById(R.id.count_view);
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
                        fetchOlderPosts();
                    }
                }
            }
        });
        if (mUser != null) {
            mHashtagToolbar.setVisibility(View.GONE);
        } else if (mHashtag != null) {
            mFilterToolbar.setVisibility(View.GONE);
            mCountLabel.setText(mCount);
        }
        mCardButton = (ImageView)findViewById(R.id.full_button);
        mGridButton = (ImageView)findViewById(R.id.grid_button);
    }

    public void loadPosts() {
        ArrayList<String> filters = new ArrayList<String>();
        showProgressBar();
        isUpdating = true;
        if (mUser != null) {

            if (mUser.isCurrentUser()) {
                mPrivateFilterButton.setVisibility(View.VISIBLE);
            }
            mAdapter.setFilters(filters);

            Post.fetchProfileFeed(mUser.uniqueID, null, null, new PostDelegate());
        } else if (mHashtag != null) {
            mAdapter.setFilters(filters);
            Post.getExplore("latest", null, null, mHashtag, new PostDelegate());
        }
    }

    private void fetchOlderPosts()
    {
        if (! isUpdating) {
            String date = "";
            if (mAdapter.getCount() > 0) {
                Post lastPost = mAdapter.mBaseList.get(mAdapter.mBaseList.size() - 1);
                date = lastPost.createDate;
            }
            isUpdating = true;
            showProgressBar();
            if (mUser != null) {
                Post.fetchProfileFeed(mUser.uniqueID, date, null, new PostDelegate());
            } else {
                Post.getExplore("latest", date, null, mHashtag, new PostDelegate());
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

    public void aspirationsButtonClicked(View view) {
        view.setSelected(!view.isSelected());
        mAdapter.toggleFilter(Post.CATEGORY_ASPIRATION);
    }

    public void experienceButtonClicked(View view) {
        view.setSelected(!view.isSelected());
        mAdapter.toggleFilter(Post.CATEGORY_EXPERIENCE);
    }

    public void passionButtonClicked(View view) {
        view.setSelected(!view.isSelected());
        mAdapter.toggleFilter(Post.CATEGORY_PASSION);
    }

    public void achievementButtonClicked(View view) {
        view.setSelected(!view.isSelected());
        mAdapter.toggleFilter(Post.CATEGORY_ACHIEVEMENT);
    }

    public void inspirationButtonClicked(View view) {
        view.setSelected(!view.isSelected());
        mAdapter.toggleFilter(Post.CATEGORY_INSPIRATION);
    }

    public void privateButtonClicked(View view) {
        view.setSelected(!view.isSelected());
        mAdapter.toggleFilter(Post.CATEGORY_PRIVATE);
    }

    public void gridButtonClicked(View view) {
        displayingCards = false;
        mListView.setVisibility(View.GONE);

        mAdapter.notifyDataSetInvalidated();
        mGridView.setVisibility(View.VISIBLE);
        mGridButton.setImageResource(R.drawable.grid_view_selected);
        mCardButton.setImageResource(R.drawable.full_view);
    }

    public void cardButtonClicked(View view) {
        displayingCards = true;
        mGridView.setVisibility(View.GONE);
        mAdapter.notifyDataSetInvalidated();
        mListView.setVisibility(View.VISIBLE);
        mGridButton.setImageResource(R.drawable.grid_view);
        mCardButton.setImageResource(R.drawable.full_view_selected);

    }

    @Override
    public void postImageClicked(Post post) {
        Intent intent = new Intent(getApplicationContext(), FullBleedPostActivity.class);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);

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
                Post.unlikePost(post, new LikeHandler(mAdapter, mAdapter.getPosition(post)));
            } else {
                Post.likePost(post, new LikeHandler(mAdapter, mAdapter.getPosition(post)));
            }
        }
    }

    private class PostAdapter extends ArrayAdapter<Post> {


        private int mViewType = 0;
        public ArrayList<Post> mBaseList;
        private ProfileFilter mFilter = new ProfileFilter();
        private ArrayList<String> mFilters;

        public PostAdapter(Context c, ArrayList<Post> items) {
            super(c, 0, items);
            mBaseList = new ArrayList<>();
            mBaseList.addAll(items);
        }

        public void setFilters(ArrayList<String> filters) {
            mFilters = filters;
        }

        public void addFilter(String filter) {
            mFilters.add(filter);
        }

        public void removeFilter(String filter) {
            mFilters.remove(filter);
        }

        public void toggleFilter(String filter) {
            if (mFilters.contains(filter)) {
                mFilters.remove(filter);
            } else {
                mFilters.add(filter);
            }
            mFilter.filter(filter);
        }

        public void setViewType(int viewType) {
            mViewType = viewType;
        }

        public void addAllPosts(Collection<? extends  Post> collection) {
            super.addAll(collection);
            mBaseList.addAll(collection);
        }

        public void addPost(Post object){
            mBaseList.add(object);
            if (mFilter.testFilters(object)) {
                add(object);
            }
        }

        public void insertPost(Post object, int index) {
            mBaseList.add(index, object);
            insert(object, index);

        }

        public void clearPosts() {
            super.clear();
            mBaseList.clear();
        }

        @Override
        public Filter getFilter() {
            return mFilter;
        }



        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View returnView = convertView;
//            ArrayList<Post> items = new ArrayList<>();
//
//            int start = position * 3;
//            int stop = start + 3;
//            if (stop > super.getCount()) {
//                stop = super.getCount();
//            }
//            for (int i = start; i != stop; ++i) {
//                items.add(getItem(i));
//            }
            if (displayingCards) {
                Post post = getItem(position);
                HomePostView itemView = null;
                if (convertView != null && convertView instanceof HomePostView) {
                    itemView = (HomePostView) convertView;
                } else {
                    itemView = HomePostView.inflate(parent);
                }
                itemView.setPost(post);
                itemView.setDelegate(PostsActivity.this);
                returnView =  itemView;

            } else {
                SingleGridImageView itemView = null;
                if (convertView != null && convertView instanceof SingleGridImageView) {
                    itemView = (SingleGridImageView) convertView;

                } else {
                    itemView = SingleGridImageView.inflate(parent);
                }
                itemView.setPost(getItem(position));
                itemView.setDelegate(PostsActivity.this);
//            itemView.setDelegate(PostsActivity.this);


                returnView = itemView;
            }

            return returnView;
        }

        public int getActualCount() {
            return super.getCount();
        }



        private class ProfileFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();

                ArrayList<Post> filt = new ArrayList<>();

                Iterator<Post> iterator = mBaseList.iterator();

                while (iterator.hasNext()) {
                    Post p = iterator.next();
                    if (testFilters(p)) {
                        filt.add(p);
                    }
                }

                results.values = filt;
                return results;
            }

            protected boolean testFilters(Post post) {
                if (mUser != null && !post.creatorId.equals(mUser.uniqueID)) {
                    return false;
                } else {
                    if (mFilters.size() == 0) {
                        return true;
                    } else {
                        boolean tested =  false;
                        for (String filter : mFilters) {
                            if (post.category.equals(filter)) {
                                tested = true;
                                break;
                            }
                        }
                        return tested;
                    }
                }
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

    @Override
    public void postClicked(Post post) {
        String path = PrizmDiskCache.getInstance(
                getApplicationContext()).bestAvailableImage(post.filePath);
        Intent intent = new Intent(getApplicationContext(), FullBleedPostActivity.class);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST_IMAGE, path);
        startActivity(intent);
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
                    for (Post o : mAdapter.mBaseList) {
                        if (post.uniqueId.equals(o.uniqueId)) {
                            inserted = true;
                        }
                    }
                    if (!inserted) {
                        mAdapter.addPost(post);
                    }
                }
                isUpdating = false;

            }
            mAdapter.notifyDataSetChanged();
            hideProgressBar();
        }
    }

    private static class LikeHandler extends Handler
    {
        private PostAdapter mAdapter;
        private int mPosition;
        public LikeHandler(PostAdapter adapter, int position) {
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
}
