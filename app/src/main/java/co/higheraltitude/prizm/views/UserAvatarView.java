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
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class UserAvatarView extends RelativeLayout implements View.OnClickListener{

    private TextView mTextView;
    private ImageView mAvatarView;
    private ImageView mToggle;
    private User mUser;
    private Boolean mSelectable = false;
    private Boolean mSelected = false;
    private UserAvatarViewDelegate mDelegate;

    private String instanceId;

    public static UserAvatarView inflate(ViewGroup parent) {
        UserAvatarView tagView = (UserAvatarView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_avatar_view, parent, false);
        return tagView;
    }

    public UserAvatarView(Context context){
        this(context, null);
    }

    public UserAvatarView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserAvatarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mAvatarView = (ImageView)findViewById(R.id.user_avatar);
        mAvatarView.setOnClickListener(this);
        mTextView = (TextView)findViewById(R.id.user_name);
        mToggle = (ImageView)findViewById(R.id.user_toggle);
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
        if (mSelectable) {
            mToggle.setVisibility(VISIBLE);
        }
        instanceId = UUID.randomUUID().toString();
        mUser = user;
        mAvatarView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.user_missing_avatar));

        PrizmDiskCache cache = PrizmDiskCache.getInstance(getContext());
        cache.fetchBitmap(user.profilePhotoURL, mAvatarView.getWidth(), new ImageHandler(this, mAvatarView, instanceId));
        mTextView.setText(user.name);
        if (mSelected) {
            Log.d("DEBUG", user.name + ": SELECTED");
            mToggle.setImageResource(R.drawable.radio_blue_full);
        } else {
            Log.d("DEBUG", user.name + ": UNSELECTED");
            mToggle.setImageResource(R.drawable.radio_blue_empty);
        }
    }


    public User getUser() {
        return mUser;
    }



    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mSelected = selected;
        mSelected = selected;
        if (mSelected) {
            if (mToggle != null) {
                if (mSelected) {
                    Log.d("DEBUG", mUser.name + ": SELECTED");
                    mToggle.setImageResource(R.drawable.radio_blue_full);
                } else {
                    Log.d("DEBUG", mUser.name + ": UNSELECTED");
                    mToggle.setImageResource(R.drawable.radio_blue_empty);
                }
            }
        }
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private UserAvatarView mView;
        private ImageView mImageView;

        public ImageHandler(UserAvatarView view, ImageView iv, String id) {
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
        void avatarViewClicked(UserAvatarView view);
    }


}
