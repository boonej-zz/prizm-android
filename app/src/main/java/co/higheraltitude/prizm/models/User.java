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
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import co.higheraltitude.prizm.MainActivity;
import co.higheraltitude.prizm.cache.PrizmCache;
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
    public String active;

    public static String PrizmCurrentUserCacheKey = "current_user";
    private static final String PRIZM_LOGIN_ENDPOINT = "/oauth2/login";
    private static final String PRIZM_USER_ENDPOINT = "/users";


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
                if (value.getClass() == boolean.class) {
                    bundle.putBoolean(key, (boolean) value);
                } else if (value.getClass() == String.class) {
                    bundle.putString(key, (String)value);
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
        HashMap<String, String> returnObject = new HashMap<>();
        Class<?> c = User.class;
        while (iterator.hasNext()) {
            String destKey = (String)iterator.next();
            String key = map.get(destKey);
            try {
                Field field = c.getField(key);
                Object value = field.get(user);
                returnObject.put(destKey, (String) value);
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
                    if (value != null) {
                        field.set(this, value);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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

    public static User getCurrentUser() {
        PrizmCache cache = PrizmCache.getInstance();
        User user = null;
        try {
            Object object = cache.objectCache.get(PrizmCurrentUserCacheKey);
            if (object != null) {
                JSONObject obj = new JSONObject((String)object);
                user = new User(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void setCurrentUser(User user) {
        Gson gson = new Gson();
        Object cacheable = cacheableObject(user);
        String json = gson.toJson(cacheable);
        PrizmCache cache = PrizmCache.getInstance();
        cache.objectCache.put(PrizmCurrentUserCacheKey, json);
    }

    public static void login(String email, String password, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> login = new LinkedMultiValueMap<>();
        login.add("email", email);
        login.add("password", password);
        service.performAuthorizedRequest(PRIZM_LOGIN_ENDPOINT, login, HttpMethod.POST, new Handler() {
            @Override
            public void handleMessage(Message message) {
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
        });
    }

    public static void login(TwitterAuthToken token, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("provider_token", token.token);
        post.add("provider_token_secret", token.secret);
        post.add("provider", "twitter");
        service.performAuthorizedRequest(PRIZM_LOGIN_ENDPOINT, post, HttpMethod.POST, new Handler(){
            public void handleMessage(Message message) {
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
        });

    }

    public static void login(AccessToken token, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        post.add("provider_token", token.getToken());
        post.add("provider", "facebook");
        service.performAuthorizedRequest(PRIZM_LOGIN_ENDPOINT, post, HttpMethod.POST, new Handler() {
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

    public static void logout(Context context) {
        PrizmCache cache = PrizmCache.getInstance();
        cache.clearObjectCache();
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);

    }

    public static void register(HashMap<String, String> map, final Handler handler) {
        PrizmAPIService service = PrizmAPIService.getInstance();
        MultiValueMap<String, String> post = new LinkedMultiValueMap<>();
        Collection<String> properties = map.keySet();
        Iterator iterator = properties.iterator();
        while (iterator.hasNext()) {
            String key = (String)iterator.next();
            post.add(key, map.get(key).toString());
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



}
