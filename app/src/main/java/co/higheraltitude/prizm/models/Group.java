package co.higheraltitude.prizm.models;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by boonej on 9/5/15.
 */
public class Group implements Parcelable {

    public String uniqueID;


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

    public static final Parcelable.Creator CREATOR =
            new Parcelable.Creator() {
                public User createFromParcel(Parcel in) {
                    return new User(in);
                }

                public User[] newArray(int size) {
                    return new User[size];
                }
            };

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
}
