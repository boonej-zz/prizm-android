package co.higheraltitude.prizm.fragments;

import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import co.higheraltitude.prizm.CreateAccountActivity;
import co.higheraltitude.prizm.EditProfile;
import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.MainActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.Registration;
import co.higheraltitude.prizm.adapters.OptionAdapter;
import co.higheraltitude.prizm.models.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EditUserFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditUserFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditUserFragment extends Fragment {
    // TODO: Rename and change types and number of parameters

    private View ethnicityButton;
    private TextView ethnicityText;
    private Button saveButton;

    private EditText firstNameField;
    private EditText lastNameField;
    private EditText emailField;
    private EditText infoField;
    private RadioGroup genderGroup;
    private RadioButton femaleButton;
    private RadioButton maleButton;
    private View religionButton;
    private EditText phoneNumberField;
    private EditText birthdateField;
    private EditText programCodeField;
    private String gender = "unknown";
    private String religion = "";
    private String ethnicity = "";
    private User baseUser;
    private EditText zipCodeField;
    private TextView religionText;
    private String city = "";
    private String state = "";

    private EditText websiteField;

    public static EditUserFragment newInstance(User user) {
        EditUserFragment fragment = new EditUserFragment();
        fragment.setBaseUser(user);

        return fragment;
    }

    public EditUserFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mPage = getArguments().getInt(ARG_PAGE);
    }

    private void setBaseUser(User base) {
        baseUser = base;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_user, container, false);
        ethnicityButton = view.findViewById(R.id.registration_button_ethnicity);
        ethnicityText = (TextView)view.findViewById(R.id.registration_ethnicity_text);
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
        infoField = (EditText)view.findViewById(R.id.registration_field_info);
        genderGroup = (RadioGroup)view.findViewById(R.id.registration_radio_gender);
        phoneNumberField = (EditText)view.findViewById(R.id.registration_field_phone);
        birthdateField = (EditText)view.findViewById(R.id.registration_field_birthday);
        programCodeField = (EditText)view.findViewById(R.id.registration_field_program_code);
        religionButton = view.findViewById(R.id.registration_button_religion);
        religionText = (TextView)view.findViewById(R.id.registation_religion_text);
        religionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onReligionClick();
            }
        });
        zipCodeField = (EditText)view.findViewById(R.id.registration_field_zipcode);
        websiteField = (EditText)view.findViewById(R.id.registration_field_website);

        if (baseUser != null) {

            emailField.setText(baseUser.email);
            firstNameField.setText(baseUser.firstName);
            lastNameField.setText(baseUser.lastName);
            zipCodeField.setText(baseUser.zipPostal);
            infoField.setText(baseUser.info);
            websiteField.setText(baseUser.website);
            birthdateField.setText(baseUser.birthday);
            programCodeField.setText(baseUser.programCode);
            phoneNumberField.setText(baseUser.phoneNumber);
            if (baseUser.ethnicity != null && !baseUser.ethnicity.isEmpty())
                ethnicityText.setText(baseUser.ethnicity);
            ethnicity = baseUser.ethnicity;
            if (baseUser.religion != null && !baseUser.religion.isEmpty())
                religionText.setText(baseUser.religion);
            religion = baseUser.religion;
            if (baseUser.gender.equals("male")) {

                maleButton.toggle();
            } else if (baseUser.gender.equals("female")) {
                femaleButton.callOnClick();
                femaleButton.toggle();

            }
            gender = baseUser.gender;


        }
        Location location = MainActivity.lastLocation();
        String zipPostal = null;
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
        if (zipPostal != null) {
            zipCodeField.setText(zipPostal);
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
                ethnicityText.setText(ethnicity);
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
                religionText.setText(religion);
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
        EditProfile activity = (EditProfile)this.getActivity();
        String profilePhotoUrl = activity.avatarPhoto();
        String coverPhotoUrl = activity.coverPhoto();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("type", "user");
        hashMap.put("first_name", firstNameField.getText().toString());
        hashMap.put("last_name", lastNameField.getText().toString());
        hashMap.put("email", emailField.getText().toString());
        hashMap.put("gender", gender);
        hashMap.put("info", infoField.getText().toString());
        hashMap.put("website", websiteField.getText().toString());
        hashMap.put("phone_number", phoneNumberField.getText().toString());
        hashMap.put("birthday", birthdateField.getText().toString());
        hashMap.put("zip_postal", zipCodeField.getText().toString());
        hashMap.put("profile_photo_url", profilePhotoUrl);
        hashMap.put("cover_photo_url", coverPhotoUrl);
        hashMap.put("program_code", programCodeField.getText().toString());
        hashMap.put("ethnicity", ethnicity);
        hashMap.put("religion", religion);

        HandleMessage handler = new HandleMessage(this);

        User.update(hashMap, handler);
    }

    private static class HandleMessage extends Handler {

        private Fragment mFragment = null;

        public HandleMessage(Fragment fragment) {
            mFragment = fragment;
        }

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
