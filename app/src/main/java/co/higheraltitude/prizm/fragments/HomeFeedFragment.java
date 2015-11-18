package co.higheraltitude.prizm.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.FullBleedPostActivity;
import co.higheraltitude.prizm.LikesActivity;
import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HomePostView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link HomeFeedFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class HomeFeedFragment extends android.support.v4.app.Fragment
implements HomePostView.HomePostViewDelegate {

    private ListView mListView;
    private HomeFeedAdapter mAdapter;
    private ProgressBar mProgressBar;

    private int lastVisibleItem = 0;
    private boolean scrollingDown = false;
    private boolean isUpdating = false;
    private boolean shouldSlide = false;
    private boolean cacheLoaded = false;

    public HomeFeedFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_feed, container, false);
        configureViews(view);
        fetchPosts(false);
        return view;
    }

    private void configureViews(View view)
    {
        mListView = (ListView)view.findViewById(R.id.home_feed_list_view);
        mAdapter = new HomeFeedAdapter(getContext(), new ArrayList<Post>());
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mListView.setAdapter(mAdapter);
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
                    fetchPosts(true);
                } else {
                    if (view.getLastVisiblePosition() >= view.getCount() - 5) {
                        fetchOlderPosts();
                    }
                }
                lastVisibleItem = firstPosition;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

    }

    public void scrollToTop() {
        mListView.smoothScrollToPositionFromTop(0, 0);
    }

    private boolean listIsAtTop()   {
        if (mListView != null) {
            if (mListView.getChildCount() == 0) return true;
            return mListView.getChildAt(0).getTop() == 0;
        }
        return false;
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(true);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void fetchPosts(boolean update)
    {
        if (!isUpdating) {
            isUpdating = true;
            showProgressBar();
            Post.fetchHomeFeed("", new HomeFeedDelegate(update));
        }
    }
    private void fetchOlderPosts()
    {
        if (! isUpdating) {
            String date = "";
            if (mAdapter.getCount() > 0) {
                Post lastPost = mAdapter.getItem(mAdapter.getCount() - 1);
                date = lastPost.createDate;
            }
            isUpdating = true;
            showProgressBar();
            Post.fetchHomeFeed(date, new HomeFeedDelegate(false));
        }
    }

    private class HomeFeedAdapter extends ArrayAdapter<Post>
    {

        public HomeFeedAdapter(Context c, List<Post> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HomePostView view = (HomePostView)convertView;
            if (convertView == null) {
                view = HomePostView.inflate(parent);
            }
            view.setPost(getItem(position));
            view.setDelegate(HomeFeedFragment.this);
//            int lastVisiblePos = mListView.getLastVisiblePosition();
            if (lastVisibleItem + 1 <  position  && !isUpdating) {
                Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in);
                view.startAnimation(animation);
            }

            return view;
        }
    }

    private class HomeFeedDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private boolean mUpdateOnly;

        public HomeFeedDelegate(boolean updateOnly) {
            mUpdateOnly = updateOnly;
        }

        @Override
        public void cached(String path, Object object) {
            if (!mUpdateOnly) {
                process(object);
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            update(object);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<Post> posts = (ArrayList<Post>)object;
                if (posts.size() > 0) {
                    cacheLoaded = true;
                }
                mAdapter.addAll(posts);
            }
            isUpdating = false;
        }

        private void update(Object object) {
            if (object instanceof ArrayList) {
                int count = mAdapter.getCount();
                ArrayList<Post> posts = (ArrayList<Post>)object;
                for (Post p : posts) {
                    boolean found = false;
                    for (int i = 0; i != count; ++i) {
                        Post t = mAdapter.getItem(i);
                        if (t.uniqueId.equals(p.uniqueId)) {
                            mAdapter.remove(t);
                            mAdapter.insert(p, i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mAdapter.add(p);
                    }
                }
            }
            isUpdating = false;
            hideProgressBar();
            cacheLoaded = false;

        }
    }

    @Override
    public void avatarButtonClicked(Post post) {
        User user = new User();
        user.uniqueID = post.creatorId;
        user.profilePhotoURL = post.creatorProfilePhotoUrl;
        user.type = post.creatorType;
        user.subtype = post.creatorSubtype;
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        getActivity().startActivity(intent);
    }

    @Override
    public void likeButtonClicked(Post post) {
        if (post.ownPost) {
            Intent intent = new Intent(getContext(), LikesActivity.class);
            intent.putExtra(LikesActivity.EXTRA_POST, post);
            getActivity().startActivity(intent);
        } else {
            if (post.isLiked) {
                Post.unlikePost(post, new LikeHandler(mAdapter, mAdapter.getPosition(post)));
            } else {
                Post.likePost(post, new LikeHandler(mAdapter, mAdapter.getPosition(post)));
            }
        }
    }

    @Override
    public void postImageClicked(Post post){
        Intent intent = new Intent(getContext(), FullBleedPostActivity.class);
        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);
        getActivity().startActivity(intent);
    }

    private static class LikeHandler extends Handler
    {
        private HomeFeedAdapter mAdapter;
        private int mPosition;
        public LikeHandler(HomeFeedAdapter adapter, int position) {
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
