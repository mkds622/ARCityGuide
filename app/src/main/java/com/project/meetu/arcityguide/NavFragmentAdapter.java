package com.project.meetu.arcityguide;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

/**
 * Created by Meetu on 17-02-2017.
 */

public class NavFragmentAdapter extends FragmentPagerAdapter {

    public static int pos = 0;

    private static int NUM_ITEMS = 2;

    private List<Fragment> myFragments;

    public NavFragmentAdapter(FragmentManager fm) {
        super(fm);

    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                return ARFragment.newInstance();
            case 1: // Fragment # 0 - This will show FirstFragment different title
                return MapFragment1.newInstance();
            default:
                return null;
        }

    }

    @Override
    public int getCount() {

        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        setPos(position);

        String PageTitle = "";

        switch(pos)
        {
            case 0:
                PageTitle = "AR Mode";
                break;
            case 1:
                PageTitle = "Map Mode";
                break;
            }
        return PageTitle;
    }

    public static int getPos() {
        return pos;
    }

    public static void setPos(int pos) {
        NavFragmentAdapter.pos = pos;
    }
}
