package co.higheraltitude.prizm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by boonej on 10/17/15.
 */
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Runnable mUpdateTimeTask = new Runnable() {
            public void run() {
                // do what you need to do here after the delay
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        };
        Handler mHandler = new Handler();
        mHandler.postDelayed(mUpdateTimeTask, 1500);


    }


}


