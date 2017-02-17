package com.project.meetu.arcityguide;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import java.util.List;
import java.util.Vector;

/**
 * Created by Meetu on 17-02-2017.
 */

public class NavigationActivity extends FragmentActivity {


    private NavFragmentAdapter N1;
    ViewPager mViewPager;
    public static String passedOn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        Intent I1=getIntent();
        passedOn=I1.getStringExtra(InputActivity.EXTRA_MESSAGE);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        N1=new NavFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(N1);
        mViewPager.setCurrentItem(0);


    }
}