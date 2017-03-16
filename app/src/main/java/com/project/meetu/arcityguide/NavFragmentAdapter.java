package com.project.meetu.arcityguide;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Meetu on 17-02-2017.
 */

public class NavFragmentAdapter extends FragmentPagerAdapter{

    public static int pos = 0;

    private static int NUM_ITEMS = 2;

    public List<Fragment> myFragments;
    public NavFragmentAdapter(FragmentManager fm) {
        super(fm);
        myFragments=new ArrayList<Fragment>();
        myFragments.add(ARFragment.newInstance());
        myFragments.add(MapFragment1.newInstance());
    }

    /*SparseArray<Fragment> registeredFragments = new SparseArray<>();


    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }


    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }*/
    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0: // Fragment # 0 - This will show FirstFragment
                    pos=0;
                return myFragments.get(0);
            case 1: // Fragment # 0 - This will show FirstFragment different title
                    pos=1;
                return myFragments.get(1);
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
