package co.higheraltitude.prizm;

import android.app.Activity;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import java.util.ArrayList;

import co.higheraltitude.prizm.fragments.SearchResultsFragment;
import co.higheraltitude.prizm.listeners.BackClickListener;
import co.higheraltitude.prizm.models.User;

public class SearchActivity extends AppCompatActivity {

    private TabLayout mTabs;
    private ViewPager mPager;
    private EditText mSearchBar;
    private Activity mParentActivity;
    private String mSearchText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(User.getTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mTabs = (TabLayout)findViewById(R.id.tabs);
        mPager = (ViewPager)findViewById(R.id.pager);

        mPager.setAdapter(new SearchPager(getSupportFragmentManager()));
        mTabs.setupWithViewPager(mPager);
        mSearchBar = (EditText)findViewById(R.id.search_bar);
        Toolbar toolbar = (Toolbar)findViewById(R.id.profile_nav_bar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.backarrow_icon);
        toolbar.setNavigationOnClickListener(new BackClickListener(this));
        configureSearchBar();
    }

    private void configureSearchBar() {
        mSearchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int position = mTabs.getSelectedTabPosition();
                mSearchText = s.toString();
                SearchResultsFragment fragment = (SearchResultsFragment) ((FragmentPagerAdapter)
                        mPager.getAdapter()).getItem(position);
                fragment.searchText(mSearchText);

            }
        });
        mSearchBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (mSearchBar.getRight() -
                            mSearchBar.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width() - 60)) {
                        mSearchText = "";
                        mSearchBar.setText(mSearchText);

                        return true;
                    }
                }
                return false;
            }
        });

    }

    private class SearchPager extends FragmentPagerAdapter {
        final int PAGE_COUNT = 2;
        private String[] titles = getResources().getStringArray(R.array.search_types);
        private ArrayList<SearchResultsFragment> mFragments = new ArrayList<>();

        public SearchPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            SearchResultsFragment fragment;
            if (mFragments.size() > position) {
                fragment = mFragments.get(position);
            } else {
                Bundle args = new Bundle();
                args.putInt(SearchResultsFragment.ARGUMENT_TYPE, position);
                fragment = new SearchResultsFragment();
                fragment.setArguments(args);
                mFragments.add(fragment);
            }

            return fragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }
}
