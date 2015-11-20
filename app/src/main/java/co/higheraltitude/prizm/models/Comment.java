package co.higheraltitude.prizm.models;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
 * Created by boonej on 11/11/15.
 */
public class Comment implements Parcelable{

    public static final String COMMENT_FORMAT = "/posts/%s/comments?requestor=%s";
    public static final String LIKE_COMMENT_FORMAT = "/posts/%s/comments/%s/likes";
    public static final String UNLIKE_COMMENT_FORMAT = "/posts/%s/comments/%s/likes/%s";
    public static final String CREATE_COMMENT_FORMAT = "/posts/%s/comments";


    public String uniqueId;
    public String status;
    public int likesCount;
    public String createDate;
    public String text;
    public String timeSince;
    public String creatorId;
    public String creatorType;
    public String creatorSubtype;
    public String creatorProfilePhotoUrl;
    public boolean isLiked;
    public String creatorName;
    public boolean ownComment;

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("text", "text");
            put("likes_count", "likesCount");
            put("time_since", "timeSince");
            put("creator_id", "creatorId");
            put("creator_type", "creatorType");
            put("creator_subtype", "creatorSubtype");
            put("creator_profile_photo_url", "creatorProfilePhotoUrl");
            put("creator_name", "creatorName");
            put("comment_liked", "isLiked");
            put("own_comment", "ownComment");
            put("status", "status");
        }};
        return map;
    }

    public Comment(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Comment.class;
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

    public Comment(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Comment.class;
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
                    } else if (field.getType() == Integer.class) {
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
        Class<?> c = Comment.class;
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
                public Comment createFromParcel(Parcel in) {
                    return new Comment(in);
                }

                public Comment[] newArray(int size) {
                    return new Comment[size];
                }
            };


    private static ArrayList<Comment> processCommentList(Object obj)
    {
        ArrayList<Comment> results = new ArrayList<>();
        if (obj instanceof JSONArray) {
            JSONArray array = (JSONArray)obj;
            int length = array.length();
            for (int i = 0; i != length; ++i) {
                try {
                    Comment post = new Comment(array.getJSONObject(i));
                    results.add(post);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        }
        return results;
    }

    public static void fetchComments(Post post, PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(COMMENT_FORMAT, post.uniqueId, User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, data, HttpMethod.GET, new CommentListDelegate(delegate));
    }

    public static void likeComment(Post post, Comment comment, Handler handler) {
        String path = String.format(LIKE_COMMENT_FORMAT, post.uniqueId, comment.uniqueId);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("user", User.getCurrentUser().uniqueID);
        PrizmAPIService.getInstance().performAuthorizedRequest(path, data, HttpMethod.PUT,
                new CommentListHandler(handler), true);
    }

    public static void unlikeComment(Post post, Comment comment, Handler handler) {
        String path = String.format(UNLIKE_COMMENT_FORMAT, post.uniqueId, comment.uniqueId,
                User.getCurrentUser().uniqueID);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        PrizmAPIService.getInstance().performAuthorizedRequest(path, data, HttpMethod.DELETE,
                new CommentListHandler(handler), true);
    }

    public static void getCommentLikes(Post post, Comment comment,
                                       PrizmDiskCache.CacheRequestDelegate delegate) {
        String path = String.format(LIKE_COMMENT_FORMAT, post.uniqueId, comment.uniqueId);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, data, HttpMethod.GET, new User.UserListDelegate(delegate));
    }

    public static void createComment(Post post, String text,
                                     Handler handler) {
        String path = String.format(CREATE_COMMENT_FORMAT, post.uniqueId);
        MultiValueMap<String, String> data = new LinkedMultiValueMap<>();
        data.add("creator", User.getCurrentUser().uniqueID);
        data.add("text", text);
        PrizmAPIService.getInstance().performAuthorizedRequest(path, data, HttpMethod.POST,
                new CommentListHandler(handler), true);
    }


    private static class CommentListDelegate implements PrizmDiskCache.CacheRequestDelegate
    {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public CommentListDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
            mDelegate = delegate;
        }

        @Override
        public void cached(String path, Object object)
        {
           mDelegate.cached(path, processCommentList(object));
        }

        @Override
        public void cacheUpdated(String path, Object object)
        {
            mDelegate.cacheUpdated(path, processCommentList(object));
        }
    }

    private static class CommentListHandler extends Handler {

        private Handler mHandler;

        public CommentListHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof JSONArray) {
                ArrayList<Comment> comments = processCommentList(msg.obj);
                Message message = mHandler.obtainMessage(1, comments);
                mHandler.sendMessage(message);
            }
        }
    }
}
