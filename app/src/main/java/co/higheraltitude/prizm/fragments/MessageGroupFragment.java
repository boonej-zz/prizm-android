package co.higheraltitude.prizm.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.Iterator;

import co.higheraltitude.prizm.DirectPickerActivity;
import co.higheraltitude.prizm.DirectSelectorActivity;
import co.higheraltitude.prizm.MainActivity;
import co.higheraltitude.prizm.NewGroupActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.ReadMessagesActivity;
import co.higheraltitude.prizm.adapters.GroupAdapter;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.handlers.GroupsHandler;
import co.higheraltitude.prizm.handlers.OrganizationCountHandler;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.User;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MessageGroupFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class MessageGroupFragment extends android.support.v4.app.Fragment
        implements View.OnClickListener, AdapterView.OnItemClickListener {

    private PrizmCache mCache;
    private GroupAdapter mGroupAdapter;
    private int [] mCounts;
    private OnFragmentInteractionListener mListener;

    // Views

    private ListView mListView;
    private FloatingActionButton mFloatingActionButton;
    private ProgressBar mProgressBar;
    private View mActionOverlay;
    private View mNewGroupView;
    private View mNewGroupButton;
    private View mDirectView;
    private User mUser;
    public int ORGANIZATION_MEMBER_COUNT;

    // Data
    private ArrayList<Group> mGroups;
    private ArrayList<String> mGroupNames;



    public MessageGroupFragment() {


    }

    public void onClick(View view) {
        if (view == mFloatingActionButton) {
            toggleFloatingActionButton();
        } else if (view == mDirectView) {
            onDirectClick();
        } else if (view == mNewGroupButton) {
            onNewGroupClick();
        }
    }

    public void onDirectClick() {
        mActionOverlay.setVisibility(View.INVISIBLE);
        final OvershootInterpolator interpolator = new OvershootInterpolator();
        ViewCompat.animate(mFloatingActionButton).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        mActionOverlay.startAnimation(fadeOut);
        Intent intent = new Intent(getContext(), DirectSelectorActivity.class);
        startActivity(intent);
    }

    public void onNewGroupClick() {
        mActionOverlay.setVisibility(View.INVISIBLE);
        final OvershootInterpolator interpolator = new OvershootInterpolator();
        ViewCompat.animate(mFloatingActionButton).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
        Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        mActionOverlay.startAnimation(fadeOut);
        Intent intent = new Intent(getContext(), NewGroupActivity.class);
        startActivity(intent);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent;
        if (position == 0) {
            intent = new Intent(getContext(), DirectPickerActivity.class);

        } else {
            intent = new Intent(getContext(), ReadMessagesActivity.class);
            if (position == 1) {
                intent.putExtra(ReadMessagesActivity.EXTRA_IS_ALL, true);
                intent.putExtra(ReadMessagesActivity.EXTRA_ORG_MEMBER_COUNT, ORGANIZATION_MEMBER_COUNT);
            } else {
                Group group = mGroups.get(position - 2);
                intent.putExtra(ReadMessagesActivity.EXTRA_GROUP, group);
            }
        }
        if (intent != null) {
            intent.putExtra(ReadMessagesActivity.EXTRA_CURRENT_USER, User.getCurrentUser());
            intent.putExtra(ReadMessagesActivity.EXTRA_ORGANIZATION, User.getCurrentUser().primaryOrganization);
            startActivity(intent);
        }
    }

    private void toggleFloatingActionButton(){
        if (mActionOverlay.getVisibility() == View.INVISIBLE) {
            mActionOverlay.setVisibility(View.VISIBLE);
            mActionOverlay.setAlpha(0);
            final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
            final OvershootInterpolator oi = new OvershootInterpolator();
            ViewCompat.animate(mFloatingActionButton).rotation(135f).withLayer().setDuration(300).setInterpolator(interpolator).start();
            ViewCompat.animate(mActionOverlay).alpha(1.f).withLayer().setDuration(300).setInterpolator(oi).start();
        } else {
            mActionOverlay.setVisibility(View.INVISIBLE);
            final OvershootInterpolator interpolator = new OvershootInterpolator();
            ViewCompat.animate(mFloatingActionButton).rotation(0f).withLayer().setDuration(300).setInterpolator(interpolator).start();
            Animation fadeOut = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
            mActionOverlay.startAnimation(fadeOut);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_group, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            mUser = bundle.getParcelable("user");
        }
        configureViews(view);
        configureAdapter();
        loadGroups();
        return view;

    }

    private void configureViews(View view){
        mNewGroupView = view.findViewById(R.id.new_group_button);
        mActionOverlay = view.findViewById(R.id.action_overlay);
        mProgressBar = (ProgressBar)view.findViewById(R.id.progress_bar);
        mListView = (ListView)view.findViewById(R.id.group_list);
        mListView.setOnItemClickListener(this);
        mFloatingActionButton = (FloatingActionButton)view.findViewById(R.id.floating_action_button);
        mFloatingActionButton.setOnClickListener(this);
        mDirectView = view.findViewById(R.id.direct_button);
        mDirectView.setOnClickListener(this);
        mNewGroupButton = view.findViewById(R.id.new_group_fab);
        mNewGroupButton.setOnClickListener(this);
        if (mUser == null || mUser.getCurrentUser().role == null ||
                ( !mUser.role.equals("owner") && !mUser.role.equals("leader"))) {
            mNewGroupView.setVisibility(View.GONE);
        }
    }

    public ArrayList<Group> getGroups() {
        return mGroups;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void setGroups(ArrayList<Group> groups) {
        mGroups = groups;
    }

    public ArrayList<String> getGroupNames() {
        return mGroupNames;
    }

    public void setGroupNames(ArrayList<String> groupNames) {
        mGroupNames = groupNames;
    }

    public GroupAdapter getAdapter() {
        return mGroupAdapter;
    }

    public void showProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(true);
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public void hideProgressBar() {
        if (mProgressBar != null) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.GONE);
        }
    }


    public void loadGroups() {
        if (mUser == null || mUser.primaryOrganization == null) {
            hideProgressBar();
            return;
        }
        Group.fetchOrganizationMemberCount(mUser.primaryOrganization,
                new OrganizationCountHandler(this));
        showProgressBar();
        mGroups = Group.fetchGroupsForUser(mUser, mUser.primaryOrganization,
                new GroupsHandler(getContext(), this));
        if (mGroups == null) {
            mGroups = new ArrayList<>();
        }

        mGroupAdapter = new GroupAdapter(MainActivity.context, mGroups);

        mListView.setAdapter(mGroupAdapter);

        mGroupNames = new ArrayList<>();
        Iterator i = mGroups.iterator();
        while (i.hasNext()) {
            Group g = (Group)i.next();
            mGroupNames.add(g.name);
        }

    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

    private void configureAdapter () {
        mGroupAdapter = new GroupAdapter(getContext(), new ArrayList<Group>());
    }

}
