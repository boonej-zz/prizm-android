package co.higheraltitude.prizm.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import co.higheraltitude.prizm.FullBleedInsight;
import co.higheraltitude.prizm.FullBleedPostActivity;
import co.higheraltitude.prizm.LoginActivity;
import co.higheraltitude.prizm.NotificationFeedActivity;
import co.higheraltitude.prizm.ProfileActivity;
import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.ReadMessagesActivity;
import co.higheraltitude.prizm.models.Activity;
import co.higheraltitude.prizm.models.Group;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.UserAvatarView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ActivitiesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActivitiesFragment extends Fragment implements AdapterView.OnItemClickListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_USER_LIST = "arg_user_list";
    private ArrayAdapter<Activity> mActivityAdapter;


    private ArrayList<Activity> mActivityList;

    private ListView mListView;

    private int lastVisibleItem = 0;
    private boolean scrollingDown = false;
    private boolean isUpdating = false;
    private boolean shouldSlide = false;
    private boolean cacheLoaded = false;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ViewedFragment.
     */
    public static ActivitiesFragment newInstance() {
        ActivitiesFragment fragment = new ActivitiesFragment();
        return fragment;
    }

    public ActivitiesFragment() {
        // Required empty public constructor
    }

    public void setArrayAdapter(ArrayAdapter<Activity> adapter) {
        mActivityAdapter = adapter;
        if (mListView != null) {
            mListView.setAdapter(mActivityAdapter);

        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void scrollToTop() {
        mListView.smoothScrollToPositionFromTop(0, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_activities, container, false);
        mListView = (ListView)v.findViewById(R.id.activity_list);
        mListView.setOnItemClickListener(this);
        if (mActivityAdapter != null) {
            mListView.setAdapter(mActivityAdapter);
            mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    int firstPosition = view.getFirstVisiblePosition();
                    if (lastVisibleItem > firstPosition) {
                        shouldSlide = false;
                        scrollingDown = false;
                    } else if (lastVisibleItem < firstPosition) {
                        shouldSlide = true;
                        scrollingDown = true;
                    } else if (listIsAtTop()) {
                        NotificationFeedActivity mActivity = (NotificationFeedActivity)getActivity();
                        mActivity.fetchActivities(true);

                    } else {
                        if (view.getLastVisiblePosition() >= view.getCount() - 5) {
                            NotificationFeedActivity mActivity = (NotificationFeedActivity)getActivity();
                            mActivity.fetchOlderActivities();
                        }
                    }
                    lastVisibleItem = firstPosition;
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });
        }
        return v;

    }

    private boolean listIsAtTop()   {
        if (mListView != null) {
            if (mListView.getChildCount() == 0) return true;
            return mListView.getChildAt(0).getTop() == 0;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Activity activity = mActivityAdapter.getItem(position);
        Intent intent = null;
        if (activity.postId != null) {
            intent = new Intent(getContext(), FullBleedPostActivity.class);
            intent.putExtra(FullBleedPostActivity.EXTRA_POST_ID, activity.postId);
        }
        if (activity.messageId != null) {
            intent = new Intent(getContext(), ReadMessagesActivity.class);

            intent.putExtra(ReadMessagesActivity.EXTRA_ORGANIZATION, activity.organizationId);


            Group group = new Group();
            if (activity.groupId == null) {
                if (activity.targetId != null) {
                    User target = new User();
                    target.uniqueID = activity.targetId;
                    target.name = activity.targetName;
                    intent.putExtra(ReadMessagesActivity.EXTRA_DIRECT_USER, target);
                    intent.putExtra(ReadMessagesActivity.EXTRA_IS_DIRECT, true);
                } else {
                    intent.putExtra(ReadMessagesActivity.EXTRA_IS_ALL, true);
                }

            } else {
                group.uniqueID = activity.groupId;
                group.name = activity.groupName;
                intent.putExtra(ReadMessagesActivity.EXTRA_GROUP, group);
            }

        }
        if (activity.insightId != null) {
            intent = new Intent(getContext(), FullBleedInsight.class);
            intent.putExtra(FullBleedInsight.EXTRA_INSIGHT_ID, activity.insightId);
        }
        if (intent != null) {
            getActivity().startActivity(intent);
        }
    }


}
