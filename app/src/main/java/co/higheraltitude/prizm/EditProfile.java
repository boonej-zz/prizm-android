package co.higheraltitude.prizm;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.User;

public class EditProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        
    }
}
