package co.higheraltitude.prizm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.joanzapata.android.asyncservice.api.annotation.InjectService;
import com.joanzapata.android.asyncservice.api.annotation.OnMessage;
import com.joanzapata.android.asyncservice.api.internal.AsyncService;

import org.json.JSONObject;

import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.network.HAAPIService;

public class LoginActivity extends AppCompatActivity {
    public final static String EXTRA_PROFILE = "co.higheraltitude.prizm.PROFILE";
    private UserLoginRow emailRow = null;
    private UserLoginRow passwordRow = null;
    @InjectService HAAPIService service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        emailRow = (UserLoginRow)findViewById(R.id.loginpage_email);
        passwordRow = (UserLoginRow)findViewById(R.id.loginpage_password);
        AsyncService.inject(this);
        configureListeners();
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
                service.getLogin(email, password);

                return true;
            }
        });
    }

    @OnMessage public void onLogin(User profile) {
        try {
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra(EXTRA_PROFILE, profile);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
