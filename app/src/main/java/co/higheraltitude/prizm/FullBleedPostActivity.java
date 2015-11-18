package co.higheraltitude.prizm;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.models.User;

public class FullBleedPostActivity extends AppCompatActivity {

    public static final String EXTRA_POST = "extra_post";

    private Post mPost;

    private ImageView mPostImageView;
    private ImageView mAvatarView;
    private TextView mCreatorTextView;
    private TextView mDateAgoTextView;
    private TextView mPostViaText;
    private TextView mLikesCount;
    private TextView mCommentCount;
    private View mLikesButton;
    private ImageView mLikesImageView;
    private ImageView mCategoryImageView;
    private View mCommentButton;
    private PrizmDiskCache mCache;
    private TextView mPostTextCreator;
    private TextView mPostText;
    private View mPostTextArea;
    private ImageView mPostTextAvatar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_bleed_post);
        configureActionBar();
        mCache = PrizmDiskCache.getInstance(getApplicationContext());
        mPost = getIntent().getParcelableExtra(EXTRA_POST);
        configureViews();
        layoutPost();
    }

    private void configureActionBar()
    {
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        actionBar.hideOverflowMenu();
    }

    private void configureViews()
    {
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mPostViaText = (TextView)findViewById(R.id.post_via_text);
        mCreatorTextView = (TextView)findViewById(R.id.creator_name);
        mLikesCount = (TextView)findViewById(R.id.likes_count);
        mLikesButton = findViewById(R.id.likes_button);
        mLikesImageView = (ImageView)findViewById(R.id.likes_image);
        mDateAgoTextView = (TextView)findViewById(R.id.date_text);
        mCategoryImageView = (ImageView)findViewById(R.id.category_image);
        mPostImageView = (ImageView)findViewById(R.id.post_image_view);
        mCommentCount = (TextView)findViewById(R.id.comment_count);
        mPostText = (TextView)findViewById(R.id.post_text);
        mPostTextCreator = (TextView)findViewById(R.id.post_text_creator_name);
        mPostTextAvatar = (ImageView)findViewById(R.id.post_text_avatar);
        mPostTextArea = findViewById(R.id.post_text_area);

    }

    private void layoutPost()
    {
        if (mPost.externalProvider != null && !mPost.externalProvider.isEmpty()) {
            String provider = mPost.externalProvider.substring(0, 1).toUpperCase() +
                    mPost.externalProvider.substring(1);
            mPostViaText.setText(String.format("Post via %s", provider));
        }
        mCreatorTextView.setText(mPost.creatorName);
        mDateAgoTextView.setText(String.format("%s ago", mPost.timeSince));
        setCategoryImage();
        mCache.fetchBitmap(mPost.creatorProfilePhotoUrl, mAvatarView.getWidth(),
                new ImageHandler(this, mAvatarView, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        mCache.fetchBitmap(mPost.filePath, mPostImageView.getWidth(),
                new ImageHandler(this, mPostImageView, ImageHandler.POST_IMAGE_TYPE_IMAGE));
        mCommentCount.setText(String.valueOf(mPost.commentsCount));
        mLikesCount.setText(String.valueOf(mPost.likesCount));
        if (mPost.commentsCount == 0) {
            mCommentCount.setVisibility(View.INVISIBLE);
        }
        if (mPost.likesCount == 0) {
            mLikesCount.setVisibility(View.INVISIBLE);
        }
        setPostText();
    }

    private void setCategoryImage()
    {
        if (mPost.category.equals("aspiration")) {
            mCategoryImageView.setImageResource(R.drawable.aspitation_active_icon);
        } else if (mPost.category.equals("passion")) {
            mCategoryImageView.setImageResource(R.drawable.passion_active_icon);
        } else if (mPost.category.equals("experience")) {
            mCategoryImageView.setImageResource(R.drawable.experience_active_icon);
        } else if (mPost.category.equals("inspiration")) {
            mCategoryImageView.setImageResource(R.drawable.inspiration_active_icon);
        } else if (mPost.category.equals("personal")) {
            mCategoryImageView.setImageResource(R.drawable.private_active_icon);
        } else if (mPost.category.equals("achievement")) {
            mCategoryImageView.setImageResource(R.drawable.achievement_active_icon);
        }
    }

    private void setPostText()
    {
        if (mPost.text != null && !mPost.text.isEmpty()) {
            mPostTextArea.setVisibility(View.VISIBLE);
            mPostText.setText(mPost.text);
            mPostTextCreator.setText(mPost.creatorName);
            mCache.fetchBitmap(mPost.creatorProfilePhotoUrl, mPostTextAvatar.getWidth(),
                    new ImageHandler(this, mPostTextAvatar, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        }
    }

    private static class ImageHandler extends Handler {
        private FullBleedPostActivity mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(FullBleedPostActivity view, ImageView iv, int type) {
            mPostView = view;
            mImageView = iv;
            mType = type;
        }

        public void handleMessage(Message msg) {
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
