package co.higheraltitude.prizm.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.network.PrizmAPIService;

/**
 * Created by boonej on 10/29/15.
 */
public class Interest implements Parcelable {

    private static final String USER_INTEREST_PATTERN = "/users/%s/interests";

    public String uniqueId;
    public Boolean isSubinterest;
    public ArrayList<String> subinterests;
    public String text;
    public String createDate;
    public Boolean selected = false;

    public Interest(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Interest.class;
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
                        if (pair.getKey().equals("member_count")) {
                            Log.d("DEBUG", String.valueOf(value));
                        }
                        if (value != null && !value.equals(JSONObject.NULL)) {
                            field.set(this, value);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public Interest(Parcel in) {
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
        Class<?> c = Interest.class;
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
                    } else if (key.equals("subinterests")) {
                        bundle.putStringArrayList(key, (ArrayList<String>) value);
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
                public Interest createFromParcel(Parcel in) {
                    return new Interest(in);
                }

                public Interest[] newArray(int size) {
                    return new Interest[size];
                }
            };

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("text", "text");
            put("subinterests", "subinterests");
            put("is_subinterest", "isSubinterest");
            put("selected", "selected");
        }};
        return map;
    }

    public static void fetchInterests(PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(USER_INTEREST_PATTERN, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new InterestsDelegate(delegate));
    }

    public static void putInterests(ArrayList<Interest> interests, Handler handler) {
        String path = String.format(USER_INTEREST_PATTERN, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        Gson gson = new Gson();
        post.add("interests", gson.toJson(interests));
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.PUT,
                handler, true);
    }


    private static class InterestsDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public InterestsDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
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

        private ArrayList<Interest> process(Object object) {
            ArrayList<Interest> returnArray = new ArrayList<>();
            if (object instanceof JSONArray) {
                JSONArray arr = (JSONArray)object;
                for (int i = 0; i != arr.length(); ++i) {
                    try {
                        JSONObject obj = arr.getJSONObject(i);
                        Interest interest = new Interest(obj);
                        returnArray.add(interest);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return returnArray;
        }
    }
}
