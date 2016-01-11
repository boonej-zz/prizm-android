package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.LeaderboardItem;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class LeaderboardView extends RelativeLayout implements View.OnClickListener{

    private ImageView mAvatarView;
    private ImageView mRewardView;
    private TextView mPointsLabel;
    private TextView mPositionLabel;
    private TextView mUserLabel;

    private LeaderboardItem mItem;
    private String mInstanceId;

    private LeaderboardViewDelegate mDelegate;


    public static LeaderboardView inflate(ViewGroup parent) {
        LeaderboardView view = (LeaderboardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.leaderboard_view, parent, false);
        return view;
    }

    public LeaderboardView(Context context){
        this(context, null);
    }

    public LeaderboardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LeaderboardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    protected void setViews() {
        mAvatarView = (ImageView)findViewById(R.id.user_avatar);
        mAvatarView.setOnClickListener(this);
        mRewardView = (ImageView)findViewById(R.id.reward_icon);
        mPointsLabel = (TextView)findViewById(R.id.points_label);
        mPositionLabel = (TextView)findViewById(R.id.position_label);
        mUserLabel = (TextView)findViewById(R.id.user_name);
    }

    public void setDelegate(LeaderboardViewDelegate delegate) {
        mDelegate = delegate;
    }


    public void setData(LeaderboardItem item, int position) {
        setViews();

        mInstanceId = item.userId;
        mItem = item;
        mAvatarView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.user_missing_avatar));

        PrizmDiskCache cache = PrizmDiskCache.getInstance(getContext());
        cache.fetchBitmap(mItem.userProfilePhotoUrl, mAvatarView.getWidth(),
                new ImageHandler(this, mAvatarView, mInstanceId));
        mUserLabel.setText(mItem.userName);
        mPositionLabel.setText(String.valueOf(position + 1));
        mPointsLabel.setText(String.valueOf(mItem.points) + " Pts");
        if (mItem.rank.equals("1st")) {
            mRewardView.setImageResource(R.drawable.reward_1_icon);
            mRewardView.setVisibility(VISIBLE);
        } else if (mItem.rank.equals("2nd")) {
            mRewardView.setImageResource(R.drawable.reward_2_icon);
            mRewardView.setVisibility(VISIBLE);
        } else if (mItem.rank.equals("3rd")) {
            mRewardView.setImageResource(R.drawable.reward_3_icon);
            mRewardView.setVisibility(VISIBLE);
        } else {
            mRewardView.setVisibility(INVISIBLE);
        }

    }


    public User getUser() {
        return mItem.extractUser();
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private LeaderboardView mView;
        private ImageView mImageView;

        public ImageHandler(LeaderboardView view, ImageView iv, String id) {
            mInstanceId = id;
            mView = view;
            mImageView = iv;
        }

        public void handleMessage(Message msg) {
            if (mView.mInstanceId.equals(mInstanceId)) {
                Bitmap bmp = (Bitmap)msg.obj;
                AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mView.getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(Bitmap.createScaledBitmap(bmp, 128, 128, false));
                mImageView.setImageDrawable(avatarDrawable);
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (mDelegate != null) {
            mDelegate.avatarViewClicked(this.mItem.extractUser());
        }
    }

    public interface LeaderboardViewDelegate {
        void avatarViewClicked(User user);
    }


}
