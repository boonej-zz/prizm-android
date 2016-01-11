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
public class Insight implements Parcelable {

    private static final String INSIGHT_GET_FORMAT = "/users/%s/insights?limit=10";
    private static final String INSIGHT_LIKE_FORMAT = "/users/%s/insights/%s";
    private static final String INSIGHT_SINGLE_FORMAT = "/insights/%s";

    public String uniqueId;
    public String timeSince;
    public String hashTags;
    public int hashTagsCount;
    public String linkTitle;
    public String link;
    public String filePath;
    public String text;
    public String title;
    public String createDate;
    public String creatorId;
    public String creatorName;
    public String creatorType;
    public String creatorSubtype;
    public String creatorProfilePhotoUrl;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>() {{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("time_since", "timeSince");
            put("hash_tags", "hashTags");
            put("hash_tags_count", "hashTagsCount");
            put("link_title", "linkTitle");
            put("link", "link");
            put("file_path", "filePath");
            put("text", "text");
            put("title", "title");
            put("create_date", "createDate");
            put("creator_id", "creatorId");
            put("creator_name", "creatorName");
            put("creator_type", "creatorType");
            put("creator_subtype", "creatorSubtype");
            put("creator_profile_photo_url", "creatorProfilePhotoUrl");
        }};
        return map;
    }

    public Insight(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Insight.class;
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

    public Insight(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Insight.class;
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
        Class<?> c = Insight.class;
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
                public Insight createFromParcel(Parcel in) {
                    return new Insight(in);
                }

                public Insight[] newArray(int size) {
                    return new Insight[size];
                }
            };

    private static ArrayList<Insight> processInsightList(Object obj)
    {
        ArrayList<Insight> results = new ArrayList<>();
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    Insight activity = new Insight(array.getJSONObject(i));
                    results.add(activity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return results;
    }

    public static void fetchInsight(String id, PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(INSIGHT_SINGLE_FORMAT, id);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        PrizmDiskCache.getInstance(null).performCachedRequest(path, map, HttpMethod.GET,
                new SingleInsightDelegate(delegate));
    }

    public static void fetchInsights(String type, int skip,
                                     PrizmDiskCache.CacheRequestDelegate delegate) {
       String path = String.format(INSIGHT_GET_FORMAT, User.getCurrentUser().uniqueID);
       MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
       path = path + "&type=" + type;
       if (skip > 0) {
           path = path + "&skip=" + String.valueOf(skip);
       }
       PrizmDiskCache.getInstance(null).performCachedRequest(path, map, HttpMethod.GET,
               new InsightListDelegate(delegate));
    }

    public static void likeInsight(Insight insight, Handler handler) {
        String path = String.format(INSIGHT_LIKE_FORMAT, User.getCurrentUser().uniqueID,
                insight.uniqueId);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, map, HttpMethod.PUT, handler,
                true);
    }

    public static void dislikeInsight(Insight insight, Handler handler) {
        String path = String.format(INSIGHT_LIKE_FORMAT, User.getCurrentUser().uniqueID,
                insight.uniqueId);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, map, HttpMethod.DELETE, handler,
                true);
    }

    private static class InsightListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public InsightListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object) {
            ArrayList<Insight> results = processInsightList(object);
            mDelegate.cached(path, results);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            ArrayList<Insight> results = processInsightList(object);
            mDelegate.cacheUpdated(path, results);
        }
    }

    private static class SingleInsightDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public SingleInsightDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object) {
            mDelegate.cached(path, process(object));
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            ArrayList<Insight> results = processInsightList(object);
            mDelegate.cacheUpdated(path, process(object));
        }

        private Insight process(Object object) {
            Insight insight = null;
            if (object instanceof JSONObject) {
                insight = new Insight((JSONObject)object);
            }
            return insight;
        }
    }
}
