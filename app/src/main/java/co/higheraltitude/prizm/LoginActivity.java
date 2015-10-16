package co.higheraltitude.prizm;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.os.Handler;

import com.crashlytics.android.Crashlytics;

import co.higheraltitude.prizm.views.UserLoginRow;
import io.fabric.sdk.android.Fabric;

import co.higheraltitude.prizm.models.User;

public class LoginActivity extends Activity {
    public final static String EXTRA_PROFILE = "co.higheraltitude.prizm.PROFILE";
    public final static String EXTRA_PROFILE_BASE = "co.higheraltitude.prizm.profile_base";
    private UserLoginRow emailRow = null;
    private UserLoginRow passwordRow = null;
    private View backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);
        emailRow = (UserLoginRow)findViewById(R.id.loginpage_email);
        passwordRow = (UserLoginRow)findViewById(R.id.loginpage_password);

        configureListeners();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (emailRow.editText.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    private void configureListeners() {
        passwordRow.editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                String email = emailRow.getText();
                String password = passwordRow.getText();
                User.login(email, password, new Handler() {
                    @Override
                    public void handleMessage(Message message) {
                        if (message.obj != null) {
                            User user = (User) message.obj;
                            Intent intent = new Intent(getApplicationContext(), Registration.class);
                            intent.putExtra(EXTRA_PROFILE, user);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });

                return true;
            }
        });
    }

    public void back(View view){
        finish();
    }


}
