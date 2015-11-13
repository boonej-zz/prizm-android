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

/**
 * TODO: document your custom view class.
 */
public class ActivityView extends RelativeLayout {

    private PrizmDiskCache mCache;
    private Activity mActivity;
    private String mInstanceId;

    private ImageView mAvatarView;
    private ImageView mImageView;
    private TextView mCreatorTextView;
    private TextView mDateAgoTextView;
    private TextView mMessageView;


    private ActivityViewDelegate mDelegate;


    public static ActivityView inflate(ViewGroup parent) {
        ActivityView view = (ActivityView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_view, parent, false);
        return view;
    }

    public void setDelegate(ActivityViewDelegate delegate) {
        mDelegate = delegate;
    }

    public ActivityView(Context context){
        this(context, null);
    }

    public ActivityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setActivity(Activity activity) {
        setViews();
        if (mCache == null) {
            mCache = PrizmDiskCache.getInstance(getContext());
        }
        boolean updatePhoto = true;
        if (mActivity != null) {
            if (mActivity.uniqueId == activity.uniqueId) {
                updatePhoto = false;
            }
        }
        mActivity = activity;
        mInstanceId = mActivity.uniqueId;
        String text = "";
        if (mActivity.action.equals(Activity.ACTIVITY_TYPE_LIKE)) {
            if (mActivity.commentId != null) {
                text = "Liked your comment.";
            } else if (mActivity.postId != null) {
                text = "Liked your post.";
            } else if (mActivity.messageId != null) {
                text = "Liked your message.";
            }
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_COMMENT)) {
            text = "Commented on your post.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_GROUP_ADD)) {
            text = "Added you to the " + mActivity.groupName + " group.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_INSIGHT)) {
            text = "Sent you an Insight.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_POST)) {
            text = "Created a new post.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_GROUP_APPROVE)) {
            text = "Approved your membership.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_TAG)) {
            if (mActivity.commentId != null) {
                text = "Tagged you in a comment.";
            } else if (mActivity.postId != null) {
                text = "Tagged you in a post.";
            }
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_TRUST_REQUEST)) {
            text = "Invited you to a trust.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_TRUST_ACCEPT)) {
            text = "Accepted your trust request.";
        } else if (mActivity.action.equals(Activity.ACTIVITY_TYPE_LEADER)) {
            text = "Made you a leader.";
        }
        String timeSince = String.format("%s ago", mActivity.timeSince);
        mDateAgoTextView.setText(timeSince);
        mMessageView.setText(text);
        mCreatorTextView.setText(mActivity.fromName);
        if (updatePhoto) {
            mAvatarView.setImageResource(R.drawable.user_missing_avatar);
            mImageView.setImageBitmap(null);
            mCache.fetchBitmap(mActivity.fromProfilePhotoUrl, mAvatarView.getWidth(),
                    new ImageHandler(this, mAvatarView, this.mInstanceId,
                            ImageHandler.POST_IMAGE_TYPE_AVATAR));
            if (mActivity.postFilePath != null) {
                mCache.fetchBitmap(mActivity.postFilePath, mImageView.getWidth(),
                        new ImageHandler(this, mImageView, this.mInstanceId,
                                ImageHandler.POST_IMAGE_TYPE_IMAGE));
            } else if (mActivity.insightFilePath != null) {
                mCache.fetchBitmap(mActivity.insightFilePath, mImageView.getWidth(),
                        new ImageHandler(this, mImageView, this.mInstanceId,
                                ImageHandler.POST_IMAGE_TYPE_IMAGE));
            }
        }


    }

    public Activity getActivity() {
        return mActivity;
    }

    private void setViews() {
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.avatarPhotoClicked(mActivity);
                }
            }
        });
        mCreatorTextView = (TextView)findViewById(R.id.notification_from);
        mMessageView = (TextView)findViewById(R.id.notification_message);
        mDateAgoTextView = (TextView)findViewById(R.id.notification_time_ago);
        mImageView = (ImageView)findViewById(R.id.notification_image_view);
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private ActivityView mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(ActivityView view, ImageView iv, String id, int type) {
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

    public interface ActivityViewDelegate {
        void avatarPhotoClicked(Activity activity);
    }


}
