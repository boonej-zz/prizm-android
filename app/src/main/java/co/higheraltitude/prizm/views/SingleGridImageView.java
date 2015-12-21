package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Post;

/**
 * Created by boonej on 9/24/15.
 */
public class SingleGridImageView extends RelativeLayout implements View.OnClickListener {

    private SingleGridDelegate mDelegate;

    private ImageView mPostView;
    private Post mPost;
    private String mInstanceId;
    private Boolean needsUpdate;

    public static SingleGridImageView inflate(ViewGroup parent) {
        SingleGridImageView tagView = (SingleGridImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_grid_image_view, parent, false);
        return tagView;
    }

    public SingleGridImageView(Context context){
        this(context, null);
    }

    public SingleGridImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SingleGridImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDelegate(SingleGridDelegate delegate) {
        mDelegate = delegate;
    }

    private void setViews() {
        mPostView = (ImageView)findViewById(R.id.post);
        mPostView.setOnClickListener(this);
    }

    public void onClick(View view) {

        if (mDelegate != null) {
            mDelegate.postClicked(mPost);
        }
    }

    public void setPost(Post post) {
        setViews();
        mPost = post;
        String id = mPost.uniqueId;
        needsUpdate = true;
        if (mInstanceId != null) {
            if (id.equals(mInstanceId)) {
                needsUpdate = false;
            }
        }
        mInstanceId = id;
        PrizmDiskCache cache = PrizmDiskCache.getInstance(getContext());


        if (needsUpdate) {
            setPlaceHolderImage();
            int width = mPostView.getWidth();
            String path = mPost.filePath;
            String[] parts = path.split("|");
            if (parts.length == 2) {
                path = parts[0] + "_2." + parts[1];
            }
            cache.fetchBestBitmap(path, width,
                    new ImageHandler(this, mPostView, mInstanceId));

        }

    }

    private void setPlaceHolderImage(){
        int drawable = -1;
        if (mPost.category.equals(Post.CATEGORY_ACHIEVEMENT)) {
            drawable = R.drawable.achievement_icon;
        } else if (mPost.category.equals(Post.CATEGORY_ASPIRATION)) {
            drawable = R.drawable.aspitation_icon;
        } else if (mPost.category.equals(Post.CATEGORY_EXPERIENCE)) {
            drawable = R.drawable.experience_icon;
        } else if (mPost.category.equals(Post.CATEGORY_INSPIRATION)) {
            drawable = R.drawable.inspiration_icon;
        } else if (mPost.category.equals(Post.CATEGORY_PASSION)) {
            drawable = R.drawable.passion_icon;
        } else if (mPost.category.equals(Post.CATEGORY_PRIVATE)) {
            drawable = R.drawable.private_icon;
        }
        if (drawable != -1) {
            mPostView.setScaleType(ImageView.ScaleType.CENTER);
            mPostView.setImageResource(drawable);
        } else {
            mPostView.setImageBitmap(null);
        }
    }

    public interface SingleGridDelegate {
        void postClicked(Post post);
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private SingleGridImageView mPostView;
        private ImageView mImageView;

        public ImageHandler(SingleGridImageView view, ImageView iv, String id) {
            mInstanceId = id;
            mPostView = view;
            mImageView = iv;
        }

        public void handleMessage(Message msg) {
            if (mPostView.mInstanceId.equals(mInstanceId)) {
                Bitmap bmp = (Bitmap)msg.obj;
                if (bmp == null) {
                    mPostView.setPlaceHolderImage();
                } else {
                    mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    mImageView.setImageBitmap(bmp);
                }
            }
        }
    }



}
