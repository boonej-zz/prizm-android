package co.higheraltitude.prizm.models;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.http.HttpMethod;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Field;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.network.PrizmAPIService;
import retrofit.http.GET;

/**
 * Created by boonej on 9/18/15.
 */
public class Peep implements Parcelable {

    private static String PRIZM_MESSAGE_FORMAT_1 = "/organizations/%s/groups/%s/messages";
    private static String PRIZM_MESSAGE_FORMAT_2 = "/organizations/%s/groups/%s/messages/%s";
    private static String PRIZM_MESSAGE_FORMAT_3 = "/organizations/%s/users/%s/messages?target=%s";
    private static String PRIZM_MESSAGE_FORMAT_4 = "/organizations/%s/users/%s/messages";
    private static String PRIZM_UNREAD_FORMAT_1 = "/organizations/%s/users/%s/unread";
    private static String PRIZM_READ_FORMAT_1 = "/organizations/%s/groups/%s/messages/%s/read";
    private static String PRIZM_LIKES_FORMAT_1 = "/organizations/%s/groups/%s/messages/%s/likes";

    public String uniqueId = "";
    public String group = "";
    public String organization = "";
    public int likesCount = 0;
    public String text = "";
    public String modifyDate = "";
    public String createDate = "";
    public String creatorProfilePhotoUrl = "";
    public String creatorId = "";
    public String creatorSubtype = "";
    public String creatorType = "";
    public Boolean creatorActive = false;
    public String creatorName ="";
    public String imageUrl;
    public String metaImageUrl;
    public String metaTitle;
    public String metaDescription;
    public String metaUrl;
    public String metaVideoUrl;
    public Boolean liked = false;
    public Boolean myPeep = false;
    public Integer readCount = 0;

    public Peep(Parcel in) {
        HashMap<String, String> map = map();
        Collection<String> properties = map.values();
        Iterator iterator = properties.iterator();
        Class<?> c = Peep.class;
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

    public Peep(JSONObject object) {
        HashMap<String, String> map = map();
        if (object.has("nameValuePairs")) {
            try {
                object = object.getJSONObject("nameValuePairs");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        Iterator iterator = map.entrySet().iterator();
        Class<?> c = Peep.class;
        while(iterator.hasNext()) {
            Map.Entry pair = (Map.Entry)iterator.next();
            Field field;
            if (object.has((String)pair.getKey())) {
                try {
                    field = c.getDeclaredField((String) pair.getValue());
                    field.setAccessible(true);
                    Object value = object.get((String) pair.getKey());
                    if (value != JSONObject.NULL) {
                        if (value instanceof JSONObject) {
                            value = value.toString();
                        }
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
        Class<?> c = Peep.class;
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
                public Peep createFromParcel(Parcel in) {
                    return new Peep(in);
                }

                public Peep[] newArray(int size) {
                    return new Peep[size];
                }
            };

    private static HashMap<String, String> map() {
        HashMap<String, String> map = new HashMap<String, String>(){{
            put("_id", "uniqueId");
            put("create_date", "createDate");
            put("modify_date", "modifyDate");
            put("group", "group");
            put("android_text", "text");
            put("organization", "organization");
            put("creator_profile_photo_url", "creatorProfilePhotoUrl");
            put("creator_id", "creatorId");
            put("creator_subtype", "creatorSubtype");
            put("creator_active", "creatorActive");
            put("creator_name", "creatorName");
            put("image_url", "imageUrl");
            put("meta_image_url", "metaImageUrl");
            put("meta_title", "metaTitle");
            put("meta_description", "metaDescription");
            put("meta_video_url", "metaVideoUrl");
            put("meta_url", "metaUrl");
            put("liked", "liked");
            put("my_post", "myPeep");
            put("likes_count", "likesCount");
            put("read_count", "readCount");
            put("creator_type", "creatorType");
        }};
        return map;
    }

    public String timeAgo() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        try {
            date = df.parse(createDate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        PrettyTime prettyTime = new PrettyTime();
        return prettyTime.format(date);
    }

    public static void fetchPeeps(String o, String g, User u, HashMap<String, String> args, final PrizmDiskCache.CacheRequestDelegate delegate) {
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        String path;
        if (u != null) {
            path = String.format(PRIZM_MESSAGE_FORMAT_3, o, User.getCurrentUser().uniqueID, u.uniqueID);
            if (args.containsKey("before")) {
                path = path + "&before=" + args.get("before");
            }
            if (args.containsKey("after")) {
                path = path + "&after=" + args.get("after");
            }
        } else {
            path = String.format(PRIZM_MESSAGE_FORMAT_1, o, g);
            if (args.containsKey("requestor")) {
                path = path + "?requestor=" + args.get("requestor");
                if (args.size() > 1) {
                    path = path + "&";
                }
            } else if (args.size() > 0) {
                path = path + "?";
            }
            if (args.containsKey("before")) {
                path = path + "before=" + args.get("before");
            }
            if (args.containsKey("after")) {
                path = path + "after=" + args.get("after");
            }
        }

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        cache.performCachedRequest(path, map, HttpMethod.GET, new MessageRequestDelegate(delegate));
    }

    public void fetchRead(final PrizmDiskCache.CacheRequestDelegate delegate) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        String groups = group != null & !group.isEmpty()?group:"all";
        String path = String.format(PRIZM_READ_FORMAT_1, organization, groups, uniqueId);
        cache.performCachedRequest(path, post, HttpMethod.GET, new User.UserListDelegate(delegate));
    }

    public static void postPeep(String text, String o, String g, User u, final Handler handler) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("text", text);
        post.add("creator", User.getCurrentUser().uniqueID);
        PrizmAPIService service = PrizmAPIService.getInstance();
        String path;
        if (u != null) {
            path = String.format(PRIZM_MESSAGE_FORMAT_4, o, User.getCurrentUser().uniqueID);
            post.add("target", u.uniqueID);
        } else {
            path = String.format(PRIZM_MESSAGE_FORMAT_1, o, g);
        }
        service.performAuthorizedRequest(path, post,
                HttpMethod.POST, new MessageRequestHandler(handler), true);
    }

    public static void postPeep(String text, String path, String o, String g, User u, final Handler handler) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("text", text);
        post.add("image_url", path);
        post.add("creator", User.getCurrentUser().uniqueID);
        if (u != null) {
            path = String.format(PRIZM_MESSAGE_FORMAT_4, o, User.getCurrentUser().uniqueID);
            post.add("target", u.uniqueID);
        } else {
            path = String.format(PRIZM_MESSAGE_FORMAT_1, o, g);
        }
        PrizmAPIService service = PrizmAPIService.getInstance();
        service.performAuthorizedRequest(path, post,
                HttpMethod.POST, new MessageRequestHandler(handler), true);
    }

    public static void getCounts(Handler handler) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        String path = String.format(PRIZM_UNREAD_FORMAT_1, User.getCurrentUser().primaryOrganization,
                User.getCurrentUser().uniqueID);
        PrizmAPIService.getInstance().performAuthorizedRequest(path, post, HttpMethod.GET, handler, true);
    }

    public void getLikes(PrizmDiskCache.CacheRequestDelegate delegate) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        String _group = group != null && !group.isEmpty()?this.group:"all";
        String path = String.format(PRIZM_LIKES_FORMAT_1, organization,  _group, uniqueId);
        PrizmDiskCache cache = PrizmDiskCache.getInstance(null);
        cache.performCachedRequest(path, post, HttpMethod.GET, new User.UserListDelegate(delegate));
    }


    private static ArrayList<Peep> processJsonList(JSONArray array) {
        ArrayList<Peep> peeps = new ArrayList<>();
        int length = array.length();
        for (int i = 0; i != length; ++i) {
            try {
                JSONObject object = array.getJSONObject(i);
                Peep peep = new Peep(object);
                peeps.add(peep);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return peeps;
    }

    public void likePeep(final Handler handler) {
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("requestor", User.getCurrentUser().uniqueID);
        String groupName = group == null || group.isEmpty()?"all":group;
        String path = String.format(PRIZM_MESSAGE_FORMAT_2, organization, groupName, uniqueId);
        PrizmAPIService service = PrizmAPIService.getInstance();
        service.performAuthorizedRequest(path, post, HttpMethod.POST, new SingleMessageRequestHandler(handler), true);
    }

    private static class MessageRequestDelegate implements PrizmDiskCache.CacheRequestDelegate {

        private PrizmDiskCache.CacheRequestDelegate mDelegate;

        public MessageRequestDelegate(PrizmDiskCache.CacheRequestDelegate delegate) {
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

        private ArrayList<Peep> process(Object object) {
            ArrayList<Peep> peeps = new ArrayList<>();
            if (object instanceof JSONArray) {
                peeps = processJsonList((JSONArray) object);
            }
            return peeps;
        }
    }

    private static class MessageRequestHandler extends Handler {
        private Handler mHandler;

        public MessageRequestHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(android.os.Message message) {
            Object obj = message.obj;
            ArrayList<Peep> peeps = new ArrayList<>();
            if (obj instanceof JSONArray) {
                peeps = processJsonList((JSONArray) obj);
            }
            android.os.Message mMessage = mHandler.obtainMessage(1, peeps);
            mHandler.sendMessage(mMessage);
        }
    }

    private static class SingleMessageRequestHandler extends Handler {
        private Handler mHandler;

        public SingleMessageRequestHandler(Handler handler) {
            mHandler = handler;
        }

        @Override
        public void handleMessage(android.os.Message message) {
            Object obj = message.obj;
            Object peep = null;
            if (obj instanceof JSONObject) {
                peep = new Peep((JSONObject)obj);
            }
            android.os.Message mMessage = mHandler.obtainMessage(1, peep);
            mHandler.sendMessage(mMessage);
        }
    }


}
