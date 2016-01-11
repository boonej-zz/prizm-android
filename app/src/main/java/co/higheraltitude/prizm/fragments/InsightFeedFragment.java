package co.higheraltitude.prizm.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.FullBleedInsight;
import co.higheraltitude.prizm.FullBleedPostActivity;
import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Insight;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.InsightArchiveView;
import co.higheraltitude.prizm.views.InsightCardView;
import co.higheraltitude.prizm.views.SingleGridImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class InsightFeedFragment extends Fragment
        implements InsightCardView.InsightCardViewDelegate, InsightArchiveView.InsightArchiveViewDelegate {

    ListView mListView;


    public static final String ARGUMENT_TYPE = "type";
    public static final int INSIGHT_TYPE_INBOX = 0;
    public static final int INSIGHT_TYPE_ARCHIVE = 1;
    public static final int FULL_BLEED_REQUEST = 667;


    private int lastVisibleItem = 0;
    private int mType = 0;
    private boolean scrollingDown = false;
    private boolean isUpdating = false;
    private boolean shouldSlide = false;
    private boolean cacheLoaded = false;
    private String mBestImageUrl;
    private ProgressBar mProgressBar;

    private InsightAdapter mInsightAdapter;

    public InsightFeedFragment() {
        // Required empty public constructor
    }

    public void loadInsights(final Boolean refresh) {
        if (!isUpdating) {
            isUpdating = true;
            String type = mType == INSIGHT_TYPE_INBOX ? "inbox" : "archive";
            int skip = refresh?0:mInsightAdapter.getCount();
            Insight.fetchInsights(type, skip, new PrizmDiskCache.CacheRequestDelegate() {
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
                        ArrayList<Insight> insights = (ArrayList<Insight>) object;
                        for (Insight insight : insights) {
                            int length = mInsightAdapter.getCount();
                            boolean updated = false;
                            for (int i = 0; i != length; ++i) {
                                Insight o = mInsightAdapter.getItem(i);
                                if (o.uniqueId == insight.uniqueId) {
                                    updated = true;
                                    break;
                                }
                            }
                            if (!updated) {
                                if (refresh) {
                                    mInsightAdapter.insert(insight, insights.indexOf(insight));
                                } else {
                                    mInsightAdapter.add(insight);
                                }
                            }
                        }
                    }
                }
            });
        }
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
        View view = inflater.inflate(R.layout.fragment_insight_feed, container, false);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mListView = (ListView)view.findViewById(R.id.list_view);
        mInsightAdapter = new InsightAdapter(getContext(), new ArrayList<Insight>());
        mListView.setAdapter(mInsightAdapter);
//        mGridView = (GridView)view.findViewById(R.id.grid_view);
//        if (mType == EXPLORE_TYPE_POPULAR) {
//            Log.d("DEBUG", "We are in the popular feed.");
//        }
//

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
//                    loadInsights();
                } else {
                    if (view.getLastVisiblePosition() >= view.getCount() - 15) {
                        loadInsights(false);
                    }
                }
                lastVisibleItem = firstPosition;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mType == INSIGHT_TYPE_ARCHIVE) {
                    Insight i = mInsightAdapter.getItem(position);
                    Intent intent = new Intent(getContext(), FullBleedInsight.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra(FullBleedInsight.EXTRA_INSIGHT, i);
                    startActivityForResult(intent, FULL_BLEED_REQUEST);
                }
            }
        });
//        mPostAdapter = new PostAdapter(getContext(), new ArrayList<Post>());
//        mGridView.setAdapter(mPostAdapter);


//        fetchPosts(false, false);

        loadInsights(false);
        return view;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            loadInsights(true);
        }
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

    private void showProgressBar() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);

    }

//    private void fetchPosts(final Boolean updateOnly, final Boolean old) {
//        if (!isUpdating) {
//            isUpdating = true;
//            String before = null;
//            String type = null;
//            switch(mType) {
//                case EXPLORE_TYPE_POPULAR:
//                    type = Post.EXPLORE_TYPE_POPULAR;
//                    break;
//                case EXPLORE_TYPE_FEATURED:
//                    type = Post.EXPLORE_TYPE_FEATURED;
//                    break;
//                default:
//                    break;
//            }
//            if (old && mPostAdapter.getCount() > 0) {
//                before = mPostAdapter.getItem(mPostAdapter.getCount() - 1).createDate;
//            }
//            showProgressBar();
//            Post.getExplore(type, before, null, null, new PrizmDiskCache.CacheRequestDelegate() {
//                @Override
//                public void cached(String path, Object object) {
//                    if (!updateOnly) {
//                        update(object);
//                    }
//                }
//
//                @Override
//                public void cacheUpdated(String path, Object object) {
//                    update(object);
//                }
//
//
//                private void update(Object object) {
//                    if (object instanceof ArrayList) {
//                        int count = mPostAdapter.getCount();
//                        ArrayList<Post> posts = (ArrayList<Post>) object;
//                        for (Post p : posts) {
//                            boolean found = false;
//                            for (int i = 0; i != count; ++i) {
//                                Post t = mPostAdapter.getItem(i);
//                                if (t.uniqueId.equals(p.uniqueId)) {
//                                    mPostAdapter.remove(t);
//                                    mPostAdapter.insert(p, i);
//                                    found = true;
//                                    break;
//                                }
//                            }
//                            if (!found) {
//                                if (updateOnly) {
//                                    mPostAdapter.insert(p, posts.indexOf(p));
//                                } else {
//                                    mPostAdapter.add(p);
//                                }
//                            }
//                        }
//                    }
//                    isUpdating = false;
//                    hideProgressBar();
//                    cacheLoaded = false;
//                    mPostAdapter.notifyDataSetChanged();
//
//                }
//            });
//        }
//    }

    private class InsightAdapter extends ArrayAdapter<Insight> {
        public InsightAdapter(Context c, List<Insight> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mType == INSIGHT_TYPE_INBOX) {
                InsightCardView view = (InsightCardView) convertView;
                if (view == null) {
                    view = InsightCardView.inflate(parent);
                }
                view.setInsight(getItem(position));
                view.setDelegate(InsightFeedFragment.this);
                return view;
            } else {
                InsightArchiveView view = (InsightArchiveView)convertView;
                if (view == null) {
                    view = InsightArchiveView.inflate(parent);
                }
                view.setInsight(getItem(position));
                view.setDelegate(InsightFeedFragment.this);
                return view;
            }
        }
    }

//    @Override
//    public void postClicked(Post post) {
//        Intent intent = new Intent(getContext(), FullBleedPostActivity.class);
//        intent.putExtra(FullBleedPostActivity.EXTRA_POST, post);
//        intent.putExtra(FullBleedPostActivity.EXTRA_POST_IMAGE,
//                PrizmDiskCache.getInstance(getContext()).bestAvailableImage(post.filePath) );
//        startActivity(intent);
//    }
    @Override
    public void avatarButtonClicked(Insight insight) {
        User user = new User();
        user.uniqueID = insight.creatorId;
        user.profilePhotoURL = insight.creatorProfilePhotoUrl;
        user.type = insight.creatorType;
        user.subtype = insight.creatorSubtype;
        user.name = insight.creatorName;
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }

    @Override
    public void likeButtonClicked(Insight insight) {
        Insight.likeInsight(insight, new InsightLikeHandler(insight, mInsightAdapter));
    }

    @Override
    public void dislikeButtonClicked(Insight insight) {
        Insight.dislikeInsight(insight, new InsightLikeHandler(insight, mInsightAdapter));
    }

    @Override
    public void insightImageClicked(Insight insight) {
        Intent intent = new Intent(getContext(), FullBleedInsight.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        intent.putExtra(FullBleedInsight.EXTRA_INSIGHT, insight);
        startActivityForResult(intent, FULL_BLEED_REQUEST);
    }

    @Override
    public void insightLinkClicked(Insight insight) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(insight.link));
        startActivity(browserIntent);
    }

    private static class InsightLikeHandler extends Handler {

        private Insight mInsight;
        private InsightAdapter mAdapter;

        public InsightLikeHandler(Insight insight, InsightAdapter adapter) {
            mInsight = insight;
            mAdapter = adapter;
        }

        @Override
        public void handleMessage(Message message){
            mAdapter.remove(mInsight);
            mAdapter.notifyDataSetChanged();
        }
    }

    private void removeInsight(Insight insight) {
        int length = mInsightAdapter.getCount();
        for (int i = 0; i != length; ++i) {
            Insight o = mInsightAdapter.getItem(i);
            if (o.uniqueId.equals(insight.uniqueId)) {
                mInsightAdapter.remove(o);
                mInsightAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == FULL_BLEED_REQUEST) {
                Boolean liked = data.getBooleanExtra(FullBleedInsight.EXTRA_RESULT_LIKED, false);
                Insight insight = data.getParcelableExtra(FullBleedInsight.EXTRA_INSIGHT);
                switch (mType) {
                    case INSIGHT_TYPE_ARCHIVE:
                        removeInsight(insight);
                        break;
                    case INSIGHT_TYPE_INBOX:
                        if (!liked) {
                            removeInsight(insight);
                        }
                        break;
                    default:
                        break;
                }
            }
        } else {
            loadInsights(true);
        }
    }


}
