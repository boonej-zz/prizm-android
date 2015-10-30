package co.higheraltitude.prizm.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

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
 * {@link NewPartnerFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NewPartnerFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewPartnerFragment extends Fragment {

    private View typeButton;
    private TextView typeText;
    private Button saveButton;

    private EditText organizationField;
    private EditText emailField;
    private EditText passwordField;
    private EditText confirmField;
    private EditText zipCodeField;
    private EditText websiteField;
    private EditText contactFirstField;
    private EditText contactLastField;
    private EditText contactEmailField;
    private String type = "";
    private String city = "";
    private String state = "";
    private View passwordWrapper;
    private View confirmWrapper;
    private Bundle baseUser;



    // TODO: Rename and change types and number of parameters
    public static NewPartnerFragment newInstance() {
        NewPartnerFragment fragment = new NewPartnerFragment();

        return fragment;
    }

    public NewPartnerFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mPage = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_partner, container, false);
        typeButton = view.findViewById(R.id.registration_button_type);
        typeText = (TextView)view.findViewById(R.id.registration_type_text);
        saveButton = (Button)view.findViewById(R.id.registration_button_save);
        organizationField = (EditText)view.findViewById(R.id.registration_field_first);
        emailField = (EditText)view.findViewById(R.id.registration_field_email);
        passwordField = (EditText)view.findViewById(R.id.registration_field_password);
        confirmField = (EditText)view.findViewById(R.id.registration_field_confirm);
        contactFirstField = (EditText)view.findViewById(R.id.registration_field_contact_first);
        contactLastField = (EditText)view.findViewById(R.id.registration_field_contact_last);
        contactEmailField = (EditText)view.findViewById(R.id.registration_field_contact_email);
        websiteField = (EditText)view.findViewById(R.id.registration_field_website);
        zipCodeField = (EditText)view.findViewById(R.id.registration_field_zipcode);
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
        typeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTypeClick();
            }
        });
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave(v);
            }
        });
        passwordWrapper = view.findViewById(R.id.registration_password_wrapper);
        confirmWrapper = view.findViewById(R.id.registration_confirm_wrapper);

        if (baseUser != null) {
            if (baseUser.getString("email") != null) {
                emailField.setText(baseUser.getString("email"));
            }

            passwordWrapper.setVisibility(View.GONE);
            confirmWrapper.setVisibility(View.GONE);
        }

        return view;
    }

    private void onTypeClick() {
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.selector_dialog);
        OptionAdapter optionAdapter = new OptionAdapter(getContext(),
                Arrays.asList(getResources().getStringArray(R.array.values_type)));
        ListView listView = (ListView) dialog.findViewById(R.id.listview_dialog);
        listView.setAdapter(optionAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                type = (String)parent.getAdapter().getItem(position);
                typeText.setText(type);
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
        String profilePhotoUrl = CreateAccountActivity.profilePhotoUrl;
        String coverPhotoUrl = CreateAccountActivity.coverPhotoUrl;

        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("type", "user");
        hashMap.put("first_name", organizationField.getText().toString());

        hashMap.put("email", emailField.getText().toString());
        hashMap.put("password", passwordField.getText().toString());
        hashMap.put("confirm_password", passwordField.getText().toString());
        hashMap.put("type", "institution");
        hashMap.put("subtype", type);
        hashMap.put("zip_postal", zipCodeField.getText().toString());
        hashMap.put("website", websiteField.getText().toString());
        hashMap.put("city", city);
        hashMap.put("state", state);
        hashMap.put("profile_photo_url", profilePhotoUrl);
        hashMap.put("cover_photo_url", coverPhotoUrl);
        hashMap.put("contact_first", contactFirstField.getText().toString());
        hashMap.put("contact_last", contactLastField.getText().toString());
        hashMap.put("contact_email", contactEmailField.getText().toString());
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
