package co.higheraltitude.prizm;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.delegates.UserDelegate;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.Interest;
import co.higheraltitude.prizm.models.User;
import co.higheraltitude.prizm.views.InterestView;

public class InterestsActivity extends AppCompatActivity
        implements InterestView.InterestViewDelegate, View.OnClickListener {


    private InterestAdapter mAdapter;
    private ArrayList<Interest> mInterests;
    private ListView mListView;
    private ImageButton mDoneButton;
    private ProgressBar mProgressBar;
    private int mInterestCount;

    private boolean mStop = false;
    private boolean mForced = false;

    public static final String EXTRA_FORCED = "extra_forced";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_interests);

        Intent intent = getIntent();
        mForced = intent.getBooleanExtra(EXTRA_FORCED, false);

        Toolbar actionBar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(actionBar);
        if (!mForced) {
            actionBar.setNavigationIcon(R.drawable.backarrow_icon);
            actionBar.setNavigationOnClickListener(new BackClickListener(this));
        }
        actionBar.hideOverflowMenu();
        ArrayList<Interest> baseList = new ArrayList<>();
        mAdapter = new InterestAdapter(getApplicationContext(), baseList);
        mListView = (ListView)findViewById(R.id.interests_list);
        mListView.setAdapter(mAdapter);
        mDoneButton = (ImageButton)findViewById(R.id.action_done_button);
        mDoneButton.setOnClickListener(this);
        mProgressBar = (ProgressBar)findViewById(R.id.progress_bar);
        mInterestCount = User.getCurrentUser().interestCount;
        if (mInterestCount < 3) {
            mDoneButton.setVisibility(View.GONE);
        } else {
            mDoneButton.setVisibility(View.VISIBLE);
        }
        showProgress();
        loadInterests();
    }

    private void showProgress() {
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setIndeterminate(false);
        mProgressBar.setVisibility(View.GONE);
    }

    private void loadInterests() {
        Interest.fetchInterests(new InterestsDelegate());
    }

    private void sortList() {
        Collections.sort(mInterests, new InterestComparator());
    }

    private class InterestComparator implements Comparator<Interest> {
        @Override
        public int compare(Interest left, Interest right) {
            return -left.selected.compareTo(right.selected);
        }
    }

    @Override
    public void onClick(View view) {
        ArrayList<Interest> selected = new ArrayList<>();
        for (Interest interest : mInterests) {
            if (interest.selected) selected.add(interest);
        }
        if (selected.size() < 3) {
            Toast.makeText(getApplicationContext(), "Uh oh! You must select at least 3 interests.",
                    Toast.LENGTH_SHORT).show();
        } else {
            showProgress();
            Interest.putInterests(selected, new PutHandler(this));
        }
    }

    @Override
    public void interestClicked(Interest interest) {
        int position = mAdapter.getPosition(interest) + 1;
        ArrayList<Interest> subinterests = new ArrayList<>();
        for (Interest i : mInterests) {
            if (interest.subinterests != null && interest.subinterests instanceof ArrayList) {
                for (String uniqueId : interest.subinterests) {
                    if (i.uniqueId.equals(uniqueId) && !i.selected) {
                        subinterests.add(i);
                    }
                }
            }
        }
        if (interest.selected) {
            mInterestCount += 1;
        } else {
            mInterestCount -= 1;
        }
        if (mInterestCount < 3) {
            mDoneButton.setVisibility(View.GONE);
        } else {
            mDoneButton.setVisibility(View.VISIBLE);
        }
        if (subinterests.size() > 0) {
            for (int i = 0; i != subinterests.size(); ++i) {
                Interest m = subinterests.get(i);
                if (interest.selected) {
                    mAdapter.insert(m, position + i);
                } else {
                    mAdapter.remove(m);
                }
            }

            mAdapter.notifyDataSetChanged();
        }
    }

    private class InterestAdapter extends ArrayAdapter<Interest> {

        public InterestAdapter(Context c, ArrayList<Interest> items) {
            super(c, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ArrayList<Interest> items = new ArrayList<>();
            int start = position * 3;
            int stop = start + 3;
            if (stop > super.getCount()) {
                stop = super.getCount();
            }
            for (int i = start; i != stop; ++i) {
                items.add(getItem(i));
            }
            InterestView itemView = (InterestView)convertView;
            if (itemView == null) {
                itemView = InterestView.inflate(parent);
            }
            itemView.setInterests(items);
            itemView.setDelegate(InterestsActivity.this);


            return itemView;
        }

        @Override
        public int getCount() {
            return super.getCount()/3;
        }
    }

    private class InterestsDelegate implements PrizmDiskCache.CacheRequestDelegate {
        @Override
        public void cached(String path, Object object) {
            mStop = false;
//            process(object);
        }

        @Override
        public void cacheUpdated(String path, Object object) {
//            mStop = true;
            process(object);
        }

        private void process(Object object) {

            if (object instanceof ArrayList) {
                mInterests = (ArrayList<Interest>)object;
                sortList();
                Iterator<Interest> iterator = mInterests.iterator();
                mStop = false;
                mAdapter.clear();
                while (iterator.hasNext()) {
                    Interest i = iterator.next();
                    if (i.selected || !i.isSubinterest) {
                        mAdapter.add(i);
                    }
                }
                mAdapter.notifyDataSetChanged();
                hideProgress();
            }
        }
    }

    private static class PutHandler extends Handler {

        private InterestsActivity mActivity;

        public PutHandler(InterestsActivity activity) {
            mActivity = activity;
        }

        public void handleMessage(Message message) {
            User.fetchUserCore(User.getCurrentUser(), mActivity.fetchDelegate());
        }
    }

    protected FetchDelegate fetchDelegate() {
        return new FetchDelegate();
    }

    private class FetchDelegate implements PrizmDiskCache.CacheRequestDelegate {
        public void cached(String path, Object obj) {

        }

        public void cacheUpdated(String path, Object obj){
            finish();
        }
    }
}
