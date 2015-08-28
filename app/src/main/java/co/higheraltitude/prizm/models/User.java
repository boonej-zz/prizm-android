package co.higheraltitude.prizm.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

/**
 * Created by boonej on 8/27/15.
 */
public class User implements Parcelable {

    public String firstName = "";
    public String lastName = "";
    public String email = "";

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[]{firstName, lastName, email});

    }

    public User(String _firstName, String _lastName, String _email) {
        firstName = _firstName;
        lastName = _lastName;
        email = _email;
    }

    public User(Parcel in){
        String[] data = in.createStringArray();
        firstName = data[0];
        lastName = data[1];
        email = data[2];
    }

    public User(JSONObject object) {
        try {
            firstName = object.getString("first_name");
            lastName = object.getString("last_name");
            email = object.getString("email");
        } catch (Exception ex) {
            ex.printStackTrace();
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
}
