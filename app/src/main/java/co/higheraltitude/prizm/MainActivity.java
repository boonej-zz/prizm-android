package co.higheraltitude.prizm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
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
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.ExploreFragment;
import co.higheraltitude.prizm.fragments.GraphFragment;
import co.higheraltitude.prizm.fragments.HomeFeedFragment;
import co.higheraltitude.prizm.fragments.InsightFragment;
import co.higheraltitude.prizm.fragments.MessageGroupFragment;
import co.higheraltitude.prizm.fragments.SurveyFragment;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.messaging.RegistrationIntentService;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.network.PrizmAPIService;
import co.higheraltitude.prizm.views.MenuItemView;
import co.higheraltitude.prizm.views.PrizmViewPager;
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
    static final int DO_SETTINGS = 9328;

    // LAYOUT
    private DrawerLayout mDrawerLayout;
    private ListView mNavigationList;
    private PrizmViewPager mViewPager;
    private ImageView mAvatarView;
    private ImageView mCoverView;
    private Toolbar mToolbar;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private ActionBarDrawerToggle mDrawerToggle;

    private Boolean isPremium;

    public static Context context;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DualCacheContextUtils.setContext(getApplicationContext());
        PrizmAPIService.registerContext(getApplicationContext());
        mCache = PrizmCache.getInstance();
        PrizmAPIService.getInstance();
        context = getApplicationContext();
        if (User.getCurrentUser() != null) {
            setTheme(User.getTheme());
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
//        Fabric.with(this, new Twitter(authConfig));
        configureReceiver();

        isPremium = false;
        if (User.getCurrentUser() != null) {
//            if (User.getCurrentUser().primaryOrganization != null &&
//                    !User.getCurrentUser().primaryOrganization.isEmpty()) {
            if (User.getCurrentUser().primaryOrganization != null) isPremium = true;
                configureDrawer();
                configureActionBar();
                configurePages();

                if (User.getCurrentUser() != null) {
                    finalizeConfiguration();
                }
                if (User.getCurrentUser().interestCount < 3) {
                    Intent intent = new Intent(getApplicationContext(), InterestsActivity.class);
                    intent.putExtra(InterestsActivity.EXTRA_FORCED, true);
                    startActivity(intent);
                }
//            } else {
//                Toast.makeText(getApplicationContext(), getString(R.string.error_no_membership),
//                        Toast.LENGTH_LONG).show();
//            }
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

        mCoverView = (ImageView)findViewById(R.id.menu_cover_view);
        mAvatarView = (ImageView)findViewById(R.id.menu_avatar_view);
        TextView nameView = (TextView)findViewById(R.id.menu_name);
        User u = User.getCurrentUser();
        nameView.setText(u.name);
        LoadImage li = new LoadImage();
        li.execute(u.profilePhotoURL);
        int rid;
        if (isPremium) {
            rid = R.array.menu_items_premium;
        } else {
            rid = R.array.menu_items;
        }
        String [] menuItems = getResources().getStringArray(rid);
        ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menuItems));
        MenuItemAdapter menuItemAdapter = new MenuItemAdapter(getApplicationContext(), menuList);
        mNavigationList  = (ListView)findViewById(R.id.menu_list);
        mNavigationList.setOnItemClickListener(this);
        mNavigationList.setAdapter(menuItemAdapter);



    }

    private void configurePages() {
        if (mViewPager == null) {
            mViewPager = (PrizmViewPager) findViewById(R.id.main_pager);
        }
        mViewPager.setAdapter(new NavigationPager(getSupportFragmentManager(), MainActivity.this));
        if (isPremium) {
            mViewPager.setCurrentItem(4);
            ((MenuItemAdapter) mNavigationList.getAdapter()).setSelectedItem(4);
        }
    }

    private void configureActionBar() {
        mToolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationIcon(R.drawable.menu);
//        mToolbar.setNavigationOnClickListener(new MenuClickListener(mDrawerLayout));
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,
                R.string.drawer_close){
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        mToolbar.hideOverflowMenu();
    }

    private void configureReceiver(){
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SharedPreferences sharedPreferences =
                        PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences
                        .getBoolean(RegistrationIntentService.SENT_TOKEN_TO_SERVER, false);

            }
        };
    }

    private void finalizeConfiguration() {
        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }
//        User u = User.getCurrentUser();
//        if (mMessageFragment == null) {
//            mMessageFragment = new MessageGroupFragment();
//        }
//        if (u != null && u.primaryOrganization != null) {
//            mMessageFragment.setUser(u);
//        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isPremium && position < 7) {
            ((MenuItemAdapter)parent.getAdapter()).setSelectedItem(position);
            mViewPager.setCurrentItem(position, false);
            String text = (String)parent.getAdapter().getItem(position);
            if (position == 0) {
                text = "Prizm";
            }
            mToolbar.setTitle(text);
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else if (isPremium && position == 7) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, DO_SETTINGS);
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else if (position < 4) {
            ((MenuItemAdapter)parent.getAdapter()).setSelectedItem(position);
            mViewPager.setCurrentItem(position, false);
            String text = (String)parent.getAdapter().getItem(position);
            if (position == 0) {
                text = "Prizm";
            }
            mToolbar.setTitle(text);
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivityForResult(intent, DO_SETTINGS);
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        }
    }

    public void profileClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, User.getCurrentUser());
        startActivity(intent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
//        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerLayout);
        return super.onPrepareOptionsMenu(menu);
    }


    public static Location lastLocation() {
        return mLastLocation;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i("DEBUG", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private static class RefreshProfileDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private Context mContext;
        private Activity mActivity;

        public RefreshProfileDelegate(Context context, Activity activity){
            mContext = context;
            mActivity = activity;
        }

        @Override
        public void cached(String path, Object user) {
            Log.d("MESSAGE", "Ignoring cached data, forcing refresh.");
        }

        @Override
        public void cacheUpdated(String path, Object user) {
            DID_START = true;
            MESSAGES_STARTED = true;
            if (user instanceof User) User.setCurrentUser((User)user);
            Intent intent = mActivity.getIntent();
            mActivity.finish();
            mActivity.startActivity(intent);
        }
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

                DID_START = true;
                MESSAGES_STARTED = true;
                Intent intent = mActivity.getIntent();
                mActivity.finish();
                mActivity.startActivity(intent);

            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(RegistrationIntentService.REGISTRATION_COMPLETE));
        if (User.getCurrentUser() == null) {
            Intent intent = new Intent(getApplicationContext(), Registration.class);
            startActivityForResult(intent, DO_LOGIN);
        }
//        if (MESSAGES_STARTED || DID_START ) {
//            this.finish();
//        }

    }

    @Override
    protected void onPause(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
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
                User.fetchUserCore(profile, new RefreshProfileDelegate(getApplicationContext(), this));
            }

        } else if (requestCode == DO_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Boolean logout = data.getBooleanExtra("logout", false);
                if (logout) {
                    PrizmDiskCache cache = PrizmDiskCache.getInstance(getApplicationContext());
                    cache.clearValue(User.PrizmCurrentUserCacheKey);
                    Intent intent = this.getIntent();
                    finish();
                    startActivity(intent);
                }
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
                mCoverView.setImageBitmap(coverImage);

            }
        }
    }

    public class NavigationPager extends FragmentPagerAdapter {

        final int PAGE_COUNT = isPremium?6:4;

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
            mNavigationList.setOnScrollListener(null);
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
