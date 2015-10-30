package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Iterator;

import co.higheraltitude.prizm.adapters.UserAdapter;
import co.higheraltitude.prizm.cache.PrizmCache;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.fragments.ViewedFragment;
import co.higheraltitude.prizm.models.Peep;
import co.higheraltitude.prizm.models.User;

public class MessageReaders extends AppCompatActivity {

    public static String EXTRA_MESSAGE = "co.higheraltitude.prizm.EXTRA_MESSAGE";

    private PrizmCache mCache;
    private Peep mPeep;
    private ArrayList<User> mAllReaders;
    private ArrayList<User> mRead;
    private ArrayList<User> mUnread;
    private UserAdapter mReadAdapter;
    private UserAdapter mUnreadAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCache = PrizmCache.getInstance();
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_readers);
        Intent intent = getIntent();
        mPeep = intent.getParcelableExtra(EXTRA_MESSAGE);
        configureToolbar();
        loadData();
        configureViews();

    }

    private void configureToolbar() {
        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        actionBar.setNavigationIcon(R.drawable.backarrow_icon);
        actionBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void configureViews() {
        TabLayout tabLayout = (TabLayout)findViewById(R.id.viewers_tabs);
        ViewPager viewPager = (ViewPager)findViewById(R.id.viewers_pager);
        viewPager.setAdapter(new ReadersPager(getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    private void loadData() {
        mPeep.fetchRead(new ViewedDelegate());
        mRead = new ArrayList<>();
        mUnread = new ArrayList<>();
        mReadAdapter = new UserAdapter(getApplicationContext(), mRead);
        mUnreadAdapter = new UserAdapter(getApplicationContext(), mUnread);

    }

    public class ReadersPager extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String[] titles = new String[] {getString(R.string.viewers_viewed),
                getString(R.string.viewers_not_viewed)};

        public ReadersPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            ViewedFragment vf = ViewedFragment.newInstance();
            ArrayAdapter<User> adapter = position == 0?mReadAdapter:mUnreadAdapter;
            vf.setArrayAdapter(adapter);
            return vf;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

    private class ViewedDelegate implements PrizmDiskCache.CacheRequestDelegate
    {
        @Override
        public void cached(String path, Object object) {
            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
            process(object);
        }

        private void process(Object object) {
            if (object != null) {
                mAllReaders = (ArrayList<User>) object;
                Iterator<User> iterator = mAllReaders.iterator();
                mUnread = new ArrayList<>();
                mRead = new ArrayList<>();
                while (iterator.hasNext()) {
                    User u = iterator.next();
                    if (u.read) {
                        mRead.add(u);
                    } else {
                        mUnread.add(u);
                    }
                }
                mReadAdapter.clear();
                mReadAdapter.addAll(mRead);
                mUnreadAdapter.clear();
                mUnreadAdapter.addAll(mUnread);
                mUnreadAdapter.notifyDataSetInvalidated();
                mReadAdapter.notifyDataSetInvalidated();
            }
        }
    }

}
