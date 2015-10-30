package co.higheraltitude.prizm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import co.higheraltitude.prizm.adapters.UserAdapter;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.delegates.UserDelegate;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;

public class LikesActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    public static final String EXTRA_PEEP = "co_higheraltitude_extra_peep";

    private ListView mListView;
    private UserAdapter mUserAdapter;

    private Peep mPeep;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        actionBar.hideOverflowMenu();

        Intent intent = getIntent();
        mPeep = intent.getParcelableExtra(EXTRA_PEEP);
        mListView = (ListView)findViewById(R.id.member_list);
        mUserAdapter = new UserAdapter(getApplicationContext(), new ArrayList<User>());
        mListView.setAdapter(mUserAdapter);
        mListView.setOnItemClickListener(this);
        mPeep.getLikes(new UserListDelegate());
    }

    private class UserListDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            if (object instanceof ArrayList) {
                mUserAdapter.clear();
                mUserAdapter.addAll((ArrayList<User>) object);
                mUserAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            if (object instanceof ArrayList) {
                mUserAdapter.clear();
                mUserAdapter.addAll((ArrayList<User>)object);
                mUserAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User user = mUserAdapter.getUser(position);
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }


}
