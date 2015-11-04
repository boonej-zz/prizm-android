package co.higheraltitude.prizm;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.InputStream;
import java.net.URL;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.models.User;

public class ProfileActivity extends AppCompatActivity implements PrizmDiskCache.CacheRequestDelegate {

    private TextView textView = null;
    private TextView locationView = null;
    private ImageView coverPhotoView = null;
    private Bitmap coverImage = null;
    private ImageView avatarView = null;
    private ImageView badgeView = null;
    private ProgressBar progressBar = null;
    private View locationArea = null;
    private User user = null;

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
        User profile = intent.getParcelableExtra(LoginActivity.EXTRA_PROFILE);
        User.fetchUserCore(profile, this);
        textView = (TextView)findViewById(R.id.profile_name);
        avatarView = (ImageView)findViewById(R.id.profile_avatar_photo);
        locationView = (TextView)findViewById(R.id.profile_location);
        locationArea = findViewById(R.id.profile_location_area);
        locationArea.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);
        mEditProfileButton = findViewById(R.id.edit_profile_button);
        if (profile.uniqueID.equals(User.getCurrentUser().uniqueID)) {
            mEditProfileButton.setVisibility(View.VISIBLE);
        }
        configureViews();
    }

    private void configureViews() {
        if (user != null) {
            coverPhotoView = (ImageView) findViewById(R.id.profile_cover_photo);
            badgeView = (ImageView) findViewById(R.id.avatar_badge);
            if (user.role != null && user.role.equals("ambassador")) {
                badgeView.setImageResource(R.drawable.ambassador_badge);
            } else if (user.role != null && user.role.equals("leader")) {
                badgeView.setImageResource(R.drawable.leader_badge);
            } else if (user.subtype != null && user.subtype.equals("luminary")) {
                badgeView.setImageResource(R.drawable.luminary_badge);
            } else {
                badgeView.setVisibility(View.GONE);
            }
            textView.setText(user.name);

            String locationString = "";
            if (user.city != null) {
                locationString = user.city;
                if (user.state != null) {
                    locationString = locationString + ", " + user.state;
                }
            } else if (user.state != null) {
                locationString = user.state;
            }

            locationView.setText(locationString);
            if (!locationString.isEmpty()) {
                locationArea.setVisibility(View.VISIBLE);
            }

//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inMutable = false;
//        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile_default_avatar, options);
//        AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
//        Drawable avatarDrawable = avatarDrawableFactory.getBorderedRoundedAvatarDrawable(Bitmap.createScaledBitmap(bmp, 128, 128, false));
//        avatarView.setImageDrawable(avatarDrawable);

            PrizmDiskCache cache = PrizmDiskCache.getInstance(getApplicationContext());
            if (user.coverPhotoURL != null && !user.coverPhotoURL.isEmpty()) {

                cache.fetchBitmap(user.coverPhotoURL, coverPhotoView.getWidth(), new CoverGradientHandler(coverPhotoView,
                        (ImageView) findViewById(R.id.cover_photo_gradient)));

            }
            if (user.profilePhotoURL != null && !user.profilePhotoURL.isEmpty()) {
                new LoadImage().execute(user.profilePhotoURL, "avatar");
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
                    coverPhotoView.setImageBitmap(image);
                } else if (field == "avatar") {
                    AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
                    Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(Bitmap.createScaledBitmap(image, 128, 128, false));
                    avatarView.setImageDrawable(avatarDrawable);
                }
            }else{
                Log.d("DEBUG", "Image Does Not exist or Network Error");
            }
        }
    }

    @Override
    public void cached(String path, Object object) {
        fillProfile(object);
    }

    @Override
    public void cacheUpdated(String path, Object object) {
        fillProfile(object);
    }

    private void fillProfile(Object object) {
        user = (User)object;
        configureViews();
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }

}
