package co.higheraltitude.prizm;

import android.animation.AnimatorInflater;
import android.animation.StateListAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.network.PrizmAPIService;

@TargetApi(21)
public class Registration extends AppCompatActivity {

    private final Activity activity = this;
    private TwitterLoginButton twitterButton;
    private ImageButton facebookButton;
    private CallbackManager callbackManager;
    private ImageButton dummyTwitterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.PrizmRegistrationTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        configureFacebookButton();
        configureTwitterButton();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_registration, menu);
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

    public void onLoginClick(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(intent, MainActivity.DO_LOGIN);
    }

    public void onSignupClick(View view) {
        Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
        startActivityForResult(intent, MainActivity.DO_LOGIN);
    }

    private void configureFacebookButton() {
        callbackManager = CallbackManager.Factory.create();
        facebookButton = (ImageButton)findViewById(R.id.facebook_login_button);
        if (Build.VERSION.SDK_INT > 20) {
            int[] attrs = new int[] {android.R.attr.selectableItemBackgroundBorderless};
            TypedArray ta = obtainStyledAttributes(attrs);
            Drawable itemBg = ta.getDrawable(0);
            ta.recycle();
            facebookButton.setBackground(itemBg);
         }

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        final AccessToken accessToken = loginResult.getAccessToken();
                        User.login(loginResult.getAccessToken(), new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg != null && msg.obj != null) {
                                    User user = (User) msg.obj;
                                    Intent intent = new Intent(getApplicationContext(), Registration.class);
                                    intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else {
//                                    GraphRequest request = GraphRequest.newMeRequest(
//                                            accessToken,
//                                            new GraphRequest.GraphJSONObjectCallback() {
//                                                @Override
//                                                public void onCompleted(
//                                                        JSONObject object,
//                                                        GraphResponse response) {
//                                                    // Application code
//                                                    Log.d("DEBUG", object.toString());
//                                                }
//                                            });
//                                    Bundle parameters = new Bundle();
//                                    parameters.putString("fields", "id,first_name,last_name,email,link");
//                                    request.setParameters(parameters);
//                                    request.executeAsync();
                                    Profile profile = Profile.getCurrentProfile();
                                    Log.d("DEBUG", profile.getFirstName());
                                    Bundle info = new Bundle();
                                    info.putString("first_name", profile.getFirstName());
                                    info.putString("last_name", profile.getLastName());
                                    info.putString("provider", "facebook");
                                    info.putString("provider_token", accessToken.getToken());
                                    String pattern = "https://graph.facebook.com/%s/picture?type=large";
                                    String path = String.format(pattern, profile.getId());
                                    info.putString("profile_photo_url", path);
                                    Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                                    intent.putExtra(LoginActivity.EXTRA_PROFILE_BASE, info);
                                    startActivity(intent);
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancel() {
                        Toast toast = Toast.makeText(getApplicationContext(), "Login cancelled", Toast.LENGTH_LONG);
                        toast.show();
                    }

                    @Override
                    public void onError(FacebookException e) {
                        e.printStackTrace();
                    }
                });
        facebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Collection<String> permissions = Arrays.asList("public_profile", "user_friends");
                LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
            }
        });
    }

    private void configureTwitterButton() {
        twitterButton = (TwitterLoginButton)findViewById(R.id.twitter_login_button);
        dummyTwitterButton = (ImageButton)findViewById(R.id.dummy_twitter_login_button);
        dummyTwitterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                twitterButton.callOnClick();
            }
        });
        if (Build.VERSION.SDK_INT > 20) {
            int[] attrs = new int[] {android.R.attr.selectableItemBackgroundBorderless};
            TypedArray ta = obtainStyledAttributes(attrs);
            Drawable itemBg = ta.getDrawable(0);
            ta.recycle();
            dummyTwitterButton.setBackground(itemBg);
        }
        twitterButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {

                final TwitterSession session = Twitter.getSessionManager().getActiveSession();
                final TwitterAuthToken token = session.getAuthToken();
                final String userName = result.data.getUserName();
                PrizmCache cache = PrizmCache.getInstance();

                cache.objectCache.put("twitter_token", token.token);
                cache.objectCache.put("twitter_secret", token.secret);
                cache.objectCache.put("twitter_name", userName);

                User.login(token, new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        if (msg != null && msg.obj != null) {
                            User user = (User) msg.obj;
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
                            setResult(RESULT_OK, intent);
                            finish();
                        } else {
                            TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                            twitterApiClient.getAccountService().verifyCredentials(false, false, new com.twitter.sdk.android.core.Callback<com.twitter.sdk.android.core.models.User>() {
                                @Override
                                public void success(Result<com.twitter.sdk.android.core.models.User> result) {
                                    Log.d("DEBUG", result.toString());
                                    Bundle info = new Bundle();
                                    info.putString("provider", "twitter");
                                    info.putString("provider_token", token.token);
                                    info.putString("provider_secret", token.secret);
                                    info.putString("twitter_name", userName);
                                    info.putString("profile_photo_url", result.data.profileImageUrlHttps);
                                    String name = result.data.name;
                                    if (name != null && !name.isEmpty()) {
                                        String [] nameArray = name.split(" ");
                                        if (nameArray.length > 0) {
                                            info.putString("first_name", nameArray[0]);
                                            info.putString("last_name", nameArray[nameArray.length - 1]);
                                        }
                                    }
                                    Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                                    intent.putExtra(LoginActivity.EXTRA_PROFILE_BASE, info);
                                    startActivity(intent);
                                }

                                @Override
                                public void failure(TwitterException e) {
                                    Bundle info = new Bundle();
                                    info.putString("provider", "twitter");
                                    info.putString("provider_token", token.token);
                                    info.putString("provider_secret", token.secret);
                                    info.putString("twitter_name", userName);
                                    Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                                    intent.putExtra(LoginActivity.EXTRA_PROFILE_BASE, info);
                                    startActivity(intent);
                                }
                            });

                        }
                    }
                });

                Log.d("DEBUG", "Things are happening now.");
            }

            @Override
            public void failure(TwitterException e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MainActivity.DO_LOGIN) {
            if (resultCode == RESULT_OK) {
                User profile = data.getParcelableExtra(LoginActivity.EXTRA_PROFILE);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(LoginActivity.EXTRA_PROFILE, profile);
                setResult(RESULT_OK, intent);
                finish();
            }
        } else {
            if (requestCode == 140) {
                super.onActivityResult(requestCode, resultCode, data);
                twitterButton.onActivityResult(requestCode, resultCode, data);
            } else {
                callbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }




}
