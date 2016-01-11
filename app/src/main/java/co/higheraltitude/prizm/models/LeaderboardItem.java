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
public class LeaderboardItem implements Parcelable {

    private static final String LEADERBOARD_FORMAT = "/organizations/%s/leaderboard";
    private static final String LEADERBOARD_INDIVIDUAL_FORMAT = "/organizations/%s/leaderboard/%s";

    public String userId;
    public String userFirstName;
    public String userLastName;
    public String userName;
    public String userType;
    public String userSubtype;
    public Boolean userActive;
    public String userProfilePhotoUrl;
    public String organizationNamespace;
    public String rank;
    public int surveyCount = 0;
    public int points = 0;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("user_id", "userId");
            put("user_first_name", "userFirstName");
            put("user_last_name", "userLastName");
            put("user_name", "userName");
            put("user_type", "userType");
            put("user_subtype", "userSubtype");
            put("user_active", "userActive");
            put("user_profile_photo_url", "userProfilePhotoUrl");
            put("organization_namespace", "organizationNamespace");
            put("points", "points");
            put("rank", "rank");
            put("survey_count", "surveyCount");
        }};
        return map;
    }

    public LeaderboardItem(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = LeaderboardItem.class;
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

    public LeaderboardItem(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = LeaderboardItem.class;
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
        Class<?> c = LeaderboardItem.class;
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
                public LeaderboardItem createFromParcel(Parcel in) {
                    return new LeaderboardItem(in);
                }

                public LeaderboardItem[] newArray(int size) {
                    return new LeaderboardItem[size];
                }
            };

    private static Object processList(Object obj)
    {

        if (obj instanceof JSONArray) {
            ArrayList<LeaderboardItem> results = new ArrayList<>();
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    LeaderboardItem activity = new LeaderboardItem(array.getJSONObject(i));
                    results.add(activity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return results;
        } else {
            LeaderboardItem item = null;
            if (obj instanceof JSONObject) {
                item = new LeaderboardItem((JSONObject)obj);
            }
            return item;
        }
    }

    public static void fetchLeaderboard(int skip, PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(LEADERBOARD_FORMAT, User.getCurrentUser().primaryOrganization);
        if (skip != 0) {
            path = path + "?skip=" + String.valueOf(skip);
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache.getInstance(null).performCachedRequest(path, post, HttpMethod.GET,
                new LeaderboardDelegate(delegate));
    }

    public static void fetchIndividualScore(PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(LEADERBOARD_INDIVIDUAL_FORMAT,
                User.getCurrentUser().primaryOrganization, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache.getInstance(null).performCachedRequest(path, post, HttpMethod.GET,
                new LeaderboardDelegate(delegate));
    }

    public User extractUser() {
        User user = new User();
        user.uniqueID = userId;
        user.profilePhotoURL = userProfilePhotoUrl;
        user.type = userType;
        user.subtype = userSubtype;
        user.active = userActive;
        user.name = userName;
        user.firstName = userFirstName;
        user.lastName = userLastName;

        return  user;
    }



    private static class LeaderboardDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public LeaderboardDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object) {
            Object results = processList(object);
            mDelegate.cached(path, results);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            Object results = processList(object);
            mDelegate.cacheUpdated(path, results);
        }
    }
}
