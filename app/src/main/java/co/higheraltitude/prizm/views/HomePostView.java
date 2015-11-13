package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;

/**
 * TODO: document your custom view class.
 */
public class HomePostView extends RelativeLayout {

    private PrizmDiskCache mCache;
    private Post mPost;
    private String mInstanceId;

    private ImageView mPostImageView;
    private ImageView mAvatarView;
    private TextView mCreatorTextView;
    private TextView mDateAgoTextView;
    private TextView mPostViaText;
    private TextView mLikesCount;
    private TextView mCommentCount;

    private HomePostViewDelegate mDelegate;


    public static HomePostView inflate(ViewGroup parent) {
        HomePostView view = (HomePostView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_post_view, parent, false);
        return view;
    }

    public void setDelegate(HomePostViewDelegate delegate) {
        mDelegate = delegate;
    }

    public HomePostView(Context context){
        this(context, null);
    }

    public HomePostView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HomePostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setPost(Post post) {

        mCache = PrizmDiskCache.getInstance(getContext());
        mInstanceId = post.uniqueId;
        boolean loadPhotos = mPost == null || !post.uniqueId.equals(mPost.uniqueId);
        mPost = post;
        setViews();

        mCreatorTextView.setText(mPost.creatorName);
        mDateAgoTextView.setText(String.format("%s ago", mPost.timeSince));
        mPostViaText.setText(null);
        if (mPost.likesCount == 0) {
            mLikesCount.setText(null);
        } else {
            mLikesCount.setText(String.valueOf(mPost.likesCount));
        }
        if (mPost.commentsCount == 0) {
            mCommentCount.setText(null);
        } else {
            mCommentCount.setText(String.valueOf(mPost.commentsCount));
        }
        if (mPost.externalProvider != null && !mPost.externalProvider.isEmpty()) {
            String provider = mPost.externalProvider.substring(0, 1).toUpperCase() +
                    mPost.externalProvider.substring(1);
            mPostViaText.setText(String.format("Post via %s", provider));
        }
        if (loadPhotos) {
            mPostImageView.setImageBitmap(null);
            mAvatarView.setImageResource(R.drawable.user_missing_avatar);

            mCache.fetchBitmap(mPost.filePath, mPostImageView.getWidth(), new ImageHandler(this,
                    mPostImageView, mInstanceId, ImageHandler.POST_IMAGE_TYPE_IMAGE));
            mCache.fetchBitmap(mPost.creatorProfilePhotoUrl, mPostImageView.getWidth(), new ImageHandler(this,
                    mAvatarView, mInstanceId, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        }

    }

    public Post getPost() {
        return mPost;
    }

    private void setViews() {
        mPostImageView = (ImageView)findViewById(R.id.post_image_view);
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.avatarButtonClicked(mPost);
                }
            }
        });
        mCreatorTextView = (TextView)findViewById(R.id.creator_name);
        mDateAgoTextView = (TextView)findViewById(R.id.date_text);
        mPostViaText = (TextView)findViewById(R.id.post_via_text);
        mLikesCount = (TextView)findViewById(R.id.likes_count);
        mCommentCount = (TextView)findViewById(R.id.comment_count);
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private HomePostView mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(HomePostView view, ImageView iv, String id, int type) {
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

    public interface HomePostViewDelegate {
        void avatarButtonClicked(Post post);
    }


}
