package co.higheraltitude.prizm.models;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.network.PrizmAPIService;
import co.higheraltitude.prizm.views.GroupView;

/**
 * Created by boonej on 9/5/15.
 */
public class Group implements Parcelable {

    public String uniqueID;
    public String createDate;
    public String name;
    public String organization;
    public String status;
    public String description;
    public String leaderName;
    public String leader;
    public Boolean muted = false;
    public int memberCount;

    private static String ENDPOINT_USER_GROUPS = "/organizations/%s/users/%s/groups";
    private static String FORMAT_GROUPS = "/organizations/%s/groups";
    private static String FORMAT_MUTE_POST_1 = "/organizations/%s/groups/%s/mutes";
    private static String FORMAT_MUTE_DELETE_1 = "/organizations/%s/groups/%s/mutes/%s";

    public Group() {

    }

    public Group(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Group.class;
        if (in != null) {
            Bundle bundle = in.readBundle();
            while (iterator.hasNext()) {
                String key = (String)iterator.next();
                try {
                    Field field = c.getField(key);
                    Object value = null;
                    if (field.getType() == Boolean.class) {
                        value = bundle.getBoolean(key);
                        field.set(this, value);
                    } else if (field.getType() == String.class) {
                        value = bundle.getString(key);
                        field.set(this, value);
                    } else if (field.getType() == int.class) {
                        value = bundle.getInt(key);
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }

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
        Class<?> c = Group.class;
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            try {
                Field field = c.getField(key);
                Object value = field.get(this);
                if (value != null) {
                    if (key.equals("muted")) {
                        bundle.putBoolean(key, (Boolean)value);
                    } else if (value.getClass() == String.class) {
                        bundle.putString(key, (String) value);
                    } else if (key.equals("memberCount")) {
                        bundle.putInt(key, (int)value);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        dest.writeBundle(bundle);
    }

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Group createFromParcel(Parcel in) {
                    return new Group(in);
                }

                public Group[] newArray(int size) {
                    return new Group[size];
                }
            };

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueID");
            put("create_date", "createDate");
            put("name", "name");
            put("organization", "organization");
            put("status", "status");
            put("description", "description");
            put("leader_name", "leaderName");
            put("leader_id", "leader");
            put("member_count", "memberCount");
            put("muted", "muted");
        }};
        return map;
    }

    public Group(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Group.class;
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Field field;
            if (object.has((String)pair.getKey())) {
                try {
                    field = c.getDeclaredField((String) pair.getValue());
                    field.setAccessible(true);
                    Object value = object.get((String) pair.getKey());
                    if (value != null && !value.equals(JSONObject.NULL)) {
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void fetchGroupsForUser(User user, String organization, PrizmDiskCache.CacheRequestDelegate delegate) {
        String endpoint = String.format(ENDPOINT_USER_GROUPS, organization, user.uniqueID);
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        cache.performCachedRequest(endpoint, params, HttpMethod.GET, new GroupsListDelegate(delegate));
    }

    public static void createGroup(HashMap<String, Object> parameters, String organization, final Handler handler) {
        String path = String.format(FORMAT_GROUPS, organization);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("name", (String)parameters.get("name"));
        post.add("organization", organization);
        post.add("leader", (String)parameters.get("leader"));
        post.add("description", (String)parameters.get("description"));
        Gson gson = new Gson();
        ArrayList<String> array = new ArrayList<>();
        for (User obj : (ArrayList<User>)parameters.get("members")) {
            array.add(obj.uniqueID);
        }
        post.add("members", gson.toJson(array));
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.POST, new SingleGroupHandler(handler), true);
    }

    public static void editGroup(HashMap<String, Object> parameters, String group, final Handler handler) {
        String path = String.format(FORMAT_GROUPS, User.getCurrentUser().primaryOrganization);
        path = path + "/" + group;
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("name", (String)parameters.get("name"));
        post.add("organization",  User.getCurrentUser().primaryOrganization);
        post.add("leader", (String)parameters.get("leader"));
        post.add("description", (String)parameters.get("description"));
        Gson gson = new Gson();
        ArrayList<String> array = new ArrayList<>();
        for (User obj : (ArrayList<User>)parameters.get("members")) {
            array.add(obj.uniqueID);
        }
        post.add("members", gson.toJson(array));
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.PUT, new SingleGroupHandler(handler), true);
    }

    public static void muteGroup(String group, final Handler handler) {
        String path = String.format(FORMAT_MUTE_POST_1, User.getCurrentUser().primaryOrganization,
                group);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("user", User.getCurrentUser().uniqueID);
        if (group.equals("all")) {
            PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.POST,
                    new User.UserUpdateHandler(handler), true);
        } else {
            PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.POST,
                    new SingleGroupHandler(handler), true);
        }
    }

    public static void unmuteGroup(String group, final Handler handler) {
        String path = String.format(FORMAT_MUTE_DELETE_1, User.getCurrentUser().primaryOrganization,
                group, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.DELETE,
                new SingleGroupHandler(handler), true);
    }

    public static void fetchOrganizationMemberCount(String organization, Handler handler) {
        String path = "/organizations/" + organization;
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.GET, handler, true);
    }


    private static ArrayList<Group> processGroupJsonArray(JSONArray array) {
        ArrayList<Group> groups = new ArrayList<>();
        int length = array.length();
        for (int i = 0; i != length; ++i) {
            try {
                JSONObject object = array.getJSONObject(i);
                Group group = new Group(object);
                groups.add(group);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return groups;
    }

    private static class GroupsListDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public GroupsListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
         public void cached(String path, Object object) {
            mDelegate.cached(path, process(object));
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            mDelegate.cacheUpdated(path, process(object));
        }

        private ArrayList<Group>process(Object obj) {
            ArrayList<Group> returnGroups = new ArrayList<>();
            if (obj instanceof JSONArray) {
                returnGroups = processGroupJsonArray((JSONArray) obj);
            }
            return returnGroups;
        }
    }

    private static class GroupsListHandler extends Handler {
        private Handler mHandler;

        public GroupsListHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message message) {
            Object obj = message.obj;
            if (obj != null) {
                ArrayList<Group> returnGroups = new ArrayList<>();
                if (obj instanceof JSONArray) {
                    returnGroups = processGroupJsonArray((JSONArray) obj);
                }
                Message mMessage = mHandler.obtainMessage(1, returnGroups);
                mHandler.sendMessage(mMessage);
            } else {
                mHandler.sendEmptyMessage(1);
            }
        }
    }

    private static class SingleGroupHandler extends Handler {
        private Handler mHandler;

        public SingleGroupHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message message) {
            Object obj = message.obj;
            Group group = null;
            if (obj instanceof JSONObject) {
                group = new Group((JSONObject)obj);
            }
            Message mMessage = mHandler.obtainMessage(1, group);
            mHandler.sendMessage(mMessage);
        }
    }

}
