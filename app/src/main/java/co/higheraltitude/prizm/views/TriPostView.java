package co.higheraltitude.prizm.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.UUID;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Interest;
import co.higheraltitude.prizm.models.Post;

/**
 * Created by boonej on 9/24/15.
 */
public class TriPostView extends RelativeLayout implements View.OnClickListener {

    private TriPostViewDelegate mDelegate;

    private ImageView mPostView1;
    private ImageView mPostView2;
    private ImageView mPostView3;
    private ArrayList<Post> mPosts;
    private String mInstanceId;
    private Boolean needsUpdate;

    public static TriPostView inflate(ViewGroup parent) {
        TriPostView tagView = (TriPostView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tri_post_view, parent, false);
        return tagView;
    }

    public TriPostView(Context context){
        this(context, null);
    }

    public TriPostView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TriPostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setDelegate(TriPostViewDelegate delegate) {
        mDelegate = delegate;
    }

    private void setViews() {
        mPostView1 = (ImageView)findViewById(R.id.post_1);
        mPostView1.setOnClickListener(this);
        mPostView2 = (ImageView)findViewById(R.id.post_2);
        mPostView2.setOnClickListener(this);
        mPostView3 = (ImageView)findViewById(R.id.post_3);
        mPostView3.setOnClickListener(this);
    }

    public void onClick(View view) {
        int position = 0;
        if (view == mPostView1) {
            position = 0;
        } else if (view == mPostView2) {
            position = 1;
        } else if (view == mPostView3) {
            position = 2;
        }

        if (mDelegate != null) {
            mDelegate.postClicked(mPosts.get(position));
        }
    }

    public void setPosts(ArrayList<Post> list) {
        setViews();
        String id = list.get(0).uniqueId;
        needsUpdate = true;
        if (mInstanceId != null) {
            if (id.equals(mInstanceId)) {
                needsUpdate = false;
            }
        }
        mInstanceId = id;
        PrizmDiskCache cache = PrizmDiskCache.getInstance(getContext());
        mPosts = list;
        if (needsUpdate) {
            int width = mPostView1.getWidth();
            mPostView1.setImageBitmap(null);
            mPostView2.setImageBitmap(null);
            mPostView3.setImageBitmap(null);
            for (int i = 0; i != list.size(); ++i) {
                String path = list.get(i).filePath;
                String[] parts = path.split("|");
                if (parts.length == 2) {
                    path = parts[0] + "_2." + parts[1];
                }
                switch (i) {
                    case 0:
                        cache.fetchBitmap(path, width,
                                new ImageHandler(this, mPostView1, mInstanceId));

                        break;
                    case 1:
                        cache.fetchBitmap(path, width,
                                new ImageHandler(this, mPostView2, mInstanceId));
                        break;
                    case 2:
                        cache.fetchBitmap(path, width,
                                new ImageHandler(this, mPostView3, mInstanceId));
                        break;
                    default:
                        break;
                }
            }
        }

    }

    public interface TriPostViewDelegate {
        void postClicked(Post post);
    }

    private static class ImageHandler extends Handler {
        private String mInstanceId;
        private TriPostView mPostView;
        private ImageView mImageView;

        public ImageHandler(TriPostView view, ImageView iv, String id) {
            mInstanceId = id;
            mPostView = view;
            mImageView = iv;
        }

        public void handleMessage(Message msg) {
            if (mPostView.mInstanceId.equals(mInstanceId)) {
                Bitmap bmp = (Bitmap)msg.obj;
                mImageView.setImageBitmap(bmp);
            }
        }
    }



}
