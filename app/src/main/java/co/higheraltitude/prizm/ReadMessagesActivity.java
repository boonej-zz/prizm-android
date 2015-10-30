package co.higheraltitude.prizm;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresPermission;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.MessageGroupFragment;
import co.higheraltitude.prizm.helpers.ImageHelper;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.HashTagView;
import co.higheraltitude.prizm.views.PeepView;
import co.higheraltitude.prizm.views.UserTagView;

public class ReadMessagesActivity extends AppCompatActivity implements PeepView.PeepViewListener {

    public static String EXTRA_IS_DIRECT = "co.higheraltitude.prizm.extra_is_direct";
    public static String EXTRA_IS_ALL = "co.higheraltitude.prizm.extra_is_all";
    public static String EXTRA_GROUP = "co.higheraltitude.prizm.extra_group";
    public static String EXTRA_GROUP_NAME = "co.higheraltitude.prizm.extra_group_name";
    public static String EXTRA_DIRECT_USER = "co.higheraltitude.prizm.extra_direct_user";
    public static String EXTRA_CURRENT_USER = "co.higheraltitude.prizm.extra_current_user";
    public static String EXTRA_ORGANIZATION = "co.higheraltitude.prizm.extra_organization";
    public static String EXTRA_ORG_MEMBER_COUNT = "co.higheraltitude.prizm.extra_org_member_count";

    public int UPDATE_TYPE_NEWER = 0;
    public int UPDATE_TYPE_OLDER = 1;
    public static int RESULT_IMAGE_POST = 939;

    private PrizmCache cache;
    private Boolean isAll = false;
    private Boolean isDirect = false;
    public Boolean isUpdating = false;
    public Boolean firstFetchComplete = false;
    public Boolean noOlderMessages = false;
    private Group group;
    private User directUser;
    private String groupName;
    private String groupTag;
    private User currentUser;
    private String organization;
    private Boolean atBottom = true;
    private String role;
    private PeepAdapter peepAdapter;
    private TextWatcher textWatcher;

    // Tagging
    private Boolean typingTag = false;
    private int tagStart = -1;
    private String currentTag = "";
    private int tagType = 0;
    private static int TAG_TYPE_HASH = 0;
    private static int TAG_TYPE_USER = 1;
    private UserTagAdapter userTagAdapter;
    private HashTagAdapter hashTagAdapter;
    private ListView tagList;
    private ProgressBar progressBar;

    private ListView listView;
    private EditText createPeepEditText;
    private ImageButton createPeepActionButton;

    private int lastVisibleItem = 0;
    private boolean scrollingDown;

    private String messageString = "";
    private int lastLength = 0;
    private int orgMemberCount = 0;
    private ArrayList<ImageSpan> deletedSpans = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        cache = PrizmCache.getInstance();
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_messages);

        progressBar = (ProgressBar)findViewById(R.id.progress_bar);
        progressBar.setIndeterminate(true);
        Intent intent = getIntent();
        isAll = intent.getBooleanExtra(EXTRA_IS_ALL, false);
        isDirect = intent.getBooleanExtra(EXTRA_IS_DIRECT, false);
        group = intent.getParcelableExtra(EXTRA_GROUP);
        directUser = intent.getParcelableExtra(EXTRA_DIRECT_USER);
        currentUser = intent.getParcelableExtra(EXTRA_CURRENT_USER);
        organization = intent.getStringExtra(EXTRA_ORGANIZATION);
        groupTag = intent.getStringExtra(EXTRA_GROUP_NAME);
        orgMemberCount = intent.getIntExtra(EXTRA_ORG_MEMBER_COUNT, 0);


        tagList = (ListView)findViewById(R.id.tag_picker_list);

        String title;
        if (isAll) {
            title = "#all";
        } else if (isDirect) {
            title = "@" + directUser.name;
        } else if (groupTag != null) {
            title = "#" + groupTag.substring(1);
        } else {
            title = "#" + group.name.toLowerCase();
        }
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        actionBar.setTitle(title);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        View ib = findViewById(R.id.action_members_button);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ViewMembersActivity.class);
                if (group == null) {
                    i.putExtra(ViewMembersActivity.EXTRA_ORGANIZATION, organization);
                } else {
                    i.putExtra(ViewMembersActivity.EXTRA_GROUP, group);
                }
                startActivity(i);
            }
        });
        if (group == null) {
            if (isDirect) {
                ib.setVisibility(View.GONE);
            } else if (isAll) {
                TextView groupCount = (TextView)findViewById(R.id.badge_count);
                groupCount.setText(String.valueOf(orgMemberCount));
            }
        } else {
            TextView groupCount = (TextView)findViewById(R.id.badge_count);
            groupCount.setText(String.valueOf(group.memberCount));
        }

        role = User.getCurrentUser().role;

        listView = (ListView)findViewById(R.id.message_list);



        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int firstPosition = view.getFirstVisiblePosition();
                if (lastVisibleItem > firstPosition) {
                    scrollingDown = false;
                } else if (lastVisibleItem < firstPosition) {
                    scrollingDown = true;
                } else {
                    int lastPos = view.getLastVisiblePosition();
                    int childPos = view.getCount();
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        fetchNewerPeeps();
                    }
                }
                lastVisibleItem = firstPosition;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (listIsAtTop()) {
                    if (!noOlderMessages) {
                        fetchOlderPeeps();
                    }
                }
            }
        });

        createPeepEditText = (EditText)findViewById(R.id.create_peep_edittext);

        createPeepActionButton = (ImageButton)findViewById(R.id.create_peep_action_button);
        createPeepActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                peepAtachmentClicked();
            }
        });
        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                if (start != tagStart + 1) {
//                    typingTag = false;
//                    tagStart = -1;
//                }
                int length = s.length();
                if (length < lastLength) {
                    Editable message = createPeepEditText.getEditableText();
                    int end = start + count;
                    ImageSpan[] list = message.getSpans(start, end, ImageSpan.class);
                    if (length < lastLength) {
                        for (ImageSpan span : list) {
                            int spanStart = message.getSpanStart(span);
                            int spanEnd = message.getSpanEnd(span);
                            if ((spanStart < end) && (spanEnd >= start))
                                deletedSpans.add(span);
                        }
                    }

                }

                lastLength = length;
                CharSequence cs = "";
                if (s.length() > start) {
                    if (s.charAt(start) == '@') {
                        typingTag = true;
                        tagStart = start;
                        tagType = TAG_TYPE_USER;
                    }
                    if (s.charAt(start) == '#') {
                        typingTag = true;
                        tagStart = start;
                        tagType = TAG_TYPE_HASH;
                    }
                    if (s.charAt(start) == ' ') {
                        typingTag = false;
                        tagStart = -1;
                        currentTag = "";
                    }
                }
                if (typingTag) {
                    if (start < s.length()) {
                        currentTag = currentTag + s.charAt(start);
                    } else {
                        currentTag = currentTag.substring(0, currentTag.length() - 1);
                    }
                    if (currentTag.isEmpty()) {
                        typingTag = false;
                        tagStart = - 1;
                        tagList.setAdapter(null);
                    }
                    if (typingTag) {
                        if (tagType == TAG_TYPE_USER) {
                            getUserTags(currentTag);
                        } else if (tagType == TAG_TYPE_HASH) {
                            getHashTags(currentTag);
                        }
                    }
                } else {
                    if (userTagAdapter != null && userTagAdapter.getCount() > 0) {
                        userTagAdapter.notifyDataSetInvalidated();
                        userTagAdapter.clear();
                    } else if (tagList.getAdapter() != null) {
                        tagList.setAdapter(null);
                    }

                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                boolean hasText = createPeepEditText.getText().length() > 0;
                if (hasText) {
                    createPeepActionButton.setImageDrawable(getResources().getDrawable(R.drawable.send));
                    createPeepActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sendPeepClicked();
                        }
                    });
                } else {
                    createPeepActionButton.setImageDrawable(getResources().getDrawable(R.drawable.attachment));
                    createPeepActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            peepAtachmentClicked();
                        }
                    });
                }
                Editable message = createPeepEditText.getEditableText();
                for (ImageSpan span : deletedSpans) {
                    int start = message.getSpanStart(span);
                    int end = message.getSpanEnd(span);
                    message.removeSpan(span);
                    if (start != end) {
                        message.delete(start, end);
                    }
                }

                deletedSpans.clear();
            }
        };
        createPeepEditText.addTextChangedListener(textWatcher);



            loadMessages();

    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        int resource;
        if (role != null) {
            if (role.equals("leader") || role.equals("owner")) {
                resource = R.menu.menu_read_messages_owner;
            } else if (role.equals("ambassador")) {
                resource = R.menu.menu_read_messages_ambassador;
            } else {
                resource = R.menu.menu_read_messages;
            }
        } else {
            resource = R.menu.menu_read_messages;
        }
        getMenuInflater().inflate(resource, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (role.equals(User.ROLE_LEADER) || role.equals(User.ROLE_OWNER)) {
            startEditGroup();
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startEditGroup() {
        Intent intent = new Intent(this, EditGroupActivity.class);
        intent.putExtra(EditGroupActivity.EXTRA_GROUP, group);
        startActivityForResult(intent, EditGroupActivity.REQUEST_EDIT_GROUP);
    }

    private void loadMessages() {
        isUpdating = true;
        if (groupTag != null) {
            groupName = groupTag;
        } else if (isAll) {
            groupName = "all";
        } else if (group != null) {
            groupName = group.uniqueID;
        }

        String groupText = "";
        if (groupTag != null) {
            groupText = "#" + groupName;
        } else if (group != null) {
           groupText = isAll ? "#all" : group.name.toLowerCase();
        } else if (directUser != null) {
            groupText = "@" + directUser.firstName;
        }
        createPeepEditText.setHint(String.format("Post a message..."));
        HashMap<String, String> query = new HashMap<>();
        query.put("requestor", User.getCurrentUser().uniqueID);

        peepAdapter = new PeepAdapter(getApplicationContext(), new ArrayList<Peep>());
        peepAdapter.activity = this;
        listView.setAdapter(peepAdapter);
        Peep.fetchPeeps(organization, groupName, directUser, query, new PeepRequestDelegate());
    }

    private void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);
    }

    private void hideProgress() {
        progressBar.setIndeterminate(false);
        progressBar.setVisibility(View.GONE);
    }

    private void fetchOlderPeeps() {

        if (peepAdapter != null) {
            if (peepAdapter.getCount() > 0) {
                if (!isUpdating) {
                    progressBar.setIndeterminate(true);
                    progressBar.setVisibility(View.VISIBLE);
                    isUpdating = true;
                    Peep lastPeep = peepAdapter.getItem(0);
                    HashMap<String, String> query = new HashMap<>();
                    query.put("before", lastPeep.createDate);
                    query.put("requestor", User.getCurrentUser().uniqueID);
                    Peep.fetchPeeps(organization, groupName, directUser, query,
                            new PeepUpdateDelegate(UPDATE_TYPE_OLDER));
                }
            }
        }
    }

    private void fetchNewerPeeps() {
        if (!isUpdating) {
            isUpdating = true;
            if (peepAdapter != null && peepAdapter.getCount() > 0) {
                progressBar.setIndeterminate(true);
                progressBar.setVisibility(View.VISIBLE);
                Peep firstPeep = peepAdapter.getItem(peepAdapter.getCount() - 1);
                HashMap<String, String> query = new HashMap<>();
                query.put("after", firstPeep.createDate);
                query.put("requestor", User.getCurrentUser().uniqueID);
                Peep.fetchPeeps(organization, groupName, directUser, query,
                        new PeepUpdateDelegate(UPDATE_TYPE_NEWER));
            } else {
                loadMessages();
            }
        }
    }

    private boolean listIsAtTop()   {
        if (listView != null) {
            if (listView.getChildCount() == 0) return true;
            return listView.getChildAt(0).getTop() == 0;
        }
        return false;
    }

    private void peepAtachmentClicked() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_IMAGE_POST);
    }

    private void sendPeepClicked(){
        String peepText = createPeepEditText.getText().toString();
        isUpdating = true;
        Peep.postPeep(peepText, organization, groupName, directUser, new PostPeepHandler(this));
        createPeepEditText.setText(null);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private static class PostPeepHandler extends Handler {
        public PostPeepHandler(ReadMessagesActivity activity) {
            mActivity = activity;
        }
        private ReadMessagesActivity mActivity;
        public void handleMessage(Message message) {
            mActivity.isUpdating = false;
            mActivity.fetchNewerPeeps();
        }
    }

    private void getUserTags(String tag){
        if (userTagAdapter == null) {
            HashMap<String, String> query = new HashMap<>();
            query.put("organization", organization);
            if (group != null) {
                query.put("group", group.uniqueID);
            }

            userTagAdapter = new UserTagAdapter(this, new ArrayList<User>());
            User.searchUsers(query, new UserTagDelegate(this));
        }


        if (tag.length() > 0) {
            if (tagList.getAdapter() != userTagAdapter) {
                tagList.setAdapter(userTagAdapter);
                tagList.setOnItemClickListener(new UserClickListener());
            }
            Filter filter = userTagAdapter.getFilter();
            filter.filter(tag);
        }

    }



    private void getHashTags(String tag){
        if (hashTagAdapter == null) {
            ArrayList<String> groups = new ArrayList<>();

            ArrayList<String> groupArray = MessageGroupFragment.getGroupNames();
            groups.add("#all");
            for (String g : groupArray) {
                groups.add("#" + g);
            }
            hashTagAdapter = new HashTagAdapter(this, groups);
        }
        if (tagList.getAdapter() != hashTagAdapter) {
            tagList.setAdapter(hashTagAdapter);
            tagList.setOnItemClickListener(new HashTagClickListener());
        }
        Filter filter = hashTagAdapter.getFilter();
        filter.filter(tag);
        hashTagAdapter.notifyDataSetChanged();
    }

    private class PeepRequestDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            ArrayList<Peep> peeps = (ArrayList<Peep>)object;
            if (peeps.size() > 0) peepAdapter.addAll(peeps);
            peepAdapter.notifyDataSetChanged();
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            peepAdapter.clear();
            ArrayList<Peep> peeps = (ArrayList<Peep>)object;
            if (peeps.size() > 0) peepAdapter.addAll(peeps);

            hideProgress();
            isUpdating = false;
        }
    }

    private class PeepUpdateDelegate implements PrizmDiskCache.CacheRequestDelegate {
        private int mUpdateType;

        public PeepUpdateDelegate(int updateType) {
            mUpdateType = updateType;
        }

        @Override
        public void cached(String path, Object object) {

        }

        @Override
        public void cacheUpdated(String path, Object object) {
            processPeeps(object, false);
            peepAdapter.notifyDataSetChanged();
            hideProgress();
            isUpdating = false;
            atBottom = false;
        }

        private void processPeeps(Object object, Boolean cached) {
            if (object instanceof ArrayList) {
                ArrayList<Peep> peeps = (ArrayList<Peep>)object;
                if (cached) {

                } else {
                    if (mUpdateType == UPDATE_TYPE_NEWER) {
                        peepAdapter.addAll(peeps);
                        peepAdapter.notifyDataSetChanged();
                    } else if (mUpdateType == UPDATE_TYPE_OLDER) {
                        if (peeps.size() == 0) {
                            noOlderMessages = true;
                        } else {
                            final int firstVisibleItem = listView.getFirstVisiblePosition();
                            final int oldCount = peepAdapter.getCount();
                            final View view = listView.getChildAt(0);
                            final int pos = (view == null ? 0 :  view.getBottom());
                            for (int i = 0; i != peeps.size(); ++i) {
                                peepAdapter.insert(peeps.get(i), i);
                            }
                            peepAdapter.notifyDataSetChanged();

                            listView.post(new Runnable() {
                                @Override
                                public void run() {
                                    listView.setSelectionFromTop(firstVisibleItem + peepAdapter.getCount() - oldCount + 1, pos);
                                }
                            });

                        }


                    }
                }

            } else if (object instanceof Peep) {
                Peep obj = (Peep)object;
                int idx = -1;
                Peep p = null;
                int c = peepAdapter.getCount() - 10;
                if (c < 0) c = 0;
                for (int i = c; i != peepAdapter.getCount(); ++i) {
                    p = peepAdapter.getItem(i);
                    if (p.uniqueId.equals(obj.uniqueId)) {
                        idx = i;
                        break;
                    }
                }
                if (idx != -1 && p != null) {
                    peepAdapter.remove(p);
                    peepAdapter.insert(obj, idx);
                    peepAdapter.notifyDataSetChanged();
                } else {
                    peepAdapter.add(obj);
                    peepAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void avatarClicked(String creatorId) {
        User user = new User();
        user.uniqueID = creatorId;
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
        startActivity(intent);
    }

    public void tagClicked(int tagType, String tag) {
        if (tagType == TAG_TYPE_HASH) {
            Intent intent = new Intent(getApplicationContext(), ReadMessagesActivity.class);
            intent.putExtra(EXTRA_ORGANIZATION, organization);
            boolean exists = false;
            if (tag.equals("#all")) {
                exists = true;
                intent.putExtra(EXTRA_IS_ALL, true);
            } else {
                int l = MessageGroupsActivity.groupNames.length;

                for (int i = 0; i != l; ++i) {
                    if (MessageGroupsActivity.groupNames[i].equals(tag)) {
                        exists = true;
                        break;
                    }
                }
                intent.putExtra(EXTRA_GROUP_NAME, "~" + tag.substring(1));
            }
            if (exists) {
                startActivity(intent);
            }
        } else if (tagType == TAG_TYPE_USER) {
//            Toast.makeText(getApplicationContext(), tag, Toast.LENGTH_LONG).show();
            User user = new User();
            user.uniqueID = tag;
            Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
            intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
            startActivity(intent);
        }
    }

    public void peepImageClicked(PeepView peepView) {
        Peep peep = peepView.getPeep();
        Intent intent = new Intent(this.getApplicationContext(), FullScreenImageActivity.class);
        intent.putExtra(FullScreenImageActivity.EXTRA_IMAGE_URL, peep.imageUrl);
        startActivity(intent);
    }

    public void likeButtonClicked(PeepView peepView) {
        Peep peep = peepView.getPeep();
        if (!peep.myPeep) {
            showProgress();
            peep.likePeep(new LikeHandler(this));
        } else {
            if (peep.likesCount > 0) {
                Intent intent = new Intent(getApplicationContext(), LikesActivity.class);
                intent.putExtra(LikesActivity.EXTRA_PEEP, peep);
                startActivity(intent);
            }
        }
    }

    private static class LikeHandler extends Handler {

        private ReadMessagesActivity mActivity;
        public LikeHandler(ReadMessagesActivity activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                Peep mPeep = (Peep)message.obj;
                int size = mActivity.peepAdapter.getCount();

                for (int i = 0; i != size; ++i) {
                    Peep a = mActivity.peepAdapter.getItem(i);
                    if (i < size) {
                        if (a.uniqueId.equals(mPeep.uniqueId)) {
                            mActivity.peepAdapter.remove(a);
                            mActivity.peepAdapter.insert(mPeep, i);
                        }
                    }
                }
                mActivity.peepAdapter.notifyDataSetChanged();

            }
            mActivity.hideProgress();

        }

    }

    public void viewedButtonClicked(PeepView peepView) {
        Intent intent = new Intent(getApplicationContext(), MessageReaders.class);
        intent.putExtra(MessageReaders.EXTRA_MESSAGE, peepView.getPeep());
        startActivity(intent);
    }

    private static class PeepAdapter extends ArrayAdapter<Peep> {


        public PeepAdapter(Context c, List<Peep> items) {
            super(c, 0, items);
        }

        public ReadMessagesActivity activity;

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            PeepView peepView = (PeepView)convertView;
            if (peepView == null) {
                peepView = PeepView.inflate(parent);
            }
            peepView.setPeep(getItem(position));
            peepView.setDelegate(activity);
            peepView.setEnabled(false);
            return peepView;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImage = data.getData();
                ImageHelper ih = ImageHelper.getInstance();
                Bitmap bmp = ih.bitmapFromUri(selectedImage);
                String path = ih.uploadImage(bmp, new RefreshListHandler(this));
                isUpdating = true;
                Peep.postPeep("", path, organization, groupName, directUser, new PostPeepHandler(this));
                Toast.makeText(getApplicationContext(), getString(R.string.notify_upload), Toast.LENGTH_SHORT).show();
            }


        }
    }

    private static class RefreshListHandler extends Handler {
        private ReadMessagesActivity mActivity;
        public RefreshListHandler(ReadMessagesActivity activity) {
            mActivity = activity;
        }
        @Override
        public void handleMessage(Message msg) {
            mActivity.peepAdapter.notifyDataSetChanged();
        }
    }

    private static class UserTagDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private ReadMessagesActivity mActivity;

        public UserTagDelegate(ReadMessagesActivity activity) {
            mActivity = activity;
        }

        @Override
        public void cached(String path, Object object) {
            if (object instanceof ArrayList) {
                mActivity.userTagAdapter.setBaseList((ArrayList<User>) object);
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            if (object instanceof ArrayList) {
                mActivity.userTagAdapter.setBaseList((ArrayList<User>) object);
            }
        }

    }


    private class UserTagAdapter extends ArrayAdapter<User> {

        private UserNameFilter filter;
        private ArrayList<User> baseList;
        private ArrayList<User> userList;

        public UserTagAdapter(Context c, List<User> users) {
            super(c, 0, users);
            this.baseList = new ArrayList<>();
            this.baseList.addAll(users);
            this.userList = new ArrayList<>();
            this.userList.addAll(users);
        }

        @Override
        public int getCount() {
            int count = super.getCount();
            count = count > 4?4:count;
            return count;
        }

        public void setBaseList(ArrayList<User> list) {
            baseList = new ArrayList<>();
            baseList.addAll(list);
            userList = new ArrayList<>();
            userList.addAll(list);
            notifyDataSetChanged();
            clear();
            addAll(baseList);
            notifyDataSetInvalidated();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            UserTagView view = (UserTagView)convertView;
            if (view == null) {
                view = UserTagView.inflate(parent);
            }
            view.setUser(getItem(position));
            return view;
        }

        @Override
        public Filter getFilter() {
            if (filter == null) {
                filter = new UserNameFilter();
            }
            return filter;
        }

        private class UserNameFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                constraint = constraint.toString().toLowerCase().substring(1);
                FilterResults results = new FilterResults();
                Iterator<User> iterator = baseList.iterator();
                ArrayList<User> filt = new ArrayList<>();

                if (constraint != null && constraint.toString().length() > 0) {
                    while (iterator.hasNext()) {
                        User u = iterator.next();
                        if (u.name.toLowerCase().contains(constraint)) {
                            filt.add(u);
                        }
                    }
                }
                results.values = filt;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                userList = (ArrayList<User>)results.values;
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

    private static class HashTagAdapter extends ArrayAdapter<String> {

        public HashTagAdapter(Context c, List<String> hashTags) {
            super(c, 0, hashTags);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            HashTagView view = (HashTagView)convertView;
            if (view == null) {
                view = HashTagView.inflate(parent);
            }
            view.setHashTag(this.getItem(position));
            return view;
        }
    }

    private class UserClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            UserTagView tagView = (UserTagView)view;
            User u = tagView.getUser();
            Editable text = createPeepEditText.getEditableText();
            String name = String.format("@%s ", u.name);
            String tag = String.format("@%s", u.uniqueID);

            Paint paint = new Paint();
            paint.setTextSize(createPeepEditText.getTextSize());
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setColor(createPeepEditText.getCurrentTextColor());
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            float baseline = -paint.ascent();
            float width = paint.measureText(name) + 0.5f;
            float height =  baseline + paint.descent() + 0.5f;

            Bitmap tagImage = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(tagImage);
            canvas.drawText(name, 0, baseline, paint);
            ImageSpan imageSpan = new ImageSpan(getApplicationContext(), tagImage);


            int start = text.toString().indexOf(currentTag);
            text.replace(start, start + currentTag.length(), tag);
            int spanEnd = start + tag.length();
            text.setSpan(imageSpan, start, spanEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.append(" ");


//            text = text.replace(currentTag, tag);
//            int index = text.indexOf(tag);
//            SpannableString spanString = new SpannableString(text);
//            EasyEditSpan ees = new EasyEditSpan();
//            spanString.setSpan(ees, index, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//            messageString = messageString.replace(currentTag, String.format("@%s ", u.uniqueID));
//            createPeepEditText.setText(spanString);
            createPeepEditText.setSelection(text.length());
            typingTag = false;
            tagList.setAdapter(null);

        }
    }

    private class HashTagClickListener implements AdapterView.OnItemClickListener {


        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            HashTagView tagView = (HashTagView)view;
            String tag = tagView.getHashTag();


            String text = createPeepEditText.getText().toString();
            text = text.replace(currentTag, tag + " ");

            messageString = messageString.replace(currentTag, tag + " ");
            createPeepEditText.setText(text);
            createPeepEditText.setSelection(text.length());
            typingTag = false;
            tagList.setAdapter(null);

        }
    }





}
