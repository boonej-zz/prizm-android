package co.higheraltitude.prizm.fragments;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.higheraltitude.prizm.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class InsightFragment extends Fragment {


    public InsightFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_insight, container, false);
        loadViews(view);
        return view;

    }

    private void loadViews(View view) {
        TabLayout tabLayout = (TabLayout)view.findViewById(R.id.explore_tabs);
        ViewPager viewPager = (ViewPager)view.findViewById(R.id.explore_pager);
        viewPager.setAdapter(new InsightPager(getChildFragmentManager()));
        tabLayout.setupWithViewPager(viewPager);
    }

    private class InsightPager extends FragmentPagerAdapter {
        private String[] titles = getResources().getStringArray(R.array.insight_pages);

        public InsightPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public Fragment getItem(int position) {
            Bundle args = new Bundle();
            args.putInt(InsightFeedFragment.ARGUMENT_TYPE, position);
            InsightFeedFragment insightFeedFragment = new InsightFeedFragment();
            insightFeedFragment.setArguments(args);
            return insightFeedFragment;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }

    }

}
