package co.higheraltitude.prizm.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

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
import java.util.Map;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.network.PrizmAPIService;

/**
 * Created by boonej on 11/13/15.
 */
public class Activity implements Parcelable {

    private static final String ACTIVITY_FORMAT_1 = "/users/%s/activities";
    public static final String ACTIVITY_TYPE_LIKE = "like";
    public static final String ACTIVITY_TYPE_INSIGHT = "insight";
    public static final String ACTIVITY_TYPE_GROUP_ADD = "group_added";
    public static final String ACTIVITY_TYPE_POST = "post";
    public static final String ACTIVITY_TYPE_COMMENT = "comment";
    public static final String ACTIVITY_TYPE_TRUST_REQUEST = "trust_request";
    public static final String ACTIVITY_TYPE_TRUST_ACCEPT = "trust_accepted";
    public static final String ACTIVITY_TYPE_GROUP_APPROVE = "group_approved";
    public static final String ACTIVITY_TYPE_LEADER = "leader";
    public static final String ACTIVITY_TYPE_TAG = "tag";
    public static final String ACTIVITY_TYPE_FOLLOW = "follow";

    public String uniqueId;
    public String createDate;
    public String timeSince;
    public String to;
    public String groupId;
    public boolean hasBeenViewed;
    public String insightTargetId;
    public String commentId;
    public String postId;
    public String action;
    public String fromId;
    public String fromSubtype;
    public String fromType;
    public String fromProfilePhotoUrl;
    public String fromName;
    public String groupName;
    public String insightId;
    public String insightFilePath;
    public String postFilePath;
    public String postText;
    public String messageId;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("time_since", "timeSince");
            put("to", "to");
            put("group_id", "groupId");
            put("has_been_viewed", "hasBeenViewed");
            put("insight_target_id", "insightTargetId");
            put("comment_id", "commentId");
            put("post_id", "postId");
            put("action", "action");
            put("from_id", "fromId");
            put("from_subtype", "fromSubtype");
            put("from_type", "fromType");
            put("from_name", "fromName");
            put("from_profile_photo_url", "fromProfilePhotoUrl");
            put("group_name", "groupName");
            put("insight_id", "insightId");
            put("insight_file_path", "insightFilePath");
            put("post_file_path", "postFilePath");
            put("post_text", "postText");
            put("message_id", "messageId");
        }};
        return map;
    }

    public Activity(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Activity.class;
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Field field;
            if (object.has((String)pair.getKey())) {
                try {
                    field = c.getDeclaredField((String) pair.getValue());
                    field.setAccessible(true);
                    Object value = object.get((String) pair.getKey());
                    if (value instanceof JSONArray) {
                        ArrayList<String> valueList = new ArrayList<>();
                        JSONArray array = (JSONArray)value;
                        for (int i = 0; i != array.length(); ++i) {
                            String v = array.getString(i);
                            valueList.add(v);
                        }
                        field.set(this, valueList);
                    } else {
                        if (value != JSONObject.NULL)
                            field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Activity(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Activity.class;
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
        Class<?> c = Activity.class;
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

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public Activity createFromParcel(Parcel in) {
                    return new Activity(in);
                }

                public Activity[] newArray(int size) {
                    return new Activity[size];
                }
            };

    private static ArrayList<Activity> processActivityList(Object obj)
    {
        ArrayList<Activity> results = new ArrayList<>();
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    Activity activity = new Activity(array.getJSONObject(i));
                    results.add(activity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return results;
    }

    public static void fetchActivities(String last, PrizmDiskCache.CacheRequestDelegate delegate)
    {
        String path = String.format(ACTIVITY_FORMAT_1, User.getCurrentUser().uniqueID);
        if (last != null && !last.isEmpty()) {
            path = path + "?last=" + last;
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new ActivityListDelegate(delegate));
    }

    public static void fetchCounts(Handler handler) {
        String path = String.format(ACTIVITY_FORMAT_1, User.getCurrentUser().uniqueID);
        path = path + "?filter=counts";
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.GET, handler, true);
    }

    private static class ActivityListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public ActivityListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object) {
            ArrayList<Activity> results = processActivityList(object);
            mDelegate.cached(path, results);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            ArrayList<Activity> results = processActivityList(object);
            mDelegate.cacheUpdated(path, results);
        }
    }
}
