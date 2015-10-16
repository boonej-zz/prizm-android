package co.higheraltitude.prizm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCacheContextUtils;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import co.higheraltitude.prizm.adapters.MenuItemAdapter;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.fragments.ExploreFragment;
import co.higheraltitude.prizm.fragments.GraphFragment;
import co.higheraltitude.prizm.fragments.HomeFeedFragment;
import co.higheraltitude.prizm.fragments.InsightFragment;
import co.higheraltitude.prizm.fragments.MessageGroupFragment;
import co.higheraltitude.prizm.fragments.SurveyFragment;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.network.PrizmAPIService;
import co.higheraltitude.prizm.views.MenuItemView;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, AdapterView.OnItemClickListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "MzIoqUFCk7BYUNpCNxtGuhuLu";
    private static final String TWITTER_SECRET = "yGhuwPvSljoVJoD4il2qtHZG0q4hWlXC87Mcdly0pxaFrMHEaf";
    public static boolean DID_START;
    public static boolean MESSAGES_STARTED
            ;

    private static GoogleApiClient mGoogleApiClient;
    private static Location mLastLocation;

    private PrizmCache mCache;

    // Fragments
    private MessageGroupFragment mMessageFragment;
    private HomeFeedFragment mHomeFragment;


    static final int DO_LOGIN = 1;

    // LAYOUT
    private DrawerLayout mDrawerLayout;
    private ListView mNavigationList;
    private ViewPager mViewPager;
    private ImageView mAvatarView;
    private Toolbar mToolbar;

    public static Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DualCacheContextUtils.setContext(getApplicationContext());
        PrizmAPIService.registerContext(getApplicationContext());
        mCache = PrizmCache.getInstance();
        context = getApplicationContext();
        Object theme = PrizmCache.objectCache.get("theme");
        if (theme != null ) {
            setTheme((int)theme);
        } else {
            setTheme(R.style.PrizmBlue);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DID_START = false;
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        ImageHelper.registerContext(getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        configureDrawer();
        configurePages();
        configureActionBar();
        if (User.getCurrentUser() != null) {
            finalizeConfiguration();
        }




//        setContentView(R.layout.activity_main);
//
//        PrizmAPIService.registerContext(getApplicationContext());
//        User user = User.getCurrentUser();
//        if (user != null) {
//            if (user.primaryOrganization != null && !user.primaryOrganization.isEmpty()) {
//                Intent intent = new Intent(getApplicationContext(), MessageGroupsActivity.class);
//                MainActivity.messagesStarted = true;
//                intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
//                startActivity(intent);
//            } else {
//                Toast.makeText(getApplicationContext(), getString(R.string.error_no_membership), Toast.LENGTH_LONG).show();
//            }
//        } else {
//            Intent intent = new Intent(getApplicationContext(), Registration.class);
//            startActivityForResult(intent, DO_LOGIN);
//        }

//        startActivity(intent);
    }



    private void configureDrawer() {
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        ImageView coverImage = (ImageView)findViewById(R.id.menu_cover_view);
        mAvatarView = (ImageView)findViewById(R.id.menu_avatar_view);
        TextView nameView = (TextView)findViewById(R.id.menu_name);
        User u = User.getCurrentUser();
        nameView.setText(u.name);
        mCache.fetchDrawable(u.profilePhotoURL, coverImage);
        LoadImage li = new LoadImage();
        li.execute(u.profilePhotoURL);
        String [] menuItems = getResources().getStringArray(R.array.menu_items);
        ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menuItems));
        MenuItemAdapter menuItemAdapter = new MenuItemAdapter(getApplicationContext(), menuList);
        mNavigationList  = (ListView)findViewById(R.id.menu_list);
        mNavigationList.setOnItemClickListener(this);
        mNavigationList.setAdapter(menuItemAdapter);


    }

    private void configurePages() {
        if (mViewPager == null) {
            mViewPager = (ViewPager) findViewById(R.id.main_pager);
        }
        mViewPager.setAdapter(new NavigationPager(getSupportFragmentManager(), MainActivity.this));
    }

    private void configureActionBar() {
        mToolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.menu);
        mToolbar.setNavigationOnClickListener(new MenuClickListener(mDrawerLayout));
    }

    private void finalizeConfiguration() {
//        User u = User.getCurrentUser();
//        if (mMessageFragment == null) {
//            mMessageFragment = new MessageGroupFragment();
//        }
//        if (u != null && u.primaryOrganization != null) {
//            mMessageFragment.setUser(u);
//        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position < 7) {
            ((MenuItemAdapter)parent.getAdapter()).setSelectedItem(position);
            mViewPager.setCurrentItem(position, false);
            String text = (String)parent.getAdapter().getItem(position);
            mToolbar.setTitle(text);
            mDrawerLayout.closeDrawer(Gravity.LEFT);

        }
    }


    public static Location lastLocation() {
        return mLastLocation;
    }

    private static class RefreshProfileHandler extends Handler {
        private Context mContext;
        private Activity mActivity;
        public RefreshProfileHandler(Context context, Activity activity){
            mContext = context;
            mActivity = activity;
        }
        @Override
        public void handleMessage(Message msg) {
            Object obj = msg.obj;
            if (obj != null) {
                User.setCurrentUser((User)obj);
                if (User.getCurrentUser().primaryOrganization != null && ! User.getCurrentUser().primaryOrganization.isEmpty()) {
                    Intent intent = new Intent(mContext, MessageGroupsActivity.class);

                    intent.putExtra(LoginActivity.EXTRA_PROFILE, (User) obj);
                    mActivity.startActivity(intent);
                    DID_START = true;
                    MESSAGES_STARTED = true;
                } else {
                    Toast.makeText(mContext, mContext.getString(R.string.error_no_membership), Toast.LENGTH_LONG).show();
                    User.setCurrentUser(null);
                }
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();


//        if (MESSAGES_STARTED || DID_START ) {
//            this.finish();
//        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DO_LOGIN) {
            if (resultCode == RESULT_OK) {
                User profile = data.getParcelableExtra(LoginActivity.EXTRA_PROFILE);
                User.fetchUserCore(profile, new RefreshProfileHandler(getApplicationContext(), this));

            }

        }
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d("DEBUG", "Connection suspended: " + cause);
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.d("DEBUG", "Connection failed: " + result.toString());
    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {

        private Bitmap coverImage;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            try {
                coverImage = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return coverImage;
        }

        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(Bitmap.createScaledBitmap(image, 128, 128, false));
                mAvatarView.setImageDrawable(avatarDrawable);
            }
        }
    }

    public class NavigationPager extends FragmentPagerAdapter {
        final int PAGE_COUNT = 6;

        private Context context;

        public NavigationPager(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (position == 0) {
                fragment = new HomeFeedFragment();
            } else if (position == 1) {
                fragment = new ExploreFragment();
            } else if (position == 2) {
                fragment = new InsightFragment();
            } else if (position == 3) {
                fragment = new GraphFragment();
            } else if (position == 4) {
                if (mMessageFragment == null) {
                    fragment = new MessageGroupFragment();
                    Bundle args = new Bundle();
                    User u = User.getCurrentUser();
                    args.putParcelable("user", u);
                    fragment.setArguments(args);
                }
            } else if (position == 5) {
                fragment = new SurveyFragment();
            }

            return fragment;
        }



//        @Override
//        public CharSequence getPageTitle(int position) {
//            return titles[position];
//        }

    }

}
