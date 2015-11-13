package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Activity;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.Trust;

/**
 * TODO: document your custom view class.
 */
public class TrustRequestView extends RelativeLayout {

    private PrizmDiskCache mCache;
    private Trust mTrust;
    private String mInstanceId;

    private ImageView mAvatarView;
    private TextView mCreatorTextView;
    private TextView mDateAgoTextView;
    private TextView mMessageView;

    private TrustRequestDelegate mDelegate;


    public void setDelegate(TrustRequestDelegate delegate) {
        mDelegate = delegate;
    }

    public static TrustRequestView inflate(ViewGroup parent) {
        TrustRequestView view = (TrustRequestView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trust_request_view, parent, false);
        return view;
    }


    public TrustRequestView(Context context){
        this(context, null);
    }

    public TrustRequestView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TrustRequestView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setTrust(Trust trust) {
        setViews();
        if (mCache == null) {
            mCache = PrizmDiskCache.getInstance(getContext());
        }
        boolean updatePhoto = true;
        if (mTrust != null) {
            if (mTrust.uniqueId == trust.uniqueId) {
                updatePhoto = false;
            }
        }
        mTrust = trust;
        mInstanceId = mTrust.uniqueId;

        String timeSince = String.format("%s ago", mTrust.timeSince);
        mDateAgoTextView.setText(timeSince);

        mCreatorTextView.setText(mTrust.fromName);
        if (updatePhoto) {
            mAvatarView.setImageResource(R.drawable.user_missing_avatar);
            mCache.fetchBitmap(mTrust.fromProfilePhotoUrl, mAvatarView.getWidth(),
                    new ImageHandler(this, mAvatarView, this.mInstanceId,
                            ImageHandler.POST_IMAGE_TYPE_AVATAR));
        }


    }

    public Trust getTrust() {
        return mTrust;
    }

    private void setViews() {
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.avatarButtonClicked(mTrust);
                }
            }
        });
        mCreatorTextView = (TextView)findViewById(R.id.notification_from);
        mMessageView = (TextView)findViewById(R.id.notification_message);
        mDateAgoTextView = (TextView)findViewById(R.id.notification_time_ago);
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private TrustRequestView mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(TrustRequestView view, ImageView iv, String id, int type) {
            mInstanceId = id;
            mPostView = view;
            mImageView = iv;
            mType = type;
        }

        public void handleMessage(Message msg) {
            if (mPostView.mInstanceId.equals(mInstanceId)) {
                Bitmap bmp = (Bitmap)msg.obj;
                if (mType == POST_IMAGE_TYPE_AVATAR) {
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mPostView.getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                    mImageView.setImageDrawable(avatarDrawable);
                } else if (mType == POST_IMAGE_TYPE_IMAGE) {
                    mImageView.setImageBitmap(bmp);
                }
            }
        }
    }

    public interface TrustRequestDelegate {
        void avatarButtonClicked(Trust trust);
    }


}
