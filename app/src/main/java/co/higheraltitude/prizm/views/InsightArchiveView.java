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
import co.higheraltitude.prizm.models.Insight;

/**
 * TODO: document your custom view class.
 */
public class InsightArchiveView extends RelativeLayout {

    private PrizmDiskCache mCache;
    private Insight mInsight;
    private String mInstanceId;


    private ImageView mAvatarView;
    private TextView mInsightTitleView;


    private InsightArchiveViewDelegate mDelegate;


    public static InsightArchiveView inflate(ViewGroup parent) {
        InsightArchiveView view = (InsightArchiveView)LayoutInflater.from(parent.getContext())
                .inflate(R.layout.insight_archive_view, parent, false);
        return view;
    }

    public void setDelegate(InsightArchiveViewDelegate delegate) {
        mDelegate = delegate;
    }

    public InsightArchiveView(Context context){
        this(context, null);
    }

    public InsightArchiveView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InsightArchiveView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    public void setInsight(Insight insight) {

        mCache = PrizmDiskCache.getInstance(getContext());
        mInstanceId = insight.uniqueId;
        boolean loadPhotos = mInsight == null || !insight.uniqueId.equals(mInsight.uniqueId);
        mInsight = insight;
        setViews();

        mInsightTitleView.setText(insight.title);

        if (loadPhotos) {
            setPlaceHolderImage();
            mAvatarView.setImageResource(R.drawable.user_missing_avatar);

            mCache.fetchBitmap(mInsight.creatorProfilePhotoUrl, mAvatarView.getWidth(), new ImageHandler(this,
                    mAvatarView, mInstanceId, ImageHandler.POST_IMAGE_TYPE_AVATAR));
        }


    }

    public Insight getInsight() {
        return mInsight;
    }

    private void setViews() {

        mAvatarView = (ImageView)findViewById(R.id.avatar_view);
        mAvatarView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDelegate != null) {
                    mDelegate.avatarButtonClicked(mInsight);
                }
            }
        });

        mInsightTitleView = (TextView)findViewById(R.id.title_view);

    }

    private void setPlaceHolderImage(){
        int drawable = -1;

    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private InsightArchiveView mPostView;
        private ImageView mImageView;
        private int mType;
        public static int POST_IMAGE_TYPE_AVATAR = 0;
        public static int POST_IMAGE_TYPE_IMAGE = 1;

        public ImageHandler(InsightArchiveView view, ImageView iv, String id, int type) {
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

    public interface InsightArchiveViewDelegate {
        void avatarButtonClicked(Insight insight);
    }


}
