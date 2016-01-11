package co.higheraltitude.prizm;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.util.Objects;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Insight;
import co.higheraltitude.prizm.models.User;

public class FullBleedInsight extends AppCompatActivity {

    public static final String EXTRA_INSIGHT= "extra_insight";
    public static final String EXTRA_RESULT_LIKED = "extra_result_liked";
    public static final String EXTRA_INSIGHT_ID = "extra_insight_id";

    private ImageView mAvatarView;
    private TextView mCreatorName;
    private ImageView mImageView;
    private TextView mHashtagView;
    private TextView mTitleView;
    private TextView mBodyView;
    private TextView mLinkView;

    private Insight mInsight;
    private String mInsightId;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_bleed_insight);
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mCreatorName = (TextView)findViewById(R.id.creator_name);
        mImageView = (ImageView)findViewById(R.id.post_image_view);
        mHashtagView = (TextView)findViewById(R.id.hash_tag_view);
        mTitleView = (TextView)findViewById(R.id.insight_title);
        mBodyView = (TextView)findViewById(R.id.insight_text);
        mLinkView = (TextView)findViewById(R.id.insight_link);
        mInsight = getIntent().getParcelableExtra(EXTRA_INSIGHT);
        Toolbar toolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.close_cancel_white_icon);
        toolbar.setNavigationOnClickListener(new BackClickListener(this));
        mInsightId = getIntent().getStringExtra(EXTRA_INSIGHT_ID);
        if (mInsightId == null) {
            loadViews();
        } else {
            Insight.fetchInsight(mInsightId, new PrizmDiskCache.CacheRequestDelegate() {
                @Override
                public void cached(String path, Object object) {
                    process(object);
                }

                @Override
                public void cacheUpdated(String path, Object object) {
                    process(object);
                }

                private void process(Object object) {
                    if (object != null && object instanceof Insight) {
                        mInsight = (Insight)object;
                        loadViews();
                    }
                }
            });
        }

    }

    public void onLikeClick(View view) {
        Insight.likeInsight(mInsight, new InsightLikeHandler(mInsight, this, true));
    }

    public void onDislikeClick(View view) {
        Insight.dislikeInsight(mInsight, new InsightLikeHandler(mInsight, this, false));
    }

    public void onLinkClick(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mInsight.link));
        startActivity(browserIntent);
    }

    public void onImageAreaClick(View view) {
        finish();
        overridePendingTransition(0, 0);
    }

    public void onAvatarClick(View view) {
        User user = new User();
        user.uniqueID = mInsight.creatorId;
        user.profilePhotoURL = mInsight.creatorProfilePhotoUrl;
        user.type = mInsight.creatorType;
        user.subtype = mInsight.creatorSubtype;
        user.name = mInsight.creatorName;
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }

    private void loadViews() {
        mCreatorName.setText(mInsight.creatorName);
        String [] hashtags = mInsight.hashTags.split(",");
        if (hashtags.length > 0) {
            for (int i = 0; i != hashtags.length; ++i) {
                if (!hashtags[i].equals(""))
                    hashtags[i] = "#" + hashtags[i];
            }
        }
        String tags = TextUtils.join(" ", hashtags);
        mHashtagView.setText(tags);
        mTitleView.setText(mInsight.title);
        mBodyView.setText(mInsight.text);
        mLinkView.setText(mInsight.linkTitle);
        PrizmDiskCache cache = PrizmDiskCache.getInstance(getApplicationContext());
        cache.fetchBitmap(mInsight.creatorProfilePhotoUrl, mAvatarView.getWidth(),
                new ImageHandler(this, mAvatarView, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        cache.fetchBitmap(mInsight.filePath, mImageView.getWidth(),
                new ImageHandler(this, mImageView, ImageHandler.POST_IMAGE_TYPE_IMAGE));
    }

    private static class ImageHandler extends Handler {
        private FullBleedInsight mInsightView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(FullBleedInsight view, ImageView iv, int type) {
            mInsightView = view;
            mImageView = iv;
            mType = type;
        }

        public void handleMessage(Message msg) {
            Bitmap bmp = (Bitmap)msg.obj;
            if (mType == POST_IMAGE_TYPE_AVATAR) {
                AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(mInsightView.getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                mImageView.setImageDrawable(avatarDrawable);
            } else if (mType == POST_IMAGE_TYPE_IMAGE) {
                mImageView.setImageBitmap(bmp);
            }
        }
    }


    private static class InsightLikeHandler extends Handler {

        private Insight mInsight;
        private Activity mActivity;
        private Boolean mLiked;

        public InsightLikeHandler(Insight insight, Activity activity, Boolean liked) {

            mInsight = insight;
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message message){
            Intent intent = new Intent();
            intent.putExtra(EXTRA_RESULT_LIKED, mLiked);
            intent.putExtra(EXTRA_INSIGHT, mInsight);
            mActivity.setResult(RESULT_OK, intent);
            mActivity.finish();
            mActivity.overridePendingTransition(0, 0);
        }
    }
}
