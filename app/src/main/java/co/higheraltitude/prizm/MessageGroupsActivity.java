package co.higheraltitude.prizm;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.sectorsieteg.avatars.AvatarDrawableFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import co.higheraltitude.prizm.adapters.MenuItemAdapter;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.listeners.MenuClickListener;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.GroupView;
import co.higheraltitude.prizm.views.MenuItemView;

@TargetApi(21)
public class MessageGroupsActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {


    private User currentUser;
    private PrizmCache cache;
    private static ArrayList<Group> groups;
    private static GroupAdapter groupAdapter;
    private MenuItemAdapter menuItemAdapter;

    private static ListView listView;
    private FloatingActionButton floatingActionButton;
    public static String [] groupNames;
    private ProgressBar progressBar;
    private View actionOverlay;
    private View newGroupView;
    private static int themeLoaded;
    private JSONArray messageCounts;

    public static DrawerLayout mDrawerLayout;

    private ImageView avatarView;
    private static int[] countArray;
    private int orgMemberCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        cache = PrizmCache.getInstance();
        Object theme = PrizmCache.objectCache.get("theme");
        if (theme != null ) {
            setTheme((int) theme);
        } else {
            setTheme(R.style.PrizmBlue);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_groups);

        newGroupView = findViewById(R.id.new_group_button);
        actionOverlay = findViewById(R.id.action_overlay);
        actionOverlay.setAlpha(0);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.menu);
//        actionBar.setNavigationOnClickListener(new MenuClickListener());
        Intent intent = getIntent();
        currentUser = intent.getParcelableExtra(LoginActivity.EXTRA_PROFILE);

        listView = (ListView)findViewById(R.id.group_list);
        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        floatingActionButton = (FloatingActionButton)findViewById(R.id.floating_action_button);


        configureDrawer();
    }

    @Override
    public void onResume(){
        super.onResume();
        loadGroups();
    }

    private void loadGroups() {

        if (currentUser != null && currentUser.primaryOrganization != null) {
            Group.fetchOrganizationMemberCount(currentUser.primaryOrganization, new OrgCountHandler(this));
            if (currentUser.role == null || ( !currentUser.role.equals("owner") && !currentUser.role.equals("leader"))) {
                newGroupView.setVisibility(View.GONE);
            }
            progressBar.setIndeterminate(true);
            groups = Group.fetchGroupsForUser(currentUser, currentUser.primaryOrganization, new FetchGroupsHandler(getApplicationContext(), this));
            ArrayList<String> groupList = new ArrayList<>();
            Iterator i = groups.iterator();
            while (i.hasNext()) {
                Group g = (Group)i.next();
                groupList.add(g.name);
            }
            groupNames = new String[groupList.size()];
            groupNames = groupList.toArray(groupNames);
            Peep.getCounts(new GroupCountsHandler(this));
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleGroupClick(position);
            }
        });


        try {

            groupAdapter = new GroupAdapter(getApplicationContext(), groups);
            listView.setAdapter(groupAdapter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void configureDrawer() {
        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawingCacheEnabled(true);
        ImageView coverImage = (ImageView)findViewById(R.id.menu_cover_view);
        avatarView = (ImageView)findViewById(R.id.menu_avatar_view);
        TextView nameView = (TextView)findViewById(R.id.menu_name);
        User u = User.getCurrentUser();
        nameView.setText(u.name);
        cache.fetchDrawable(u.profilePhotoURL, coverImage);
        LoadImage li = new LoadImage();
        li.execute(u.profilePhotoURL);
        String [] menuItems = getResources().getStringArray(R.array.menu_items);
        ArrayList<String> menuList = new ArrayList<>(Arrays.asList(menuItems));
        menuItemAdapter = new MenuItemAdapter(getApplicationContext(), menuList);
        ListView menuListView  = (ListView)findViewById(R.id.menu_list);
        menuListView.setAdapter(menuItemAdapter);
        menuListView.setOnItemClickListener(this);

    }

    public void onFABClick(View view) {
        if (actionOverlay.getVisibility() == View.INVISIBLE) {
            actionOverlay.setVisibility(View.VISIBLE);
            actionOverlay.setAlpha(0);
            final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();

            final OvershootInterpolator oi = new OvershootInterpolator();
            ViewCompat.animate(floatingActionButton).rotation(135f).withLayer().setDuration(300).setInterpolator(interpolator).start();
            ViewCompat.animate(actionOverlay).alpha(1.f).withLayer().setDuration(300).setInterpolator(oi).start();


        } else {
            actionOverlay.setVisibility(View.INVISIBLE);
            final OvershootInterpolator interpolator = new OvershootInterpolator();
            ViewCompat.animate(floatingActionButton).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
            actionOverlay.startAnimation(fadeOut);
        }
    }

    public void onDirectClick(View view) {
        actionOverlay.setVisibility(View.INVISIBLE);
        final OvershootInterpolator interpolator = new OvershootInterpolator();
        ViewCompat.animate(floatingActionButton).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        actionOverlay.startAnimation(fadeOut);
        Intent intent = new Intent(getApplicationContext(), DirectSelectorActivity.class);
        startActivity(intent);
    }

    public void onNewGroupClick(View view) {
        actionOverlay.setVisibility(View.INVISIBLE);
        final OvershootInterpolator interpolator = new OvershootInterpolator();
        ViewCompat.animate(floatingActionButton).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        actionOverlay.startAnimation(fadeOut);
        Intent intent = new Intent(getApplicationContext(), NewGroupActivity.class);
        startActivity(intent);
    }

    private void handleGroupClick(int position) {
        Intent intent;
        if (position == 0) {
            intent = new Intent(getApplicationContext(), DirectPickerActivity.class);

        } else {
            intent = new Intent(getApplicationContext(), ReadMessagesActivity.class);
            if (position == 1) {
                intent.putExtra(ReadMessagesActivity.EXTRA_IS_ALL, true);
                intent.putExtra(ReadMessagesActivity.EXTRA_ORG_MEMBER_COUNT, orgMemberCount);
            } else {
                Group group = groups.get(position - 2);
                intent.putExtra(ReadMessagesActivity.EXTRA_GROUP, group);
            }
        }
        if (intent != null) {
            intent.putExtra(ReadMessagesActivity.EXTRA_CURRENT_USER, User.getCurrentUser());
            intent.putExtra(ReadMessagesActivity.EXTRA_ORGANIZATION, User.getCurrentUser().primaryOrganization);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_message_groups, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement



        return super.onOptionsItemSelected(item);
    }

    private void processCounts() {
        countArray = new int[groupAdapter.getCount()];
        for (int i = 0; i!= countArray.length; ++i) {
            countArray[i] = 0;
        }
        if (messageCounts != null) {
            for (int i = 0; i != messageCounts.length(); ++i) {
                try {
                    JSONObject object = messageCounts.getJSONObject(i);
                    int total = object.getInt("total");
                    JSONObject j = object.getJSONObject("_id");
                    String org = j.getString("organization");
                    String groupId = j.getString("group");

                    if (!org.equals("null")) {
                        if (!groupId.equals("null")) {
                            int index = groupAdapter.indexOf(groupId);
                            if (index != -1) {
                                countArray[i + 2] = total;
                            }
                        } else {
                            countArray[1] = total;
                        }
                    } else {
                        countArray[0] = total;
                    }
                    groupAdapter.notifyDataSetChanged();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static class GroupCountsHandler extends Handler {
        private MessageGroupsActivity mActivity;

        public GroupCountsHandler(MessageGroupsActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                if (msg.obj instanceof JSONArray) {
                    JSONArray array = (JSONArray)msg.obj;
                    mActivity.messageCounts = array;
                    mActivity.processCounts();
                        Log.d("DEBUG", "Fetched Counts");

                }
            }
        }

    }

    private static class OrgCountHandler extends Handler {
        private MessageGroupsActivity mActivity;

        public OrgCountHandler(MessageGroupsActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                if (msg.obj instanceof JSONObject) {
                    JSONObject object = (JSONObject)msg.obj;
                    try {
                        int orgCount = object.getInt("member_count");
                        mActivity.orgMemberCount = orgCount;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        }
    }

    private static class FetchGroupsHandler extends Handler {

        private Context mContext;
        private MessageGroupsActivity mActivity;

        public FetchGroupsHandler(Context context, MessageGroupsActivity activity) {
            mContext = context;
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.obj != null) {
                    if (msg.obj instanceof ArrayList) {
                        ArrayList<Group> obj = (ArrayList<Group>)msg.obj;

                        int objSize = obj.size();
                        if (groups.size() != obj.size()) {
                            groups = obj;
                            ArrayList<String> groupList = new ArrayList<>();
                            Iterator i = groups.iterator();
                            while (i.hasNext()) {
                                Group g = (Group)i.next();
                                groupList.add(g.name);
                            }
                            groupNames = groupList.toArray(groupNames);
                            int size = groupAdapter.getCount();
                            try {
                                for (int j = 0; j != groups.size(); ++j) {
                                    Group a = groups.get(j);
                                    if (j < size - 2) {
                                        Group b = groupAdapter.getItem(j + 2);
                                        if (!a.uniqueID.equals(b.uniqueID)) {
                                            groupAdapter.remove(b);
                                            groupAdapter.insert(a, j + 2);
                                        }
                                    } else {
                                        groupAdapter.add(a);
                                    }
                                }
                                groupAdapter.notifyDataSetChanged();


                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                Toast.makeText(mContext, "Uh oh! There was a problem loading your groups.", Toast.LENGTH_SHORT).show();
            }
            mActivity.progressBar.setIndeterminate(false);
            mActivity.progressBar.setVisibility(View.GONE);

        }
    }

    public static class GroupAdapter extends ArrayAdapter<Group> {

        public GroupAdapter(Context c, List<Group> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GroupView groupView = (GroupView)convertView;
            if (groupView == null) {
                groupView = GroupView.inflate(parent);
            }
            if (position == 0) {
                groupView.setTitle("@direct");
            } else if (position == 1) {
                groupView.setTitle("#all");
            } else {
                groupView.setGroup(getItem(position - 2));
            }
            if (countArray != null && countArray.length > position) {
                groupView.setCount(countArray[position]);
            } else {
                groupView.setCount(0);
            }
            return groupView;
        }

        public View getView(String groupId) {
            int idx = -1;
            for (int i = 0; i != getCount() - 2; ++i) {
                Group group = getItem(i);
                if (group.uniqueID.equals(groupId)) {
                    idx = i;
                    break;
                }
            }
            View view = null;
            if (idx != -1) {
                view = getView(idx + 2, null, null);
            }
            return view;
        }

        public int indexOf(String groupId) {
            int idx = -1;
            for (int i = 0; i != getCount() - 2; ++i) {
                Group group = getItem(i);
                if (group.uniqueID.equals(groupId)) {
                    idx = i;
                    break;
                }
            }
            return idx;
        }

        @Override
        public int getCount() {
            return super.getCount() + 2;
        }


    }

    private class LoadImage extends AsyncTask<String, String, Bitmap> {

        private Bitmap coverImage;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Bitmap doInBackground(String... args) {
            try {
                coverImage = BitmapFactory.decodeStream((InputStream) new URL(args[0]).getContent());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return coverImage;
        }

        protected void onPostExecute(Bitmap image) {
            if (image != null) {
                AvatarDrawableFactory avatarDrawableFactory = new AvatarDrawableFactory(getResources());
                Drawable avatarDrawable = avatarDrawableFactory.getRoundedAvatarDrawable(Bitmap.createScaledBitmap(image, 128, 128, false));
                avatarView.setImageDrawable(avatarDrawable);
            }
        }
    }

    public void profileClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, User.getCurrentUser());
        startActivity(intent);
    }


    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == menuItemAdapter.getCount() - 1) {
            User.logout(getApplicationContext());
        }
    }
}
