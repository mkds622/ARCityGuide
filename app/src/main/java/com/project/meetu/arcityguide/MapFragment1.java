package com.project.meetu.arcityguide;

//import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.R.attr.max;

/**
 * Created by Meetu on 17-02-2017.
 */

public class MapFragment1 extends Fragment{

    coordinateTask c1;
    Marker mLocationMarker;
    GoogleMap Map1;
    MapFragment mapFragment;
    ArrayList<LatLng> points;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        View view=inflater.inflate(R.layout.fragment_map, container, false);
        //setContentView(R.layout.fragment_map);
        mapFragment = (MapFragment) getActivity().getFragmentManager().findFragmentById(R.id.map1);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {

                Map1=map;
                //Map1.setMaxZoomPreference(14.0f);
                //Map1.setMinZoomPreference(6.0f);
                continueOnCreate();


            }
        });



        return view ;
    }
    public void continueOnCreate() {
        coordinateTask c1 = new coordinateTask(getActivity(), mapFragment, Map1);
        String Origin = NavigationActivity.inputs.get(0);
        String Destination = NavigationActivity.inputs.get(1);
        try {
            points = c1.execute(Origin, Destination).get();
            NavigationActivity.setPoints(points);
        }catch(Exception e){

        }
    }
    public static MapFragment1 newInstance() {
        return new MapFragment1();
    }
    public Marker addLocationMarker(MarkerOptions mLocationMarkerOptions){

        mLocationMarker= Map1.addMarker(mLocationMarkerOptions);
        return mLocationMarker;
    }
}
