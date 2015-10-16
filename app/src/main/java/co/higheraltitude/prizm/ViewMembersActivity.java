package co.higheraltitude.prizm;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;

import co.higheraltitude.prizm.adapters.UserAdapter;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.handlers.UserHandler;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.User;



public class ViewMembersActivity extends AppCompatActivity implements
        UserHandler.UserHandlerDelegate {

    public static String EXTRA_GROUP = "co_higheraltitude_extra_group";
    public static String EXTRA_ORGANIZATION = "co_higheraltitude_extra_organization";

    private ListView memberList;

    private Group mGroup;
    private String mOrganization;
    private static UserAdapter mUserAdapter;
    private static UserHandler mUserHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Object theme = PrizmCache.objectCache.get("theme");

        if (theme != null ) {
            setTheme((int)theme);
        } else {
            setTheme(R.style.PrizmBlue);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_members);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        actionBar.hideOverflowMenu();

        Intent intent = getIntent();
        mGroup = intent.getParcelableExtra(EXTRA_GROUP);
        mOrganization = intent.getStringExtra(EXTRA_ORGANIZATION);

        memberList = (ListView)findViewById(R.id.member_list);
        mUserAdapter = new UserAdapter(getApplicationContext(), new ArrayList<User>());
        memberList.setAdapter(mUserAdapter);
        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User u = mUserAdapter.getUser(position);
                Intent profileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                profileIntent.putExtra(LoginActivity.EXTRA_PROFILE, u);
                startActivity(profileIntent);
            }
        });
        mUserHandler = new UserHandler(mUserAdapter, this);
        loadMembers();
    }

    public void loadMembers() {
        String name = null;

        if (mUserAdapter.getCount() > 0) {
            name = mUserAdapter.getUser(mUserAdapter.getCount() - 1).name;
        }

        if (mGroup != null) {
            User.fetchGroupMembers(mGroup.uniqueID, name, false, new UserHandler(mUserAdapter, this));
        } else if (mOrganization != null) {
            User.fetchOrganizationMembers(mOrganization, name, new UserHandler(mUserAdapter, this));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_members, menu);
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

}
