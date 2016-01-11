package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.w3c.dom.Text;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Insight;
import co.higheraltitude.prizm.models.Post;

/**
 * TODO: document your custom view class.
 */
public class InsightCardView extends RelativeLayout {

    private PrizmDiskCache mCache;
    private Insight mInsight;
    private String mInstanceId;

    private ImageView mImageView;
    private ImageView mAvatarView;
    private TextView mCreatorTextView;
    private TextView mInsightTitleView;
    private TextView mInsightLinkView;
    private ImageButton mLikeButton;
    private ImageButton mDislikeButton;

    private TextView mHashTagTextView;

    private InsightCardViewDelegate mDelegate;


    public static InsightCardView inflate(ViewGroup parent) {
        InsightCardView view = (InsightCardView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.insight_card_view, parent, false);
        return view;
    }

    public void setDelegate(InsightCardViewDelegate delegate) {
        mDelegate = delegate;
    }

    public InsightCardView(Context context){
        this(context, null);
    }

    public InsightCardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InsightCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setInsight(Insight insight) {

        mCache = PrizmDiskCache.getInstance(getContext());
        mInstanceId = insight.uniqueId;
        boolean loadPhotos = true;
        mInsight = insight;
        setViews();


        mCreatorTextView.setText(insight.creatorName);
        mInsightTitleView.setText(insight.title);
        mInsightLinkView.setText(insight.linkTitle);
        mInsightLinkView.setVisibility(View.GONE);
        String [] hashtags = mInsight.hashTags.split(",");
        if (hashtags.length > 0) {
            for (int i = 0; i != hashtags.length; ++i) {
                hashtags[i] = "#" + hashtags[i];
            }
        }
        String tags = TextUtils.join(" ", hashtags);
        mHashTagTextView.setText(tags);

        if (loadPhotos) {
            setPlaceHolderImage();
            mAvatarView.setImageResource(R.drawable.user_missing_avatar);

            mCache.fetchBitmap(insight.filePath, mImageView.getWidth(), new ImageHandler(this,
                    mImageView, mInstanceId, ImageHandler.POST_IMAGE_TYPE_IMAGE));
            mCache.fetchBitmap(mInsight.creatorProfilePhotoUrl, mAvatarView.getWidth(), new ImageHandler(this,
                    mAvatarView, mInstanceId, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        }


    }

    public Insight getInsight() {
        return mInsight;
    }

    private void setViews() {
        mImageView = (ImageView)findViewById(R.id.post_image_view);
        mImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.insightImageClicked(mInsight);
                }
            }
        });
        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.avatarButtonClicked(mInsight);
                }
            }
        });
        mCreatorTextView = (TextView)findViewById(R.id.creator_name);
        mInsightTitleView = (TextView)findViewById(R.id.title_view);
        mInsightLinkView = (TextView)findViewById(R.id.link_title);
        mInsightLinkView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.insightLinkClicked(mInsight);
                }
            }
        });

        mHashTagTextView = (TextView)findViewById(R.id.hash_tag_view);
        mLikeButton = (ImageButton)findViewById(R.id.like_button);
        mLikeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.likeButtonClicked(mInsight);
                }
            }
        });
        mDislikeButton = (ImageButton)findViewById(R.id.dislike_button);
        mDislikeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.dislikeButtonClicked(mInsight);
                }
            }
        });
    }

    private void setPlaceHolderImage(){
        int drawable = -1;

    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private InsightCardView mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(InsightCardView view, ImageView iv, String id, int type) {
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
                    if (bmp == null) {
                        mPostView.setPlaceHolderImage();
                    } else {
                        mImageView.setImageBitmap(bmp);
                        mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    }
                }
            }
        }
    }

    public interface InsightCardViewDelegate {
        void avatarButtonClicked(Insight insight);
        void likeButtonClicked(Insight insight);
        void dislikeButtonClicked(Insight insight);
        void insightImageClicked(Insight insight);
        void insightLinkClicked(Insight insight);
    }


}
