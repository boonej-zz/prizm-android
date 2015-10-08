package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;

/**
 * Created by boonej on 9/24/15.
 */
public class UserTagView extends RelativeLayout {

    private TextView mTextView;
    private ImageView mAvatarView;
    private User mUser;

    private String instanceId;

    public static UserTagView inflate(ViewGroup parent) {
        UserTagView tagView = (UserTagView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_tag_view, parent, false);
        return tagView;
    }

    public UserTagView(Context context){
        this(context, null);
    }

    public UserTagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UserTagView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private void setViews() {
        mAvatarView = (ImageView)findViewById(R.id.user_avatar);
        mTextView = (TextView)findViewById(R.id.user_name);
    }

    public void setUser(User user) {
        setViews();
        instanceId = UUID.randomUUID().toString();
        mUser = user;
        mAvatarView.setImageBitmap(null);
        PrizmCache.getInstance().fetchDrawable(user.profilePhotoURL, new ImageHandler(this, mAvatarView, instanceId));
        mTextView.setText(user.name);
    }

    public User getUser() {
        return mUser;
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private UserTagView mView;
        private ImageView mImageView;

        public ImageHandler(UserTagView view, ImageView iv, String id) {
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

}
