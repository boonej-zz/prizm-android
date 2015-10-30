package co.higheraltitude.prizm;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserAvatarView;
import co.higheraltitude.prizm.views.UserSearchBox;

public class EditGroupActivity extends AppCompatActivity implements UserSearchBox.UserSearchListener,
        AdapterView.OnItemClickListener {

    public static String EXTRA_GROUP = "co.higheraltitude.prizm.edit_group";
    public static int REQUEST_EDIT_GROUP = 9820;

    private static Group mGroup;
    private static String mLeader;
    private static ArrayList<User> mSelectedUsers;
    private ArrayList<User> mUsers;

    private EditText mGroupNameField;
    private EditText mGroupDescriptionField;
    private Button mGroupLeaderButton;
    private ListView mListView;
    private UserSearchBox mSearchBox;

    private UserAdapter mUserAdapter;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Object theme = PrizmCache.getInstance().objectCache.get("theme");
        setTheme(User.getTheme());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_group);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        actionBar.hideOverflowMenu();
        ImageButton doneButton = (ImageButton)findViewById(R.id.action_done_button);

        Intent intent = getIntent();
        mGroup = intent.getParcelableExtra(EXTRA_GROUP);

        mSelectedUsers = new ArrayList<>();
        mGroupNameField = (EditText)findViewById(R.id.group_name);
        mGroupDescriptionField = (EditText)findViewById(R.id.group_description);
        mGroupLeaderButton = (Button)findViewById(R.id.leader_dropdown);
        mGroupNameField.setText(mGroup.name);
        mGroupDescriptionField.setText(mGroup.description);
        mGroupLeaderButton.setText(mGroup.leaderName);
        mListView = (ListView)findViewById(R.id.user_list);
        mListView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
        mListView.setOnItemClickListener(this);
        mLeader = mGroup.leader;
        mSearchBox = (UserSearchBox)findViewById(R.id.search_members_box);
        mSearchBox.setSearchListener(this);

        mGroupNameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String result = s.toString().replaceAll(" ", "").toLowerCase();
                if (!s.toString().equals(result)) {
                    mGroupNameField.setText(result);
                    mGroupNameField.setSelection(result.length());
                    // alert the user
                }
            }
        });

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDoneClicked(v);
            }
        });

        configureList();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_group, menu);
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User u = mUserAdapter.getItem(position);
        if (!mSelectedUsers.contains(u)) {
            mListView.setItemChecked(position, true);
            view.setSelected(true);
            mSelectedUsers.add((u));
        } else {
            mListView.setItemChecked(position, false);
            view.setSelected(false);
            mSelectedUsers.remove((u));
        }
    }

    private void configureList() {
        mUserAdapter = new UserAdapter(getApplicationContext(), new ArrayList<User>());
        mListView.setAdapter(mUserAdapter);
        loadMembers();
    }

    public void loadMembers() {
        String name = null;
        if (mUserAdapter.getCount() > 0) {
            name = mUserAdapter.getUser(mUserAdapter.getCount() - 1).name;
        }
        User.fetchGroupMembers(mGroup.uniqueID, name, true, new UserDelegate());
    }

    public void voiceButtonTapped() {

    }

    public void searchTextChanged(String s) {
        mUserAdapter.getFilter().filter(s);
    }

    public void onGroupLeaderClicked(View view) {
        ArrayList<User> leaders = new ArrayList<>();
        for (int i = 0; i != mUserAdapter.getCount(); ++i) {
            User u = mUserAdapter.getUser(i);
            if (u.role != null && u.role.equals("leader")) {
                leaders.add(u);
            }

        }
        final LeaderAdapter adapter = new LeaderAdapter(getApplicationContext(), leaders);
        final Dialog dialog = new Dialog(EditGroupActivity.this);
        dialog.setContentView(R.layout.selector_dialog);
        ListView lv = (ListView)dialog.findViewById(R.id.listview_dialog);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User u = adapter.getItem(position);
                mGroupLeaderButton.setText(u.name);
                mLeader = u.uniqueID;
                int pos = mUserAdapter.getPosition(u);

                mListView.performItemClick(mListView.getAdapter().getView(pos, null, mListView), pos, mListView.getAdapter().getItemId(pos));
                dialog.dismiss();
            }
        });
        lv.setAdapter(adapter);
//        Button cancelButton = (Button)dialog.findViewById(R.id.listview_cancel);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        dialog.show();
    }

    public void onDoneClicked(View view) {
        String groupName = mGroupNameField.getText().toString();
        String groupDescription = mGroupDescriptionField.getText().toString();
        if ((groupName != null && groupName.length() > 0) &&
                (groupDescription != null && groupDescription.length() > 0)) {
            HashMap<String, Object> post = new HashMap<>();
            post.put("name", groupName);
            post.put("description", groupDescription);
            if (mLeader != null) {
                post.put("leader", mLeader);
            } else {
                post.put("leader", null);
            }
            post.put("members", mSelectedUsers);
            Group.editGroup(post, mGroup.uniqueID, new DoneHandler(this));
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_group), Toast.LENGTH_SHORT).show();
        }
    }

    private static class LeaderAdapter extends ArrayAdapter<User> {

        public LeaderAdapter(Context c, List<User> items){
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserAvatarView userTagView = (UserAvatarView) convertView;
            if (userTagView == null) {
                userTagView = UserAvatarView.inflate(parent);
            }
            if (!userTagView.isSelectable()) {
                userTagView.setSelectable(true);
            }
            User user = getItem(position);

            userTagView.setUser(user);
            userTagView.setSelected(mLeader.equals(user.uniqueID));
            return userTagView;
        }
    }

    private static class UserAdapter extends ArrayAdapter<User> {
        private UserNameFilter filter = new UserNameFilter();
        public ArrayList<User> baseList;
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
            if (!userTagView.isSelectable()) {
                userTagView.setSelectable(true);
            }
            User user = getItem(position);

            userTagView.setUser(user);
            userTagView.setSelected(mSelectedUsers.contains(user));
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


    private static class DoneHandler extends Handler {

        private Activity mActivity;

        public DoneHandler(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                mGroup.name = ((EditText)mActivity.findViewById(R.id.group_name)).getText().toString();
                mGroup.description = ((EditText)mActivity.findViewById(R.id.group_description)).getText().toString();
                mGroup.leader = mLeader;
                mActivity.finish();
            } else {
                Toast.makeText(mActivity.getApplicationContext(),
                        "Uh oh. There was a problem editing your group.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private class UserDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private boolean emptySet = true;
        @Override
        public void cached(String path, Object object) {
            processUsers(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            processUsers(object);

            emptySet = false;
        }

        private void processUsers(Object object) {
            if (object instanceof  ArrayList) {
                ArrayList<User> users = (ArrayList<User>) object;
                if (users.size() > 0) {
                    if (emptySet) {
                        mUserAdapter.clear();
                    }
                    mUserAdapter.addAll(users);
                    if (mSelectedUsers == null) {
                        mSelectedUsers = new ArrayList<>();
                    }
                    for (User u : users) {
                        if (u.isMember) {
                            mSelectedUsers.add(u);
                        }
                    }
                    mUserAdapter.notifyDataSetChanged();
                    loadMembers();
                }
            }
        }
    }
}
