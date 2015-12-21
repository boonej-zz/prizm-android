package co.higheraltitude.prizm.fragments;


import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.TabWidget;

import java.util.ArrayList;
import java.util.List;

import co.higheraltitude.prizm.R;
import co.higheraltitude.prizm.cache.PrizmDiskCache;
import co.higheraltitude.prizm.models.Post;
import co.higheraltitude.prizm.views.SingleGridImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    public ExploreFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        loadViews(view);
        return view;
    }

    private void loadViews(View view) {
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.explore_tabs);
        ViewPager viewPager = (ViewPager)view.findViewById(R.id.explore_pager);
        viewPager.setAdapter(new ExplorePager(getActivity().getSupportFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }


    public class ExplorePager extends FragmentPagerAdapter {
        final int PAGE_COUNT = 3;
        private String[] titles = new String[] {getString(R.string.explore_latest),
                getString(R.string.explore_popular), getString(R.string.explore_featured)};

        public ExplorePager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            Bundle args = new Bundle();
            args.putInt(ExploreFeedFragment.ARGUMENT_TYPE, position);
            ExploreFeedFragment exploreFeedFragment = new ExploreFeedFragment();
            exploreFeedFragment.setArguments(args);
            return exploreFeedFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }



}
