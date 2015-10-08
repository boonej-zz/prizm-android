package co.higheraltitude.prizm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.vincentbrison.openlibraries.android.dualcache.lib.DualCacheContextUtils;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.network.PrizmAPIService;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "MzIoqUFCk7BYUNpCNxtGuhuLu";
    private static final String TWITTER_SECRET = "yGhuwPvSljoVJoD4il2qtHZG0q4hWlXC87Mcdly0pxaFrMHEaf";
    private static boolean didStart;
    public static boolean messagesStarted;
    private static GoogleApiClient mGoogleApiClient;
    public static Location lastLocation;


    static final int DO_LOGIN = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        didStart = false;
        buildGoogleApiClient();
        mGoogleApiClient.connect();
        ImageHelper.registerContext(getApplicationContext());
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Fabric.with(this, new Twitter(authConfig), new Crashlytics());
        Resources.Theme current = this.getTheme();



        setContentView(R.layout.activity_main);
        DualCacheContextUtils.setContext(getApplicationContext());
        PrizmAPIService.registerContext(getApplicationContext());
        User user = User.getCurrentUser();
        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), MessageGroupsActivity.class);
            MainActivity.messagesStarted = true;
            intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
            startActivity(intent);
        } else {
            Intent intent = new Intent(getApplicationContext(), Registration.class);
            startActivityForResult(intent, DO_LOGIN);
        }

//        startActivity(intent);
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
                Intent intent = new Intent(mContext, MessageGroupsActivity.class);

                intent.putExtra(LoginActivity.EXTRA_PROFILE, (User)obj);
                mActivity.startActivity(intent);
                didStart = true;
                messagesStarted = true;
            }
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        if (messagesStarted || didStart ) {
            this.finish();
        }

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
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(
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

}
