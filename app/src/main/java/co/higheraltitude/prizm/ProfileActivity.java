package co.higheraltitude.prizm;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.InputStream;
import java.net.URL;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.PartnerInfoFragment;
import co.higheraltitude.prizm.fragments.ProfileInfoFragment;
import co.higheraltitude.prizm.fragments.ProfileMainFragment;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.models.User;

public class ProfileActivity extends AppCompatActivity implements PrizmDiskCache.CacheRequestDelegate,
        ViewPager.OnPageChangeListener {


    private ProgressBar progressBar = null;
    private User mUser = null;
    private ImageView mCoverPhotoView = null;
    private ViewPager mViewPager = null;
    private View mBlackOverlay = null;

    private View mEditProfileButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Object theme = PrizmCache.getInstance().objectCache.get("theme");
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
//        actionBar.hideOverflowMenu();

        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));

        Intent intent = getIntent();
        mUser = intent.getParcelableExtra(LoginActivity.EXTRA_PROFILE);
        User.fetchUserCore(mUser, this);

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);
        mEditProfileButton = findViewById(R.id.edit_profile_button);
        if (mUser.uniqueID.equals(User.getCurrentUser().uniqueID)) {
            mEditProfileButton.setVisibility(View.VISIBLE);
        }
        mViewPager = (ViewPager)findViewById(R.id.profile_view_pager);
        mViewPager.setAdapter(new ProfilePager(getSupportFragmentManager(), ProfileActivity.this));
        mViewPager.addOnPageChangeListener(this);

        configureViews();
    }

    private void configureViews() {
        mCoverPhotoView = (ImageView)findViewById(R.id.profile_cover_photo);
        mBlackOverlay = findViewById(R.id.black_overlay);



        if (mUser != null) {





//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inMutable = false;
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile_default_avatar, options);
//        AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
//        Drawable avatarDrawable = avatarDrawableFactory.getBorderedRoundedAvatarDrawable(Bitmap.createScaledBitmap(bmp, 128, 128, false));
//        avatarView.setImageDrawable(avatarDrawable);

            PrizmDiskCache cache = PrizmDiskCache.getInstance(getApplicationContext());
            if (mUser.coverPhotoURL != null && !mUser.coverPhotoURL.isEmpty()) {

                cache.fetchBitmap(mUser.coverPhotoURL, mCoverPhotoView.getWidth(), new CoverGradientHandler(mCoverPhotoView,
                        (ImageView) findViewById(R.id.cover_photo_gradient)));

            }

        }
    }

    public void editProfileClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), EditProfile.class);
        startActivity(intent);
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



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {
        private String field;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            Bitmap coverImage = null;
            try {
                coverImage = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());
                field = args[1];

            } catch (Exception e) {
                e.printStackTrace();
            }
            return coverImage;
        }

        protected void onPostExecute(Bitmap image) {
            if(image != null){
                if (field == "cover") {
                    mCoverPhotoView.setImageBitmap(image);
                } else if (field == "avatar") {

                }
            }else{
                Log.d("DEBUG", "Image Does Not exist or Network Error");
            }
        }
    }

    @Override
    public void cached(String path, Object object) {

    }

    @Override
    public void cacheUpdated(String path, Object object) {
        fillProfile(object);
    }

    private void fillProfile(Object object) {
        mUser = (User)object;
        ((ProfilePager)mViewPager.getAdapter()).setUser(mUser);
        configureViews();
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }

    public class ProfilePager extends FragmentPagerAdapter {

        final int PAGE_COUNT = 2;

        private Context context;

        public ProfilePager(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                return new ProfileMainFragment();
            } else {
                if (mUser != null && mUser.type != null && mUser.type.equals("institution_verified")) {
                    return new PartnerInfoFragment();
                } else {
                    return new ProfileInfoFragment();
                }
            }
        }

        public void setUser(User user) {
            ((ProfileMainFragment)getItem(0)).setUser(user);
            if (user.type != null && user.type.equals("institution_verified")) {
                ((PartnerInfoFragment) getItem(1)).setUser(user);
            } else {
                ((ProfileInfoFragment) getItem(1)).setUser(user);
            }
        }









//        @Override
//        public CharSequence getPageTitle(int position) {
//            return titles[position];
//        }


    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        if (position == 0) {
            mBlackOverlay.setVisibility(View.INVISIBLE);
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            mBlackOverlay.startAnimation(fadeOut);
        } else {
            if (mBlackOverlay.getVisibility() == View.INVISIBLE) {
                mBlackOverlay.setVisibility(View.VISIBLE);
                mBlackOverlay.setAlpha(0);
                final OvershootInterpolator oi = new OvershootInterpolator();
                ViewCompat.animate(mBlackOverlay).alpha(1.f).withLayer().setDuration(600).setInterpolator(oi).start();
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

}
