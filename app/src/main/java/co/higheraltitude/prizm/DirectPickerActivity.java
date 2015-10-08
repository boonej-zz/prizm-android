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

import java.util.List;


import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserAvatarView;
import co.higheraltitude.prizm.views.UserTagView;

public class DirectPickerActivity extends AppCompatActivity {
    private PrizmCache cache;
    private String mOrganization;
    private ListView listView;
    private UserAdapter userAdapter;
    private ProgressBar progressBar;
    public static String EXTRA_ORGANIZATION = "co.higheraltitude.prizm.extra_organization";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        cache = PrizmCache.getInstance();
        Object theme = PrizmCache.objectCache.get("theme");

        if (theme != null ) {
            setTheme((int)theme);
        } else {
            setTheme(R.style.PrizmBlue);
        }
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
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);
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
        userAdapter = new UserAdapter(getApplicationContext(),
                User.fetchAvailableMessageRecipients(mOrganization, new UserHandler(this)));
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

    private static class UserHandler extends Handler {

        private DirectPickerActivity mActivity;

        public UserHandler(DirectPickerActivity activity){
            mActivity = activity;
        }

        public void handleMessage(Message msg) {
            List<User> users = (List<User>)msg.obj;
            mActivity.userAdapter = new UserAdapter(mActivity.getApplicationContext(), users);
            mActivity.listView.setAdapter(mActivity.userAdapter);
            mActivity.userAdapter.notifyDataSetChanged();
            mActivity.progressBar.setIndeterminate(false);
            mActivity.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private static class UserAdapter extends ArrayAdapter<User> {
        public UserAdapter(Context c, List<User> items){
            super(c, 0, items);
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserAvatarView userTagView = (UserAvatarView) convertView;
            if (userTagView == null) {
                userTagView = UserAvatarView.inflate(parent);
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
}
