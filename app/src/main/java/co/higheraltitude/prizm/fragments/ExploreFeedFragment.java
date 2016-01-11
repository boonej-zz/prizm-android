package co.higheraltitude.prizm.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.FullBleedPostActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.views.SingleGridImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFeedFragment extends Fragment implements SingleGridImageView.SingleGridDelegate {

    GridView mGridView;
    PostAdapter mPostAdapter;

    public static final String ARGUMENT_TYPE = "type";
    public static final int EXPLORE_TYPE_LATEST = 0;
    public static final int EXPLORE_TYPE_POPULAR = 1;
    public static final int EXPLORE_TYPE_FEATURED = 2;


    private int lastVisibleItem = 0;
    private int mType = 0;
    private boolean scrollingDown = false;
    private boolean isUpdating = false;
    private boolean shouldSlide = false;
    private boolean cacheLoaded = false;
    private String mBestImageUrl;
    private ProgressBar mProgressBar;

    public ExploreFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mType = getArguments().getInt(ARGUMENT_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPostAdapter.notifyDataSetInvalidated();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore_feed, container, false);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mGridView = (GridView)view.findViewById(R.id.grid_view);
        if (mType == EXPLORE_TYPE_POPULAR) {
            Log.d("DEBUG", "We are in the popular feed.");
        }


        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int firstPosition = view.getFirstVisiblePosition();

                if (lastVisibleItem > firstPosition) {
                    shouldSlide = false;
                    scrollingDown = false;
                } else if (lastVisibleItem < firstPosition) {
                    shouldSlide = true;
                    scrollingDown = true;
                } else if (listIsAtTop()) {
                    fetchPosts(true, false);
                } else {
                    if (view.getLastVisiblePosition() >= view.getCount() - 20) {
                        fetchPosts(false, true);
                    }
                }
                lastVisibleItem = firstPosition;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
        mPostAdapter = new PostAdapter(getContext(), new ArrayList<Post>());
        mGridView.setAdapter(mPostAdapter);


        fetchPosts(false, false);


        return view;
    }


    public void scrollToTop() {
        mGridView.smoothScrollToPositionFromTop(0, 0);
    }


    private boolean listIsAtTop()   {
        if (mGridView != null) {
            if (mGridView.getChildCount() == 0) return true;
            return mGridView.getChildAt(0).getTop() == 0;
        }
        return false;
    }

    private void showProgressBar() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);

    }

    private void fetchPosts(final Boolean updateOnly, final Boolean old) {
        if (!isUpdating) {
            isUpdating = true;
            String before = null;
            String type = null;
            switch(mType) {
                case EXPLORE_TYPE_POPULAR:
                    type = Post.EXPLORE_TYPE_POPULAR;
                    break;
                case EXPLORE_TYPE_FEATURED:
                    type = Post.EXPLORE_TYPE_FEATURED;
                    break;
                default:
                    break;
            }
            if (old && mPostAdapter.getCount() > 0) {
                before = mPostAdapter.getItem(mPostAdapter.getCount() - 1).createDate;
            }
            showProgressBar();
            Post.getExplore(type, before, null, null, new PrizmDiskCache.CacheRequestDelegate() {
                @Override
                public void cached(String path, Object object) {
                    if (!updateOnly) {
                        update(object);
                    }
                }

                @Override
                public void cacheUpdated(String path, Object object) {
                    update(object);
                }


                private void update(Object object) {
                    if (object instanceof ArrayList) {
                        int count = mPostAdapter.getCount();
                        ArrayList<Post> posts = (ArrayList<Post>) object;
                        for (Post p : posts) {
                            boolean found = false;
                            for (int i = 0; i != count; ++i) {
                                Post t = mPostAdapter.getItem(i);
                                if (t.uniqueId.equals(p.uniqueId)) {
                                    mPostAdapter.remove(t);
                                    mPostAdapter.insert(p, i);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                if (updateOnly) {
                                    mPostAdapter.insert(p, posts.indexOf(p));
                                } else {
                                    mPostAdapter.add(p);
                                }
                            }
                        }
                    }
                    isUpdating = false;
                    hideProgressBar();
                    cacheLoaded = false;
                    mPostAdapter.notifyDataSetChanged();

                }
            });
        }
    }

    private class PostAdapter extends ArrayAdapter<Post> {
        public PostAdapter(Context c, List<Post> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SingleGridImageView view = (SingleGridImageView)convertView;
            if (convertView == null) {
                view = SingleGridImageView.inflate(parent);
            }
            view.setPost(getItem(position));
            view.setDelegate(ExploreFeedFragment.this);
            return view;
        }
    }

    @Override
    public void postClicked(Post post) {
        Intent intent = new Intent(getContext(), FullBleedPostActivity.class);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST_IMAGE,
                PrizmDiskCache.getInstance(getContext()).bestAvailableImage(post.filePath) );
        startActivity(intent);
    }


}
