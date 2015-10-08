package co.higheraltitude.prizm;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.os.Handler;
import android.os.Message;
import android.speech.RecognizerIntent;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserAvatarView;
import co.higheraltitude.prizm.views.UserTagView;

public class DirectSelectorActivity extends AppCompatActivity {
    private PrizmCache cache;
    private SearchView mSearchView;
    private static Boolean needUsers = true;
    private static UserAdapter mUserAdapter;
    private ListView mListView;
    private EditText mEditText;
    private static ProgressBar mProgressBar;

    private int VOICE_RESULT = 8383;

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
        setContentView(R.layout.activity_direct_selector);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackListener());
        ArrayList<User> userArrayList = new ArrayList<>();
        mUserAdapter = new UserAdapter(getApplicationContext(), userArrayList);
        mListView = (ListView)findViewById(R.id.user_list);
        mListView.setAdapter(mUserAdapter);
        mListView.setOnItemClickListener(new ClickListener());
        mEditText = (EditText)findViewById(R.id.search_users);
        mEditText.addTextChangedListener(new UserInputWatcher());
        mEditText.setActivated(false);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setIndeterminate(true);
        loadUsers();


    }

    private static void loadUsers() {
        if (needUsers) {
            String name = null;
            if (mUserAdapter.getCount() > 0) {
                name = mUserAdapter.getUser(mUserAdapter.getCount() - 1).name;
            }
            User.fetchUserContacts(User.getCurrentUser().primaryOrganization, name, new UserHandler());
        }
    }

    private void filter(String query){

    }

    private class BackListener implements AdapterView.OnClickListener {
        @Override
        public void onClick(View view) {
            finish();
        }
    }

    private class ClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?>parent, View view, int position, long l ) {
            UserAvatarView v = (UserAvatarView)view;
            User u = v.getUser();
            Intent intent = new Intent(getApplicationContext(), ReadMessagesActivity.class);
            intent.putExtra(ReadMessagesActivity.EXTRA_IS_DIRECT, true);
            intent.putExtra(ReadMessagesActivity.EXTRA_DIRECT_USER, u);
            intent.putExtra(ReadMessagesActivity.EXTRA_ORGANIZATION, User.getCurrentUser().primaryOrganization);
            startActivityForResult(intent, 88);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_direct_selector, menu);
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

    public void searchClicked(View view) {
        mEditText.requestFocus();
    }

    public void micClicked(View view) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak...");
        startActivityForResult(intent, VOICE_RESULT);
    }

    private static class UserAdapter extends ArrayAdapter<User> {
        private UserNameFilter filter = new UserNameFilter();
        private ArrayList<User> baseList;
        private ArrayList<User> userList;


        public UserAdapter(Context c, List<User> items){
            super(c, 0, items);
            baseList = new ArrayList<>();
            baseList.addAll(items);
            userList = new ArrayList<>();
            userList.addAll(items);
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

        public void addUsers(List<User> users){
            addAll(users);
            baseList.addAll(users);
        }

        public User getUser(int position) {
            return getItem(position);
        }

        @Override
        public Filter getFilter() {
            return filter;
        }

        private class UserNameFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                Iterator<User> iterator = baseList.iterator();
                ArrayList<User> filt = new ArrayList<>();
                if (constraint.length() > 0) {
                    while (iterator.hasNext()) {
                        User u = iterator.next();
                        if (u.name.toLowerCase().contains(constraint.toString().toLowerCase())) {
                            filt.add(u);
                        }
                    }
                } else {
                    filt.addAll(baseList);
                }
                results.values = filt;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                userList = (ArrayList<User>)results.values;
                if (userList == null) {
                    userList = new ArrayList<>();
                }
                notifyDataSetChanged();
                clear();
                Iterator<User> iterator = userList.iterator();
                while (iterator.hasNext()) {
                    add(iterator.next());
                }
                notifyDataSetInvalidated();
            }
        }
    }

    private static class UserHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<User> u = (ArrayList<User>)msg.obj;
            if (u.size() > 0) {
                mUserAdapter.addUsers(u);
                mUserAdapter.notifyDataSetChanged();
                loadUsers();
//                loadUsers();
            } else {
                mProgressBar.setIndeterminate(false);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RESULT) {
            ArrayList<String> matches = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            if (matches.size() > 0) {

                mEditText.setText(matches.get(0));
            }
        } else {
            finish();
        }
    }

    private class UserInputWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            mUserAdapter.getFilter().filter(s);
            mUserAdapter.notifyDataSetChanged();
        }
    }
}
