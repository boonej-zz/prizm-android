package co.higheraltitude.prizm.fragments;


import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Activity;
import co.higheraltitude.prizm.models.LeaderboardItem;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.LeaderboardView;


public class SurveyFragment extends Fragment implements PrizmDiskCache.CacheRequestDelegate,
        AbsListView.OnScrollListener, LeaderboardView.LeaderboardViewDelegate {

    private LeaderboardAdapter mAdapter;

    private ListView mListView;
    private ImageView mAvatarView;
    private TextView mUserNameLabel;
    private TextView mUserRankLabel;
    private TextView mUserPointsLabel;
    private TextView mSurveyCountLabel;
    private ProgressBar mProgressBar;

    private Boolean mIsUpdating = false;
    private int mLastVisibleItem;


    public SurveyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_survey, container, false);
        mListView = (ListView)view.findViewById(R.id.leaderboard);
        mListView.setOnScrollListener(this);

        mAdapter = new LeaderboardAdapter(getContext(), new ArrayList<LeaderboardItem>());
        mListView.setAdapter(mAdapter);
        mAvatarView = (ImageView)view.findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                avatarClicked(v);
            }
        });
        mUserNameLabel = (TextView)view.findViewById(R.id.user_name);
        mUserRankLabel = (TextView)view.findViewById(R.id.rank_label);
        mUserPointsLabel = (TextView)view.findViewById(R.id.points_label);
        mSurveyCountLabel = (TextView)view.findViewById(R.id.survey_count);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);

        Rect rectangle= new Rect();
        Window window= getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)mListView.getLayoutParams();
        params.height = rectangle.height();
        mListView.setLayoutParams(params);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLeaderboard(0);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        int firstPosition = view.getFirstVisiblePosition();

        if (mLastVisibleItem > firstPosition) {

        } else if (mLastVisibleItem < firstPosition) {

        } else {
            if (view.getLastVisiblePosition() >= view.getCount() - 15) {
                loadLeaderboard(mAdapter.getCount());
            }
        }
        mLastVisibleItem = firstPosition;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }

    @Override
    public void cached(String path, Object data) {
        update(data);

    }

    @Override
    public void cacheUpdated(String path, Object data) {
        update(data);
        hideProgressBar();
        mIsUpdating = false;
    }

    @Override
    public void avatarViewClicked(User user) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }

    protected void loadLeaderboard(int skip) {
        if (! mIsUpdating) {
            showProgressBar();
            mIsUpdating = true;
            LeaderboardItem.fetchIndividualScore(this);
            LeaderboardItem.fetchLeaderboard(skip, this);
        }
    }

    protected void showProgressBar() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    protected void hideProgressBar() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);

    }

    public void avatarClicked(View view) {
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, User.getCurrentUser());
        startActivity(intent);
    }

    private class LeaderboardAdapter extends ArrayAdapter<LeaderboardItem> {

        public LeaderboardAdapter(Context c, List<LeaderboardItem> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LeaderboardView view = (LeaderboardView)convertView;
            if (view == null) {
                view = LeaderboardView.inflate(parent);
            }
            view.setData(getItem(position), position);
            view.setDelegate(SurveyFragment.this);
//            int lastVisiblePos = mListView.getLastVisiblePosition();

            return view;
        }
    }



    protected void update(Object data) {
        if (data instanceof ArrayList) {
            ArrayList<?> items = (ArrayList<?>)data;
            for (Object item : items) {
                if (item instanceof LeaderboardItem) {
                    boolean updated = false;
                    for (int i = 0; i != mAdapter.getCount(); ++i) {
                        LeaderboardItem current = mAdapter.getItem(i);
                        if (current.userId.equals(((LeaderboardItem) item).userId)) {
                            mAdapter.insert((LeaderboardItem)item, i);
                            mAdapter.remove(current);
                            updated = true;
                        }
                    }
                    if (!updated) {
                        mAdapter.add((LeaderboardItem)item);
                    }
                }
            }
            mAdapter.notifyDataSetChanged();
        } else {
            if (data instanceof LeaderboardItem) {
                LeaderboardItem item = (LeaderboardItem)data;
                if (item.userFirstName != null && item.organizationNamespace != null) {
                    mUserNameLabel.setText(String.format("%s@%s", item.userFirstName.toLowerCase(),
                            item.organizationNamespace.toLowerCase()));
                }
                if (item.points > 0) {
                    mUserPointsLabel.setText(String.valueOf(item.points));
                }
                if (item.surveyCount > 0) {
                    mSurveyCountLabel.setText(String.valueOf(item.surveyCount));
                }

                if (item.userProfilePhotoUrl != null) {
                    PrizmDiskCache.getInstance(getContext()).fetchBitmap(item.userProfilePhotoUrl,
                            mAvatarView.getWidth(), new ImageHandler(this, mAvatarView));
                }

                if (item.rank != null) {
                    mUserRankLabel.setText(item.rank);
                }

            }
        }
    }

    private static class ImageHandler extends Handler {

        private SurveyFragment mActivity;
        private ImageView mImageView;

        public ImageHandler(SurveyFragment activity, ImageView iv) {

            mActivity = activity;
            mImageView = iv;
        }

        public void handleMessage(Message msg) {

            Bitmap bmp = (Bitmap)msg.obj;
            AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mActivity.getResources());
            Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(Bitmap.createScaledBitmap(bmp, 128, 128, false));
            mImageView.setImageDrawable(avatarDrawable);

        }
    }


}
