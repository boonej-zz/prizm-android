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

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterAuthToken;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.util.Arrays;
import java.util.Collection;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;

@TargetApi(21)
public class Registration extends AppCompatActivity {

    private final Activity activity = this;
    private TwitterLoginButton twitterButton;
    private ImageButton facebookButton;
    private CallbackManager callbackManager;
    private ImageButton dummyTwitterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    public void onSuccess(LoginResult loginResult) {
                        User.login(loginResult.getAccessToken(), new Handler(){
                            @Override
                            public void handleMessage(Message msg) {
                                if (msg != null && msg.obj != null) {
                                    User user = (User) msg.obj;
                                    Intent intent = new Intent(getApplicationContext(), Registration.class);
                                    intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
                                    setResult(RESULT_OK, intent);
                                    finish();
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

                TwitterSession session = Twitter.getSessionManager().getActiveSession();
                TwitterAuthToken token = session.getAuthToken();
                String userName = result.data.getUserName();
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
