package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.support.v4.view.ViewPager;
import android.widget.ScrollView;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.InputStream;
import java.net.URL;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.EditPartnerFragment;
import co.higheraltitude.prizm.fragments.EditUserFragment;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.User;

public class EditProfile extends AppCompatActivity {

    public static final int RESULT_IMAGE = 932;
    public static final int RESULT_COVER_IMAGE = 933;

    private User mUser;

    private View mGradientView;
    private ImageView mCoverPhotoView;
    private ImageView mAvatarView;
    private Bitmap mCoverImage;
    private ViewPager mViewPager;

    private String mAvatarUrl;
    private String mCoverPhotoUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        layoutViews();
        loadData();
        ScrollView sv = (ScrollView)findViewById(R.id.scroll_view);
        
    }

    private void layoutViews() {
        mUser = User.getCurrentUser();
        mAvatarView = (ImageView)findViewById(R.id.profile_avatar_photo);
        mCoverPhotoView = (ImageView)findViewById(R.id.profile_cover_photo);
        mViewPager = (ViewPager)findViewById(R.id.edit_profile_view_pager);
        mViewPager.setAdapter(new EditPagerAdapter(getSupportFragmentManager(), getApplicationContext()));
        mAvatarUrl = mUser.profilePhotoURL;
        mCoverPhotoUrl = mUser.coverPhotoURL;
    }

    private void loadData() {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(getApplicationContext());
        if (mUser.coverPhotoURL != null && !mUser.coverPhotoURL.isEmpty()) {

            cache.fetchBitmap(mUser.coverPhotoURL, mCoverPhotoView.getWidth(), new CoverGradientHandler(mCoverPhotoView,
                    (ImageView) findViewById(R.id.cover_photo_gradient)));

        }
        if (mUser.profilePhotoURL != null && !mUser.profilePhotoURL.isEmpty()) {
            new LoadImage().execute(mUser.profilePhotoURL, "avatar");
        }
    }

    public void coverPhotoClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_COVER_IMAGE);
    }

    public void avatarPhotoClicked(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_IMAGE);
    }

    public String coverPhoto() {
        return mCoverPhotoUrl;
    }

    public String avatarPhoto() {
        return mAvatarUrl;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            String fieldName;
            if (requestCode == RESULT_IMAGE) {
                fieldName = "profile_photo_url";
            } else if (requestCode == RESULT_COVER_IMAGE) {
                fieldName = "profile_cover_photo_url";
            }
            if (data != null) {
                Uri selectedImage = data.getData();
                ImageHelper ih = ImageHelper.getInstance();
                Bitmap bmp = ih.bitmapFromUri(selectedImage);
                if (requestCode == RESULT_IMAGE) {
                    bmp = ih.cropToSquare(bmp);
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(bmp);
                    mAvatarView.setImageDrawable(avatarDrawable);
                } else if (requestCode == RESULT_COVER_IMAGE) {
                    bmp = ih.cropCoverPhoto(bmp);
                    mCoverPhotoView.setImageBitmap(bmp);
                }
                String path = ih.uploadImage(bmp);

                if (requestCode == RESULT_IMAGE) {
                    mAvatarUrl = path;
                } else if (requestCode == RESULT_COVER_IMAGE) {
                    mCoverPhotoUrl = path;
                }
            }


        }
    }

    private class CoverGradientHandler extends Handler {

        private ImageView mImageView;
        private  ImageView mGradientView;

        public CoverGradientHandler(ImageView imageView, ImageView gradientView) {
            mImageView = imageView;
            mGradientView = gradientView;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                mGradientView.setVisibility(View.VISIBLE);
                Bitmap bmp = (Bitmap)message.obj;
                mImageView.setImageBitmap(bmp);
            }
        }
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        private String field;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            try {
                mCoverImage = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
                field = args[1];

            } catch (Exception e) {
                e.printStackTrace();
            }
            return mCoverImage;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null){
                if (field == "cover") {
                    mCoverPhotoView.setImageBitmap(image);
                } else if (field == "avatar") {
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(Bitmap.createScaledBitmap(image, 128, 128, false));
                    mAvatarView.setImageDrawable(avatarDrawable);
                }
            }else{
                Log.d("DEBUG", "Image Does Not exist or Network Error");
            }
        }
    }

    private class EditPagerAdapter extends FragmentPagerAdapter {

        private Context mContext;

        public EditPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            if (mUser.type.equals("institution") || mUser.type.equals("institution_verified")) {
                fragment = EditPartnerFragment.newInstance(mUser);
            } else {
                fragment = EditUserFragment.newInstance(mUser);
            }
            return fragment;
        }

    }
}
