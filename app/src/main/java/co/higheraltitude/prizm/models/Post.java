package co.higheraltitude.prizm.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

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

/**
 * Created by boonej on 11/11/15.
 */
public class Post implements Parcelable{

    public static final String POST_HOME_FEED_FORMAT = "/users/%s/home";

    public String uniqueId;
    public String category;
    public String externalProvider;
    public String hashTags;
    public int commentsCount;
    public int likesCount;
    public String filePath;
    public int locationLatitude;
    public int locationLongitude;
    public String createDate;
    public String text;
    public String timeSince;
    public String creatorId;
    public String creatorType;
    public String creatorSubtype;
    public String creatorProfilePhotoUrl;
    public Boolean isLiked;
    public String creatorName;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("text", "text");
            put("category", "category");
            put("external_provider", "externalProvider");
            put("hash_tags", "hashTags");
            put("comments_count", "commentsCount");
            put("likes_count", "likesCount");
            put("file_path", "filePath");
            put("location_latitude", "locationLatitude");
            put("location_longitude", "locationLongitude");
            put("time_since", "timeSince");
            put("creator_id", "creatorId");
            put("creator_type", "creatorType");
            put("creator_subtype", "creatorSubtype");
            put("creator_profile_photo_url", "creatorProfilePhotoUrl");
            put("creator_name", "creatorName");
            put("is_liked", "isLiked");
        }};
        return map;
    }

    public Post(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Post.class;
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

    public Post(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Post.class;
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
        Class<?> c = Post.class;
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
                public Interest createFromParcel(Parcel in) {
                    return new Interest(in);
                }

                public Interest[] newArray(int size) {
                    return new Interest[size];
                }
            };


    private static ArrayList<Post> processPostList(Object obj)
    {
        ArrayList<Post> results = new ArrayList<>();
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    Post post = new Post(array.getJSONObject(i));
                    results.add(post);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return results;
    }

    public static void fetchHomeFeed(String lastDate, final PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(POST_HOME_FEED_FORMAT, User.getCurrentUser().uniqueID);
        if (lastDate != null && !lastDate.isEmpty()) {
            path = path + "?last=" + lastDate;
        }
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new PostListDelegate(delegate));

    }

    private static class PostListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public PostListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object)
        {
           mDelegate.cached(path, processPostList(object));
        }

        @Override
        public void cacheUpdated(String path, Object object)
        {
            mDelegate.cacheUpdated(path, processPostList(object));
        }
    }
}
