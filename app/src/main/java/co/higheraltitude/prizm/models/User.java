package co.higheraltitude.prizm.models;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
import com.google.gson.Gson;
import com.twitter.sdk.android.core.TwitterAuthToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import co.higheraltitude.prizm.MainActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.network.PrizmAPIService;


/**
 * Created by boonej on 8/27/15.
 */
public class User implements Parcelable {

    public String uniqueID;
    public String firstName;
    public String lastName;
    public String email;
    public String name;
    public String info;
    public String website;
    public String ethnicity;
    public String religion;
    public String phoneNumber;
    public String gender;
    public String birthday;
    public String city;
    public String state;
    public String zipPostal;
    public String coverPhotoURL;
    public String profilePhotoURL;
    public Boolean active;
    public String primaryOrganization;
    public String role;
    public Boolean isMember;
    public String theme;
    public String subtype;
    public Boolean read;
    public int interestCount;

    public static String PrizmCurrentUserCacheKey = "current_user";
    private static final String PRIZM_LOGIN_ENDPOINT = "/oauth2/login";
    private static final String PRIZM_USER_ENDPOINT = "/users";
    private static final String PRIZM_MESSAGE_USER_FORMAT = "/organizations/%s/users/%s/messages?format=digest";
    private static final String PRIZM_MESSAGE_USER_FORMAT_2 = "/organizations/%s/users/%s/contacts";
    private static final String PRIZM_GROUP_USER_FORMAT = "/organizations/%s/groups/%s/members";
    private static final String PRIZM_ORG_USER_FORMAT = "/organizations/%s/members";
    private static final String PRIZM_DEVICE_REGISTER_FORMAT = "/users/%s/devices";

    public static String ROLE_LEADER = "leader";
    public static String ROLE_OWNER = "owner";
    public static String ROLE_AMBASSADOR = "ambassador";

    public User() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();

        Bundle bundle = new Bundle();
        Iterator iterator = properties.iterator();
        Class<?> c = User.class;
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            try {
                Field field = c.getField(key);
                Object value = field.get(this);
                if (value != null) {
                    if (value.getClass() == boolean.class) {
                        bundle.putBoolean(key, (boolean) value);
                    } else if (value.getClass() == String.class) {
                        bundle.putString(key, (String) value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dest.writeBundle(bundle);
    }

    private static Object cacheableObject(User user) {
        HashMap<String, String> map = map();
        Set<String> keys = map.keySet();
        Iterator iterator = keys.iterator();
        HashMap<String, Object> returnObject = new HashMap<>();
        Class<?> c = User.class;
        while (iterator.hasNext()) {
            String destKey = (String)iterator.next();
            String key = map.get(destKey);
            try {
                Field field = c.getField(key);
                Object value = field.get(user);
                returnObject.put(destKey, value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return returnObject;
    }

    private User(HashMap<String, String> userMap) {
        HashMap<String, String> map = map();
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = User.class;
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Field field = null;
            String value = userMap.get((String)pair.getKey());
            if (value != null) {
                try {
                    field = c.getDeclaredField((String) pair.getValue());
                    field.setAccessible(true);
                    if (value != null) {
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueID");
            put("first_name", "firstName");
            put("last_name", "lastName");
            put("email", "email");
            put("name", "name");
            put("info", "info");
            put("website", "website");
            put("ethnicity", "ethnicity");
            put("religion", "religion");
            put("phone_number", "phoneNumber");
            put("gender", "gender");
            put("birthday", "birthday");
            put("city", "city");
            put("state", "state");
            put("zip_postal", "zipPostal");
            put("cover_photo_url", "coverPhotoURL");
            put("profile_photo_url", "profilePhotoURL");
            put("active", "active");
            put("primary_organization", "primaryOrganization");
            put("role", "role");
            put("is_member", "isMember");
            put("theme", "theme");
            put("subtype", "subtype");
            put("read", "read");
            put("interest_count", "interestCount");
        }};
        return map;
    }

    public User(String _firstName, String _lastName, String _email) {
        firstName = _firstName;
        lastName = _lastName;
        email = _email;
    }

    public User(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = User.class;
        if (in != null) {
            Bundle bundle = in.readBundle();
            while (iterator.hasNext()) {
                String key = (String)iterator.next();
                try {
                    Field field = c.getField(key);
                    Object value = null;
                    if (field.getType() == boolean.class) {
                        value = bundle.getBoolean(key);
                        field.set(this, value);
                    } else if (field.getType() == String.class) {
                        value = bundle.getString(key);
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

    }

    public User(JSONObject object) {
        HashMap<String, String> map = map();
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = User.class;
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Field field = null;
            if (object.has((String)pair.getKey())) {
                try {
                    field = c.getDeclaredField((String) pair.getValue());
                    field.setAccessible(true);
                    Object value = object.get((String) pair.getKey());
                    if (value != null && value != JSONObject.NULL) {
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        if (!object.has("primary_organization")) {
            try {
                if (object.has("org_status")) {
                    JSONArray orgStatus = object.getJSONArray("org_status");
                    int length = orgStatus.length();
                    JSONObject status = null;
                    for (int i = 0; i != length; ++i) {
                        JSONObject temp = orgStatus.getJSONObject(i);
                        if (temp.getString("status").equals("active")) {
                            status = temp;
                        }
                    }
                    if (status != null) {
                        primaryOrganization = status.getString("organization");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public User createFromParcel(Parcel in) {
                    return new User(in);
                }

                public User[] newArray(int size) {
                    return new User[size];
                }
            };

    public String toJSON() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    private static void setTheme(User user) {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        int style = R.style.PrizmBlue;
        if (user != null && user.theme != null) {
            if (user.theme.equals("purple")) {
                style = R.style.PrizmPurple;
            } else if (user.theme.equals("red")) {
                style = R.style.PrizmRed;
            } else if (user.theme.equals("green")) {
                style = R.style.PrizmGreen;
            } else if (user.theme.equals("pink")) {
               style =  R.style.PrizmPink;
            } else if (user.theme.equals("orange")) {
                style = R.style.PrizmOrange;
            } else if (user.theme.equals("black")) {
                style = R.style.PrizmBlack;
            }
        }
        cache.storeInt("theme", style);
    }

    public static int getTheme() {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        return cache.readInt("theme");
    }

    public static User getCurrentUser() {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        User user = null;
        try {
            String data = cache.readString(PrizmCurrentUserCacheKey);
            if (data != null && !data.equals("")) {
                JSONObject obj = new JSONObject(data);
                user = new User(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (user != null && user.theme != null) {
            setTheme(user);
        }

        return user;
    }

    public static void setCurrentUser(User user) {
        setTheme(user);
        Gson gson = new Gson();
        Object cacheable = cacheableObject(user);
        String json = gson.toJson(cacheable);
        Log.d("DEBUG", json);
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.storeString(PrizmCurrentUserCacheKey, json);
    }

    public static void login(String email, String password, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
        login.add("email", email);
        login.add("password", password);
        service.performAuthorizedRequest(PRIZM_LOGIN_ENDPOINT, login, HttpMethod.POST, new HandleLoginMessage(handler));
    }


    private static class HandleLoginMessage extends Handler {
        private Handler mHandler;

        public HandleLoginMessage(Handler handler) {
            mHandler = handler;
        }
        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                try {
                    JSONObject userObject = (JSONObject) message.obj;
                    JSONArray dataArray = userObject.getJSONArray("data");
                    if (dataArray.length() > 0) {
                        JSONObject userProfile = dataArray.getJSONObject(0);
                        User user = new User(userProfile);
                        setCurrentUser(user);
                        Message message1 = mHandler.obtainMessage(1, user);
                        mHandler.sendMessage(message1);
                    } else {
                        mHandler.sendEmptyMessage(1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    mHandler.sendEmptyMessage(1);
                }
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    public static void login(TwitterAuthToken token, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("provider_token", token.token);
        post.add("provider_token_secret", token.secret);
        post.add("provider", "twitter");
        service.performAuthorizedRequest(PRIZM_LOGIN_ENDPOINT, post, HttpMethod.POST, new LoginHandler(handler) {

        });

    }

    public static class LoginHandler extends Handler {
        private Handler mHandler;
        public LoginHandler(Handler handler) {
            mHandler = handler;
        }
        public void handleMessage(Message message) {
            if (message.obj != null) {
                JSONObject userObject = (JSONObject) message.obj;
                try {
                    JSONArray dataArray = userObject.getJSONArray("data");
                    if (dataArray.length() > 0) {
                        JSONObject userProfile = dataArray.getJSONObject(0);
                        User user = new User(userProfile);
                        setCurrentUser(user);
                        Message message1 = mHandler.obtainMessage(1, user);
                        mHandler.sendMessage(message1);
                    } else {
                        mHandler.sendEmptyMessage(1);
                    }
                } catch (JSONException ex) {
                    ex.printStackTrace();
                    mHandler.sendEmptyMessage(1);
                }
            } else {
                 mHandler.sendEmptyMessage(0);
            }
        }
    }

    public static void login(AccessToken token, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("provider_token", token.getToken());
        post.add("provider", "facebook");
        service.performAuthorizedRequest(PRIZM_LOGIN_ENDPOINT, post, HttpMethod.POST, new LoginHandler(handler));
    }

    public static void logout(Context context) {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(context);
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        cache.clearValue(PrizmCurrentUserCacheKey);
        System.exit(0);

    }

    public static void register(HashMap<String, String> map, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        Collection<String> properties = map.keySet();
        Iterator iterator = properties.iterator();
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            Object value = map.get(key);
            if (value != null) {
                post.add(key, value.toString());
            }
        }
        service.performAuthorizedRequest(PRIZM_USER_ENDPOINT, post, HttpMethod.POST, new Handler() {
            public void handleMessage(Message message) {
                if (message != null && message.obj != null) {
                    JSONObject userObject = (JSONObject) message.obj;
                    try {
                        JSONArray dataArray = userObject.getJSONArray("data");
                        if (dataArray.length() > 0) {
                            JSONObject userProfile = dataArray.getJSONObject(0);
                            User user = new User(userProfile);
                            setCurrentUser(user);
                            Message message1 = handler.obtainMessage(1, user);
                            handler.sendMessage(message1);
                        } else {
                            handler.sendEmptyMessage(1);
                        }
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        });

    }

    public static void fetchUserCore(User user, final PrizmDiskCache.CacheRequestDelegate delegate) {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        cache.performCachedRequest(PRIZM_USER_ENDPOINT + '/' + user.uniqueID, post, HttpMethod.GET,
                new UserCoreDelegate(delegate));
    }

    private static class UserCoreDelegate implements PrizmDiskCache.CacheRequestDelegate {

        PrizmDiskCache.CacheRequestDelegate mDelegate;

        public UserCoreDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }
        @Override
        public void cached(String path, Object object) {
            User user = null;
            if (object instanceof JSONObject) {
                user = new User((JSONObject)object);
            }
            mDelegate.cached(path, user);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            User user = null;
            if (object instanceof JSONObject) {
                user = new User((JSONObject)object);
                if (user != null && user.uniqueID.equals(getCurrentUser().uniqueID)) {
                    setCurrentUser(user);
                }
            }
            mDelegate.cacheUpdated(path, user);
        }
    }

    public static void registerDevice(final String device) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("device", device);
        String path = String.format(PRIZM_DEVICE_REGISTER_FORMAT, getCurrentUser().uniqueID);
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.PUT, new EmptyHandler(), true);
    }

    private static class EmptyHandler extends  Handler {
        @Override
        public void handleMessage(Message msg) {
            Object o = msg.obj;
            Log.d("DEBUG", "Retrieved object");
        }
    }

    public static void searchUsers(HashMap<String, String> query, final PrizmDiskCache.CacheRequestDelegate delegate) {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        String search = null;
        String organization = null;
        String group = null;
        String queryString = null;
        if (query.containsKey("search")) {
            search = query.get("search");
        }
        if (query.containsKey("organization")) {
            organization = query.get("organization");
        }
        if (query.containsKey("group")) {
            group = query.get("group");
        }

        if (search != null) {
            if (organization != null) {
                if (group != null) {
                    queryString = String.format("?search=%s&organization=%s&group=%s",
                            search, organization, group);
                } else {
                    queryString = String.format("?search=%s&organization=%s", search, organization);
                }
            } else {
                queryString = String.format("?search=%s", search);
            }
        } else if (organization != null) {
            if (group != null) {
                queryString = String.format("?organization=%s&group=%s",
                        organization, group);
            } else {
                queryString = String.format("?organization=%s",
                        organization);
            }
        }

        String path = String.format("%s%s", PRIZM_USER_ENDPOINT, queryString);
        cache.performCachedRequest(path, post, HttpMethod.GET, new UserListDelegate(delegate));
    }

    public static void fetchAvailableMessageRecipients(String oid, final PrizmDiskCache.CacheRequestDelegate delegate) {
        User user = User.getCurrentUser();
        String path = String.format(PRIZM_MESSAGE_USER_FORMAT, oid, user.uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new UserListDelegate(delegate));
    }

    public static void fetchUserContacts(String oid, String lastItem, final PrizmDiskCache.CacheRequestDelegate delegate) {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        String path = String.format(PRIZM_MESSAGE_USER_FORMAT_2, oid, getCurrentUser().uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        if (lastItem != null) {
            try {
                lastItem = URLEncoder.encode(lastItem, "UTF-8");
                path = path + "?last=" + lastItem;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        cache.performCachedRequest(path, post, HttpMethod.GET, new UserListDelegate(delegate));
    }

    public static void fetchOrganizationMembers(String oid, String lastItem, final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path =  String.format(PRIZM_ORG_USER_FORMAT, oid);
        if (lastItem != null) {
            try {
                lastItem = URLEncoder.encode(lastItem, "UTF-8");
                path = path + "?last=" + lastItem;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new UserListDelegate(delegate));
    }

    public static void fetchGroupMembers(String gid, String lastItem, boolean allUsers, final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path =  String.format(PRIZM_GROUP_USER_FORMAT, getCurrentUser().primaryOrganization, gid);
        if (allUsers) {
            path = path + "?show_all=true&";
        }
        if (lastItem != null) {
            try {
                lastItem = URLEncoder.encode(lastItem, "UTF-8");
                path = path + "last=" + lastItem;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else if (allUsers){
            path = path.substring(0, path.length() - 1);
        }
        Log.d("DEBUG", path);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new UserListDelegate(delegate));
    }

    public static class UserListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public UserListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object) {
            ArrayList<User> users = null;
            if (object != null) {
                users = processObject(object);
            }
            mDelegate.cached(path, users);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            ArrayList<User> users = null;
            if (object != null) {
                users = processObject(object);
            }
            mDelegate.cacheUpdated(path, users);
        }

        private ArrayList<User> processObject(Object obj) {
            ArrayList<User> userList = new ArrayList<>();
            if (obj instanceof  JSONArray) {
                for (int i = 0; i != ((JSONArray)obj).length(); ++i) {
                    try {
                        JSONObject o = ((JSONArray) obj).getJSONObject(i);
                        User u = new User(o);
                        userList.add(u);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return userList;
        }
    }

    public static class UserListHandler extends Handler {
        private Handler mHandler;

        public UserListHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            Object obj = msg.obj;
            ArrayList<User> userList = new ArrayList<>();
            if (obj instanceof  JSONArray) {
                for (int i = 0; i != ((JSONArray)obj).length(); ++i) {
                    try {
                        JSONObject o = ((JSONArray) obj).getJSONObject(i);
                        User u = new User(o);
                        userList.add(u);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                Message message = mHandler.obtainMessage(1, userList);
                mHandler.sendMessage(message);
            }
        }
    }



}
