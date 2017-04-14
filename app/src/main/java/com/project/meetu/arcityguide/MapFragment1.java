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
    Polyline P1;
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
//            NavigationActivity.setPolyline(P1);
            LatLng src=new LatLng(0.0,0.0);
            src=points.get(0);
            LatLng dest=new LatLng(0.0,0.0);
            dest=points.get(points.size()-1);
            drawMarker(Map1,src,dest);
            PolylineOptions polylineOptions = new PolylineOptions();
            Map1.moveCamera(CameraUpdateFactory.newLatLngZoom(src, 15));
// Create polyline options with existing LatLng ArrayList
            polylineOptions.addAll(points);
            polylineOptions
                    .width(15)
                    .color(Color.RED);
            Polyline l1=Map1.addPolyline(polylineOptions);
            //l1.setJointType(JointType.ROUND);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(src)
                    .zoom(max)
                    .tilt(90)
                    .build();
            Map1.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            NavigationActivity.setPolyline(P1);
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

    public void setMapOrientation(LatLng mLastLocation, float degree){
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mLastLocation)
                .zoom(max)
                .tilt(90)
                .bearing(degree)
                .build();
        Map1.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }
    public void drawMarker(GoogleMap map1,LatLng source_point,LatLng destination_point) {

        // Creating an instance of MarkerOptions
        MarkerOptions markerOptions1 = new MarkerOptions();
        markerOptions1.title("Marker");
        markerOptions1.snippet("Marker Yo Yo");
        markerOptions1.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerOptions1.position(source_point);

        MarkerOptions markerOptions2 = new MarkerOptions();
        markerOptions2.title("Marker2");
        markerOptions2.snippet("Marker Xo Xo");
        markerOptions2.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        markerOptions2.position(destination_point);

        // Adding marker on the Google Map

        map1.addMarker(markerOptions1);
        map1.addMarker(markerOptions2);
    }
}
