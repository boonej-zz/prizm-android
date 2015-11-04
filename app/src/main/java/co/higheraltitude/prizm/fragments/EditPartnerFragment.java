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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

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
 * {@link EditPartnerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EditPartnerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditPartnerFragment extends Fragment {
    // TODO: Rename and change types and number of parameters


    private Button saveButton;

    private EditText firstNameField;
    private EditText mascotField;
    private EditText populationField;

    private EditText emailField;
    private EditText infoField;

    private EditText phoneNumberField;
    private EditText dateFoundedField;
    private EditText contactFirstField;
    private EditText contactLastField;
    private EditText contactEmailField;

    private User baseUser;
    private EditText zipCodeField;

    private String city = "";
    private String state = "";

    private EditText websiteField;

    public static EditPartnerFragment newInstance(User user) {
        EditPartnerFragment fragment = new EditPartnerFragment();
        fragment.setBaseUser(user);

        return fragment;
    }

    public EditPartnerFragment() {
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
        View view = inflater.inflate(R.layout.fragment_edit_partner, container, false);

        saveButton = (Button)view.findViewById(R.id.registration_button_save);
        saveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                onSave(v);
            }
        });


        dateFoundedField = (EditText)view.findViewById(R.id.registration_field_date_founded);
        mascotField = (EditText)view.findViewById(R.id.registration_field_mascot);

        firstNameField = (EditText)view.findViewById(R.id.registration_field_first);
        populationField = (EditText)view.findViewById(R.id.registration_field_population);
        emailField = (EditText)view.findViewById(R.id.registration_field_email);
        infoField = (EditText)view.findViewById(R.id.registration_field_info);

        phoneNumberField = (EditText)view.findViewById(R.id.registration_field_phone);
        contactFirstField = (EditText)view.findViewById(R.id.registration_field_contact_first);
        contactLastField = (EditText)view.findViewById(R.id.registration_field_contact_last);
        contactEmailField = (EditText)view.findViewById(R.id.registration_field_contact_email);
        zipCodeField = (EditText)view.findViewById(R.id.registration_field_zipcode);
        websiteField = (EditText)view.findViewById(R.id.registration_field_website);

        if (baseUser != null) {

            emailField.setText(baseUser.email);
            firstNameField.setText(baseUser.firstName);
            zipCodeField.setText(baseUser.zipPostal);
            infoField.setText(baseUser.info);
            websiteField.setText(baseUser.website);

            phoneNumberField.setText(baseUser.phoneNumber);
            dateFoundedField.setText(baseUser.dateFounded);
            mascotField.setText(baseUser.mascot);
            populationField.setText(String.valueOf(baseUser.population));
            contactFirstField.setText(baseUser.contactFirstName);
            contactLastField.setText(baseUser.contactLastName);
            contactEmailField.setText(baseUser.contactEmail);



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



    public void onSave(View view) {
        EditProfile activity = (EditProfile)this.getActivity();
        String profilePhotoUrl = activity.avatarPhoto();
        String coverPhotoUrl = activity.coverPhoto();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("first_name", firstNameField.getText().toString());

        hashMap.put("email", emailField.getText().toString());

        hashMap.put("info", infoField.getText().toString());
        hashMap.put("website", websiteField.getText().toString());
        hashMap.put("phone_number", phoneNumberField.getText().toString());

        hashMap.put("zip_postal", zipCodeField.getText().toString());
        hashMap.put("profile_photo_url", profilePhotoUrl);
        hashMap.put("cover_photo_url", coverPhotoUrl);
        hashMap.put("mascot", mascotField.getText().toString());
        hashMap.put("enrollment", populationField.getText().toString());
        hashMap.put("contact_first", contactFirstField.getText().toString());
        hashMap.put("contact_last", contactLastField.getText().toString());
        hashMap.put("contact_email", contactEmailField.getText().toString());
        hashMap.put("date_founded", dateFoundedField.getText().toString());

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
