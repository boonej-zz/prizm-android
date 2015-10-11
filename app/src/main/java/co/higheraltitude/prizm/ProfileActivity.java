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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;


import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.InputStream;
import java.net.URL;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.models.User;

public class ProfileActivity extends AppCompatActivity {

    private TextView textView = null;
    private TextView locationView = null;
    private ImageView coverPhotoView = null;
    private Bitmap coverImage = null;
    private ImageView avatarView = null;
    private ImageView badgeView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Object theme = PrizmCache.getInstance().objectCache.get("theme");
        if (theme != null ) {
            setTheme((int)theme);
        } else {
            setTheme(R.style.PrizmBlue);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
//        actionBar.hideOverflowMenu();

        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));

        Intent intent = getIntent();
        User profile = intent.getParcelableExtra(LoginActivity.EXTRA_PROFILE);
        textView = (TextView)findViewById(R.id.profile_name);
        avatarView = (ImageView)findViewById(R.id.profile_avatar_photo);
        locationView = (TextView)findViewById(R.id.profile_location);

        coverPhotoView = (ImageView)findViewById(R.id.profile_cover_photo);
        badgeView = (ImageView)findViewById(R.id.avatar_badge);
        if (profile.role.equals("ambassador")) {
            badgeView.setImageResource(R.drawable.ambassador_badge);
        } else if (profile.role.equals("leader")) {
            badgeView.setImageResource(R.drawable.leader_badge);
        } else if (profile.subtype.equals("luminary")) {
            badgeView.setImageResource(R.drawable.luminary_badge);
        } else {
            badgeView.setVisibility(View.GONE);
        }
        textView.setText(profile.name);

        String locationString = "";
        if (profile.city != null) {
            locationString = profile.city;
            if (profile.state != null) {
                locationString = locationString + ", " + profile.state;
            }
        } else if (profile.state != null) {
            locationString = profile.state;
        }

        locationView.setText(locationString);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = false;
        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.profile_default_avatar, options);
        AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
        Drawable avatarDrawable = avatarDrawableFactory.getBorderedRoundedAvatarDrawable(Bitmap.createScaledBitmap(bmp, 128, 128, false));
        avatarView.setImageDrawable(avatarDrawable);

        PrizmCache cache = PrizmCache.getInstance();
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            coverPhotoView.setImageDrawable(getDrawable(R.drawable.cover_photo_holder));
        } else {
            coverPhotoView.setImageDrawable(getResources().getDrawable(R.drawable.cover_photo_holder));
        }
        if (profile.coverPhotoURL != null && !profile.coverPhotoURL.isEmpty()) {
            cache.fetchDrawable(profile.coverPhotoURL, coverPhotoView);
        }
        if (profile.profilePhotoURL != null && !profile.profilePhotoURL.isEmpty()) {
            new LoadImage().execute(profile.profilePhotoURL, "avatar");
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

        if (id == R.id.action_red_theme) {
            PrizmCache.getInstance().objectCache.put("theme", R.style.PrizmRed);
            this.recreate();
        }

        if (id == R.id.action_blue_theme) {
            PrizmCache.getInstance().objectCache.put("theme", R.style.PrizmBlue);
            this.recreate();
        }

        if(id == R.id.action_logout) {
            User.logout(getApplicationContext());
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
}
