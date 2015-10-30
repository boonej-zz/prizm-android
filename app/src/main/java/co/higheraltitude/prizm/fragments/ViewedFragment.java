package co.higheraltitude.prizm.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.PeepView;
import co.higheraltitude.prizm.views.UserAvatarView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewedFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewedFragment extends Fragment implements AdapterView.OnItemClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER_LIST = "arg_user_list";
    private ArrayAdapter<User> mUserAdapter;


    private ArrayList<User> mUserList;

    private ListView mUserListView;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewedFragment.
     */
    public static ViewedFragment newInstance() {
        ViewedFragment fragment = new ViewedFragment();
        return fragment;
    }

    public ViewedFragment() {
        // Required empty public constructor
    }

    public void setArrayAdapter(ArrayAdapter<User> adapter) {
        mUserAdapter = adapter;
        if (mUserListView != null) {
            mUserListView.setAdapter(adapter);
            mUserAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUserList = getArguments().getParcelableArrayList(ARG_USER_LIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_viewed, container, false);
        mUserListView = (ListView)v.findViewById(R.id.user_list);
        mUserListView.setOnItemClickListener(this);
        if (mUserAdapter != null) {
            mUserListView.setAdapter(mUserAdapter);
            mUserAdapter.notifyDataSetChanged();
        }
        return v;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        User u = ((UserAvatarView)view).getUser();
        Intent intent = new Intent(getContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, u);
        startActivity(intent);
    }


}
