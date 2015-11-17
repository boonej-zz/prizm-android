package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.ActivitiesFragment;
import co.higheraltitude.prizm.fragments.TrustsFragment;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Activity;
import co.higheraltitude.prizm.models.Trust;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.ActivityView;
import co.higheraltitude.prizm.views.TrustRequestView;
import co.higheraltitude.prizm.views.UserAvatarView;

public class NotificationFeedActivity extends AppCompatActivity
        implements TrustRequestView.TrustRequestDelegate, ActivityView.ActivityViewDelegate {

    private TabLayout mTabLayout;
    private ViewPager mViewPager;

    private ActivityFeedAdapter mActivityAdapter;
    private TrustFeedAdapter mTrustAdapter;


    private boolean isUpdating = false;
    private boolean isUpdatingTrusts = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_feed);
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new BackClickListener(this));
        actionBar.hideOverflowMenu();
        actionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment f = getSupportFragmentManager().findFragmentByTag("android:switcher:" +
                        R.id.activity_pager + ":" + mViewPager.getCurrentItem());
                if (f instanceof ActivitiesFragment) {
                    ((ActivitiesFragment)f).scrollToTop();
                } else if (f instanceof TrustsFragment) {
                    ((TrustsFragment)f).scrollToTop();
                }
            }
        });
        configureViews();
        fetchActivities(false);
        fetchTrusts(false);
    }



    private void configureViews()
    {
        mTabLayout = (TabLayout)findViewById(R.id.activity_tabs);
        mViewPager = (ViewPager)findViewById(R.id.activity_pager);
        mViewPager.setAdapter(new NotificationsPager(getSupportFragmentManager()));
        mTabLayout.setupWithViewPager(mViewPager);
        mActivityAdapter = new ActivityFeedAdapter(getApplicationContext(), new ArrayList<Activity>());
        mTrustAdapter = new TrustFeedAdapter(getApplicationContext(), new ArrayList<Trust>());
    }

    public void fetchActivities(boolean updateOnly) {
        if (!isUpdating) {
            isUpdating = true;
            Activity.fetchActivities("", new ActivityFeedDelegate(updateOnly));
        }
    }

    public void fetchTrusts(boolean updateOnly) {
        if (!isUpdatingTrusts) {
            isUpdatingTrusts = true;
            Trust.fetchTrustActivity("", new TrustFeedDelegate(updateOnly));
        }
    }

    public void fetchOlderTrusts() {
        if (!isUpdatingTrusts) {
            isUpdatingTrusts = true;
            String last = "";
            if (mTrustAdapter.getCount() > 0) {
                last = mTrustAdapter.getItem(mActivityAdapter.getCount() - 1).modifyDate;
            }
            Trust.fetchTrustActivity(last, new TrustFeedDelegate(false));

        }
    }

    public void fetchOlderActivities(){
        if (!isUpdating) {
            isUpdating = true;
            String last = "";
            if (mActivityAdapter.getCount() > 0) {
                last = mActivityAdapter.getItem(mActivityAdapter.getCount() - 1).createDate;
            }
            Activity.fetchActivities(last, new ActivityFeedDelegate(false));
        }
    }

    public class NotificationsPager extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String[] titles = new String[] {getString(R.string.notifications_activities),
                getString(R.string.notifications_requests)};

        public NotificationsPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                ActivitiesFragment f = ActivitiesFragment.newInstance();
                f.setArrayAdapter(mActivityAdapter);
                return f;
            } else {
                TrustsFragment f = TrustsFragment.newInstance();
                f.setArrayAdapter(mTrustAdapter);
                return f;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    private class ActivityFeedAdapter extends ArrayAdapter<Activity>
    {

        public ActivityFeedAdapter(Context c, List<Activity> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ActivityView view = (ActivityView)convertView;
            if (convertView == null) {
                view = ActivityView.inflate(parent);
            }
            view.setActivity(getItem(position));
            view.setDelegate(NotificationFeedActivity.this);

            return view;
        }
    }

    private class TrustFeedAdapter extends ArrayAdapter<Trust>
    {

        public TrustFeedAdapter(Context c, List<Trust> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TrustRequestView view = (TrustRequestView)convertView;
            if (convertView == null) {
                view = TrustRequestView.inflate(parent);
            }
            view.setTrust(getItem(position));
            view.setDelegate(NotificationFeedActivity.this);

            return view;
        }
    }

    private class ActivityFeedDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private boolean mUpdateOnly;

        public ActivityFeedDelegate(boolean updateOnly) {
            mUpdateOnly = updateOnly;
        }

        @Override
        public void cached(String path, Object object) {
            if (!mUpdateOnly) {
                process(object);
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            update(object);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<Activity> activities = (ArrayList<Activity>)object;

                mActivityAdapter.addAll(activities);
            }
        }

        private void update(Object object) {
            if (object instanceof ArrayList) {
                int count = mActivityAdapter.getCount();
                ArrayList<Activity> activities = (ArrayList<Activity>)object;
                for (Activity a : activities) {
                    boolean found = false;
                    for (int i = 0; i != count; ++i) {
                        Activity t = mActivityAdapter.getItem(i);
                        if (t.uniqueId.equals(a.uniqueId)) {
                            mActivityAdapter.remove(t);
                            mActivityAdapter.insert(a, i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mActivityAdapter.add(a);
                    }
                }
            }
            isUpdating = false;
//            hideProgressBar();
//            cacheLoaded = false;

        }
    }

    private class TrustFeedDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        private boolean mUpdateOnly;

        public TrustFeedDelegate(boolean updateOnly) {
            mUpdateOnly = updateOnly;
        }

        @Override
        public void cached(String path, Object object) {
            if (!mUpdateOnly) {
                process(object);
            }
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            update(object);
        }

        private void process(Object object) {
            if (object instanceof ArrayList) {
                ArrayList<Trust> activities = (ArrayList<Trust>)object;

                mTrustAdapter.addAll(activities);
            }
        }

        private void update(Object object) {
            if (object instanceof ArrayList) {
                int count = mTrustAdapter.getCount();
                ArrayList<Trust> activities = (ArrayList<Trust>)object;
                for (Trust a : activities) {
                    boolean found = false;
                    for (int i = 0; i != count; ++i) {
                        Trust t = mTrustAdapter.getItem(i);
                        if (t.uniqueId.equals(a.uniqueId)) {
                            mTrustAdapter.remove(t);
                            mTrustAdapter.insert(a, i);
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        mTrustAdapter.add(a);
                    }
                }
            }
            isUpdatingTrusts = false;
//            hideProgressBar();
//            cacheLoaded = false;

        }
    }

    @Override
    public void avatarButtonClicked(Trust trust) {
        User u = new User();
        u.uniqueID = trust.fromId;
        u.subtype = trust.fromSubtype;
        u.type = trust.fromType;
        u.profilePhotoURL = trust.fromProfilePhotoUrl;
        u.name = trust.fromName;
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, u);
        startActivity(intent);
    }

    @Override
    public void avatarPhotoClicked(Activity activity) {
        User u = new User();
        u.uniqueID = activity.fromId;
        u.subtype = activity.fromSubtype;
        u.type = activity.fromType;
        u.profilePhotoURL = activity.fromProfilePhotoUrl;
        u.name = activity.fromName;
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        intent.putExtra(LoginActivity.EXTRA_PROFILE, u);
        startActivity(intent);
    }
}
