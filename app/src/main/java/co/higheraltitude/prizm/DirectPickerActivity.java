package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;


import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserAvatarView;
import co.higheraltitude.prizm.views.UserTagView;

public class DirectPickerActivity extends AppCompatActivity implements UserAvatarView.UserAvatarViewDelegate {
    private PrizmCache cache;
    private String mOrganization;
    private ListView listView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    public static String EXTRA_ORGANIZATION = "co.higheraltitude.prizm.extra_organization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        cache = PrizmCache.getInstance();
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_picker);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackListener());
        Intent intent = getIntent();
        mOrganization = intent.getStringExtra(EXTRA_ORGANIZATION);
        listView = (ListView)findViewById(R.id.group_list);
        listView.setOnItemClickListener(new UserClickListener());

        fetchUsers();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_direct_picker, menu);
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

    private void fetchUsers() {
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);
        userAdapter = new UserAdapter(getApplicationContext(), new ArrayList<User>());
        User.fetchAvailableMessageRecipients(mOrganization, new UserDelegate());
        listView.setAdapter(userAdapter);
    }

    private class UserClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            User user = userAdapter.getItem(position);
            Intent intent = new Intent(getApplicationContext(), ReadMessagesActivity.class);
            intent.putExtra(ReadMessagesActivity.EXTRA_ORGANIZATION, User.getCurrentUser().primaryOrganization);
            intent.putExtra(ReadMessagesActivity.EXTRA_DIRECT_USER, user);
            intent.putExtra(ReadMessagesActivity.EXTRA_IS_DIRECT, true);
            startActivity(intent);
        }
    }

    private class UserDelegate implements PrizmDiskCache.CacheRequestDelegate {

        @Override
        public void cached(String path, Object object) {
            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            process(object);
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.GONE);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                List<User> users = (List<User>) object;
                userAdapter.clear();
                userAdapter.addAll(users);
                userAdapter.notifyDataSetChanged();
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    private class UserAdapter extends ArrayAdapter<User> {
        public UserAdapter(Context c, List<User> items){
            super(c, 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserAvatarView userTagView = (UserAvatarView) convertView;
            if (userTagView == null) {
                userTagView = UserAvatarView.inflate(parent);
                userTagView.setDelegate(DirectPickerActivity.this);
            }
            userTagView.setUser(getItem(position));
            return userTagView;
        }
    }

    private class BackListener implements AdapterView.OnClickListener {
        @Override
        public void onClick(View view) {
            finish();
        }
    }

    public void avatarViewClicked(UserAvatarView view) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, view.getUser());
        startActivity(intent);
    }
}
