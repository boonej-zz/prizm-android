package co.higheraltitude.prizm.fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONObject;

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
import co.higheraltitude.prizm.models.Peep;
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
    private JSONArray messageCounts;
    private static int[] countArray;

    // Data
    private static ArrayList<Group> mGroups;
    private static ArrayList<String> mGroupNames;



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
                Group group = mGroupAdapter.getItem(position - 2);
                intent.putExtra(ReadMessagesActivity.EXTRA_GROUP, group);
            }
        }
        if (intent != null) {
            intent.putExtra(ReadMessagesActivity.EXTRA_CURRENT_USER, User.getCurrentUser());
            intent.putExtra(ReadMessagesActivity.EXTRA_ORGANIZATION, User.getCurrentUser().primaryOrganization);
            startActivity(intent);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGroups();
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

    public static ArrayList<String> getGroupNames() {
        return mGroupNames;
    }

    public static void setGroupNames(ArrayList<String> groupNames) {
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

    public void scrollToTop() {
        mListView.smoothScrollToPositionFromTop(0, 0);
    }


    public void loadGroups() {
        if (mUser == null || mUser.primaryOrganization == null) {
            hideProgressBar();
            return;
        }
        Group.fetchOrganizationMemberCount(mUser.primaryOrganization,
                new OrganizationCountHandler(this));
        showProgressBar();

        if (mGroupAdapter == null) {
            mGroupAdapter = new GroupAdapter(MainActivity.context, new ArrayList<Group>());

        }
        mListView.setAdapter(mGroupAdapter);
        Group.fetchGroupsForUser(mUser, mUser.primaryOrganization,
                new GroupsHandler(getContext(), this));



        mGroupNames = new ArrayList<>();
//        if (mGroupAdapter.getCount() > 0) {
//            for (int i = 0; i != mGroupAdapter.getCount(); ++i) {
//                Group g = mGroupAdapter.getItem(i);
//                mGroupNames.add(g.name);
//            }
//        }
        Peep.getCounts(new GroupCountsHandler(this));


    }


    private void processCounts() {
        countArray = new int[mGroupAdapter.getCount()];
        for (int i = 0; i!= countArray.length; ++i) {
            countArray[i] = 0;
        }
        if (messageCounts != null) {
            for (int i = 0; i != messageCounts.length(); ++i) {
                try {
                    JSONObject object = messageCounts.getJSONObject(i);
                    int total = object.getInt("total");
                    JSONObject j = object.getJSONObject("_id");
                    String org = null;
                    String groupId = null;
                    String target = null;
                    if (j.has("organization")) {
                        org = j.getString("organization");
                    }
                    if (j.has("group")) {
                        groupId = j.getString("group");
                    }
                    if (j.has("target")) {
                        target = j.getString("target");
                    }

                    if (org != null && !org.equals("null")) {
                        if (groupId != null && !groupId.equals("null")) {
                            int index = mGroupAdapter.indexOf(groupId);
                            if (index != -1) {
                                countArray[i + 2] = total;
                            }
                        } else if (target == null){
                            countArray[1] = total;
                        } else {
                            countArray[0] = total;
                        }
                    }
                    mGroupAdapter.setCounts(countArray);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private static class GroupCountsHandler extends Handler {
        private MessageGroupFragment mActivity;

        public GroupCountsHandler(MessageGroupFragment activity) {
            mActivity = activity;
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj != null) {
                if (msg.obj instanceof JSONArray) {
                    JSONArray array = (JSONArray)msg.obj;
                    mActivity.messageCounts = array;
                    mActivity.processCounts();
                    Log.d("DEBUG", "Fetched Counts");

                }
            }
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
