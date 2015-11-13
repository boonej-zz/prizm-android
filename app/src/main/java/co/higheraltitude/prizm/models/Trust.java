package co.higheraltitude.prizm.models;

import android.os.Bundle;
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

/**
 * Created by boonej on 11/13/15.
 */
public class Trust implements Parcelable {

    private static final String TRUST_FORMAT_1 = "/users/%s/trusts?filter=activity";
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

    public String uniqueId;
    public String to;
    public String createDate;
    public String modifyDate;
    public String status;
    public String fromName;
    public String fromId;
    public String fromProfilePhotoUrl;
    public String fromType;
    public String fromSubtype;
    public String timeSince;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("_id", "uniqueId");
            put("to", "to");
            put("create_date", "createDate");
            put("modify_date", "modifyDate");
            put("status", "status");
            put("from_name", "fromName");
            put("from_id", "fromId");
            put("from_profile_photo_url", "fromProfilePhotoUrl");
            put("from_type", "fromType");
            put("from_subtype", "fromSubtype");
            put("time_since", "timeSince");
        }};
        return map;
    }

    public Trust(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Trust.class;
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

    public Trust(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Trust.class;
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
        Class<?> c = Trust.class;
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

    public static final Creator CREATOR =
            new Creator() {
                public Trust createFromParcel(Parcel in) {
                    return new Trust(in);
                }

                public Trust[] newArray(int size) {
                    return new Trust[size];
                }
            };

    private static ArrayList<Trust> processTrustList(Object obj)
    {
        ArrayList<Trust> results = new ArrayList<>();
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    Trust activity = new Trust(array.getJSONObject(i));
                    results.add(activity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return results;
    }

    public static void fetchTrustActivity(String last, PrizmDiskCache.CacheRequestDelegate delegate)
    {
        String path = String.format(TRUST_FORMAT_1, User.getCurrentUser().uniqueID);
        if (last != null && !last.isEmpty()) {
            path = path + "&last=" + last;
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new TrustListDelegate(delegate));
    }

    private static class TrustListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public TrustListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object) {
            ArrayList<Trust> results = processTrustList(object);
            mDelegate.cached(path, results);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            ArrayList<Trust> results = processTrustList(object);
            mDelegate.cacheUpdated(path, results);
        }
    }
}
