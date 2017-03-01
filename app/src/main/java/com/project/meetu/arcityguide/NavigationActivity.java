package com.project.meetu.arcityguide;

import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

import java.util.List;
import java.util.Vector;

/**
 * Created by Meetu on 17-02-2017.
 */

public class NavigationActivity extends FragmentActivity{//implements GoogleApiClient.ConnectionCallbacks ,LocationListener, GoogleApiClient.OnConnectionFailedListener {


    private NavFragmentAdapter N1;
    ViewPager mViewPager;
    public static String passedOn;
    //for location
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng Location;

    protected LocationRequest mLocationRequest;
    private static final String LOGTAG="Navigation Activity";
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
       /* if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }*/

    }
/*
    @Override
    protected void onStart(){
        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onConnected(Bundle connectionHint) {
        if ( ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },1        );
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        mLastLocation =LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Location=new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        LatLng newLoc = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(newLoc, 16);
        //mMap.animateCamera(update);

        Toast.makeText(this, newLoc.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }*/
}