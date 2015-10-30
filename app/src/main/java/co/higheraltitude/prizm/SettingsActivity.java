package co.higheraltitude.prizm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.util.Arrays;

import co.higheraltitude.prizm.adapters.SettingsAdapter;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.FooterButtonView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PrizmCache.getInstance();
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        configureToolbar();
        configureLists();
    }

    private void configureToolbar(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.backarrow_icon);
        toolbar.setNavigationOnClickListener(new BackClickListener(this));
        toolbar.setTitle("Settings");
    }

    private void configureLists() {
        ListView listView = (ListView)findViewById(R.id.settings_list);
        listView.setAdapter(new SettingsAdapter(getApplicationContext(),
                Arrays.asList(getResources().getStringArray(R.array.settings_items))));
        listView.setOnItemClickListener(this);
        FooterButtonView footerButtonView = FooterButtonView.inflate(listView);
        footerButtonView.setOnClickListener(this);
        listView.addFooterView(footerButtonView);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 2:
                startInterestsActivity();
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("logout", true);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void startInterestsActivity() {
        Intent intent = new Intent(getApplicationContext(), InterestsActivity.class);
        startActivity(intent);
    }
}
