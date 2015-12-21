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
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class UserFollowingAvatarView extends RelativeLayout implements View.OnClickListener{

    private TextView mTextView;
    private ImageView mAvatarView;
    private ImageView mFollowView;
    private User mUser;
    private Boolean mSelectable = false;
    private Boolean mSelected = false;
    private UserAvatarViewDelegate mDelegate;
    private View mUserFollowingButton;

    private String instanceId;

    public static UserFollowingAvatarView inflate(ViewGroup parent) {
        UserFollowingAvatarView tagView = (UserFollowingAvatarView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_following_avatar_view, parent, false);
        return tagView;
    }

    public UserFollowingAvatarView(Context context){
        this(context, null);
    }

    public UserFollowingAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserFollowingAvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mAvatarView = (ImageView)findViewById(R.id.user_avatar);
        mAvatarView.setOnClickListener(this);
        mTextView = (TextView)findViewById(R.id.user_name);
        mFollowView = (ImageView)findViewById(R.id.user_follow);
        mUserFollowingButton = findViewById(R.id.user_follow_button);
        mUserFollowingButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelected(!mUser.isFollowing);
                if (mDelegate != null) {
                    mDelegate.followButtonClicked(mUser);
                }
            }
        });
    }

    public void setDelegate(UserAvatarViewDelegate delegate) {
        mDelegate = delegate;
    }

    public void setSelectable(Boolean selectable) {
        mSelectable = selectable;
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setUser(User user) {
        setViews();

        instanceId = UUID.randomUUID().toString();
        mUser = user;
        mAvatarView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.user_missing_avatar));

        PrizmDiskCache cache = PrizmDiskCache.getInstance(getContext());
        cache.fetchBitmap(user.profilePhotoURL, mAvatarView.getWidth(), new ImageHandler(this, mAvatarView, instanceId));
        mTextView.setText(user.name);
        if (mSelected) {
            Log.d("DEBUG", user.name + ": SELECTED");
            mFollowView.setImageResource(R.drawable.following_icon);
        } else {
            Log.d("DEBUG", user.name + ": UNSELECTED");
            mFollowView.setImageResource(R.drawable.follow_icon);
        }
        setSelected(mUser.isFollowing);
    }


    public User getUser() {
        return mUser;
    }



    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mSelected = selected;
        if (mSelected) {
            if (mFollowView != null) {
                if (mSelected) {
                    mFollowView.setImageResource(R.drawable.following_icon);
                } else {
                    mFollowView.setImageResource(R.drawable.follow_icon);
                }
            }
        }
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private UserFollowingAvatarView mView;
        private ImageView mImageView;

        public ImageHandler(UserFollowingAvatarView view, ImageView iv, String id) {
            mInstanceId = id;
            mView = view;
            mImageView = iv;
        }

        public void handleMessage(Message msg) {
            if (mView.instanceId.equals(mInstanceId)) {
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
            mDelegate.avatarViewClicked(this);
        }

    }

    public static interface UserAvatarViewDelegate {
        void avatarViewClicked(UserFollowingAvatarView view);
        void followButtonClicked(User user);
    }


}
