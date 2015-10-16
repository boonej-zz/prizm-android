package co.higheraltitude.prizm.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import co.higheraltitude.prizm.CreateAccountActivity;
import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.MainActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.Registration;
import co.higheraltitude.prizm.adapters.OptionAdapter;
import co.higheraltitude.prizm.models.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NewUserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewUserFragment extends Fragment {
    // TODO: Rename and change types and number of parameters

    private Button ethnicityButton;
    private Button saveButton;

    private EditText firstNameField;
    private EditText lastNameField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmField;
    private RadioGroup genderGroup;
    private RadioButton femaleButton;
    private RadioButton maleButton;
    private Button religionButton;
    private EditText phoneNumberField;
    private EditText birthdateField;
    private EditText programCodeField;
    private String gender = "unknown";
    private String religion = "";
    private String ethnicity = "";
    private Bundle baseUser;
    private View passwordWrapper;
    private View confirmWrapper;

    public static NewUserFragment newInstance(Bundle base) {
        NewUserFragment fragment = new NewUserFragment();
        fragment.setBaseUser(base);

        return fragment;
    }

    public NewUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mPage = getArguments().getInt(ARG_PAGE);
    }

    private void setBaseUser(Bundle base) {
        baseUser = base;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_user, container, false);
        ethnicityButton = (Button)view.findViewById(R.id.registration_button_ethnicity);

        saveButton = (Button)view.findViewById(R.id.registration_button_save);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onSave(v);
            }
        });
        ethnicityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onEthnicityClick();
            }
        });

        maleButton = (RadioButton)view.findViewById(R.id.radio_male);
        maleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "male";
            }
        });
        femaleButton = (RadioButton)view.findViewById(R.id.radio_female);
        femaleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gender = "female";
            }
        });
        firstNameField = (EditText)view.findViewById(R.id.registration_field_first);
        lastNameField = (EditText)view.findViewById(R.id.registration_field_last);
        emailField = (EditText)view.findViewById(R.id.registration_field_email);
        passwordField = (EditText)view.findViewById(R.id.registration_field_password);
        confirmField = (EditText)view.findViewById(R.id.registration_field_confirm);
        genderGroup = (RadioGroup)view.findViewById(R.id.registration_radio_gender);
        phoneNumberField = (EditText)view.findViewById(R.id.registration_field_phone);
        birthdateField = (EditText)view.findViewById(R.id.registration_field_birthday);
        programCodeField = (EditText)view.findViewById(R.id.registration_field_program_code);
        religionButton = (Button)view.findViewById(R.id.registration_button_religion);
        religionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReligionClick();
            }
        });
        passwordWrapper = view.findViewById(R.id.registration_password_wrapper);
        confirmWrapper = view.findViewById(R.id.registration_confirm_wrapper);

        if (baseUser != null) {
            if (baseUser.getString("email") != null) {
                emailField.setText(baseUser.getString("email"));
            }
            if (baseUser.getString("first_name") != null) {
                firstNameField.setText(baseUser.getString("first_name"));
            }
            if (baseUser.getString("last_name") != null) {
                lastNameField.setText(baseUser.getString("last_name"));
            }
            passwordWrapper.setVisibility(View.GONE);
            confirmWrapper.setVisibility(View.GONE);
        }
        return view;
    }

    private void onEthnicityClick() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.selector_dialog);
        OptionAdapter optionAdapter = new OptionAdapter(getContext(),
                Arrays.asList(getResources().getStringArray(R.array.values_ethnicity)));
        ListView listView = (ListView) dialog.findViewById(R.id.listview_dialog);
        listView.setAdapter(optionAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ethnicity = (String)parent.getAdapter().getItem(position);
                ethnicityButton.setText(ethnicity);
                dialog.hide();
            }
        });
//        Button cancelButton = (Button)dialog.findViewById(R.id.listview_cancel);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.hide();
//            }
//        });

        dialog.show();
    }

    private void onReligionClick() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.selector_dialog);
        OptionAdapter arrayAdapter = new OptionAdapter(getContext(),
                Arrays.asList(getResources().getStringArray(R.array.values_religion)));
        ListView listView = (ListView) dialog.findViewById(R.id.listview_dialog);

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                religion = (String)parent.getAdapter().getItem(position);
                religionButton.setText(religion);
                dialog.dismiss();
            }
        });
//        Button cancelButton = (Button)dialog.findViewById(R.id.listview_cancel);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.hide();
//            }
//        });

        dialog.show();
    }

    public void onSave(View view) {
        Location location = MainActivity.lastLocation();
        String zipPostal = "00000";
        String city = "";
        String state = "";
        String profilePhotoUrl = CreateAccountActivity.profilePhotoUrl;
        String coverPhotoUrl = CreateAccountActivity.coverPhotoUrl;
        if (location != null) {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Address address = addresses.get(0);
                zipPostal = address.getPostalCode();
                city = address.getLocality();
                state = address.getAdminArea();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("type", "user");
        hashMap.put("first_name", firstNameField.getText().toString());
        hashMap.put("last_name", lastNameField.getText().toString());
        hashMap.put("email", emailField.getText().toString());
        hashMap.put("password", passwordField.getText().toString());
        hashMap.put("confirm_password", passwordField.getText().toString());
        hashMap.put("gender", gender);
        hashMap.put("phone_number", phoneNumberField.getText().toString());
        hashMap.put("birthday", birthdateField.getText().toString());
        hashMap.put("zip_postal", zipPostal);
        hashMap.put("city", city);
        hashMap.put("state", state);
        hashMap.put("profile_photo_url", profilePhotoUrl);
        hashMap.put("cover_photo_url", coverPhotoUrl);
        hashMap.put("program_code", programCodeField.getText().toString());
        hashMap.put("ethnicity", ethnicity);
        hashMap.put("religion", religion);
        if (baseUser != null) {
            hashMap.put("provider", baseUser.getString("provider"));
            hashMap.put("provider_token", baseUser.getString("provider_token"));
            hashMap.put("provider_token_secret", baseUser.getString("provider_secret"));
        }
        HandleMessage handler = new HandleMessage();
        handler.setFragment(this);
        User.register(hashMap, handler);
    }

    private static class HandleMessage extends Handler {

        private Fragment mFragment = null;

        public void setFragment(Fragment fragment) {
            mFragment = fragment;
        }
        @Override
        public void handleMessage(Message message) {
            if (message.obj != null) {
                User user = (User) message.obj;
                Intent intent = new Intent(mFragment.getContext(), Registration.class);
                intent.putExtra(LoginActivity.EXTRA_PROFILE, user);
                mFragment.getActivity().setResult(LoginActivity.RESULT_OK, intent);
                mFragment.getActivity().finish();
            }
        }
    }

}
