package com.project.meetu.arcityguide;

import android.*;
import android.Manifest;
import android.app.ActionBar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.project.meetu.arcityguide.PolyUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;
import static com.google.ads.AdRequest.LOGTAG;

/**
 * Created by Meetu on 17-02-2017.
 */

public class NavigationActivity extends FragmentActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, SensorEventListener {//implements GoogleApiClient.ConnectionCallbacks ,LocationListener, GoogleApiClient.OnConnectionFailedListener {


    private NavFragmentAdapter N1;
    ViewPager mViewPager;
    Toast toast1, toast2, toast3, toast4;
    public static String passedOn;
    public static List<String> inputs;
    public static ArrayList<LatLng> points;//to be updated in rerouting
    LatLng next;//to be updated in rerouting
    static LatLng dest;//to be verified in rerouting
    static List<LatLng> temp;//to be changed in rerouting
    static int size;//to be updated in rerouting
    //for location
    int permissionCheck;
    public static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOACTION = 0;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected Boolean mRequestingLocationUpdates;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LatLng mLastLocation1;
    Marker mLocationMarker;
    public static int navCount = 0;
    public static int navErrorCount = 0;
    //TextView mLatitudeText,mLongitudeText;
    LocationRequest mLocationRequest;
    LocationSettingsRequest.Builder builder;
    PendingResult<LocationSettingsResult> result;

    // Variables for Sensor data
    //TextView orientationTextView;
    private SensorManager mSensorManager;

    //new
    private final float[] mAccelerometerReading = new float[3];
    private final float[] mMagnetometerReading = new float[3];

    private final float[] mRotationMatrix = new float[9];
    private final float[] outRotationMatrix = new float[9];
    private final float[] mOrientationAngles = new float[3];
    //GeomagneticField gm;
    MarkerOptions mLocationMarkerOptions;
    public BitmapDescriptor locationIcon;


    private static final String LOGTAG = "Navigation Activity";
    static Polyline p1;
    Button B1;
    boolean isNavigationOn,isNavigationComplete;
    double d_required;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        //initialize location marker
        MapsInitializer.initialize(getApplicationContext());
        locationIcon = BitmapDescriptorFactory.fromResource(R.drawable.mylocationmarker);
//Below code is for continually extracting sensor data and GPS data which we will use for navigation purposes
        mRequestingLocationUpdates = false;
        toast1 = Toast.makeText(getApplicationContext(), "AR Fragment", Toast.LENGTH_SHORT);
        toast2 = Toast.makeText(getApplicationContext(), "Map Fragment", Toast.LENGTH_SHORT);
        toast3 = Toast.makeText(getApplicationContext(), "Correct Direction", Toast.LENGTH_SHORT);
        toast4 = Toast.makeText(getApplicationContext(), "InCorrect Direction", Toast.LENGTH_SHORT);
        //permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        //ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOACTION);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this /* FragmentActivity */,
                            this /* OnConnectionFailedListener */)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        B1=(Button)findViewById(R.id.navigationControl);
        B1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startNavigation();
            }
        });
        createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                builder.build());
        //mGoogleApiClient.connect();

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        //Receive Intent and get src and dest locations....
        Intent I1 = getIntent();
        passedOn = I1.getStringExtra(InputActivity.EXTRA_MESSAGE);
        inputs = Arrays.asList(NavigationActivity.passedOn.split("@"));

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        N1 = new NavFragmentAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(N1);
        mViewPager.setCurrentItem(0);



    }
    public static void setPoints(ArrayList<LatLng> points1) {
        points = points1;
        size = points.size();
        dest = points.get(size - 1);
        temp=new ArrayList<>();
        temp.add(dest);
    }
    public void startNavigation(){
        navCount=0;
        navErrorCount=0;
        isNavigationOn=true;
    }
    public void reroute(){

    }
    public static void setPolyline(Polyline P1){
        p1=P1;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOACTION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onStart() {

        mGoogleApiClient.connect();
        super.onStart();
    }
    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        //old and deprecated
        //mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
        //        SensorManager.SENSOR_DELAY_GAME);
        //New
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {

                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOACTION);

            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
            //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
        }
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    protected void startLocationUpdates() {
        mRequestingLocationUpdates = true;
        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {


            } else {


                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOACTION);

            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        mLastLocation1 = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
        Log.d("OnLocationChanged()", "mLastLocation=" + mLastLocation.getLatitude() + " " + mLastLocation.getLongitude() + "mLastLocation1=" + mLastLocation1);

        try {
            if(isNavigationOn==true){
            int x = PolyUtil.locationIndexOnPath(mLastLocation1, points, true, 30);
            Log.e(LOGTAG, "x=" + x + "navcount=" + navCount + "NavErrorCOunt=" + navErrorCount);
            if (PolyUtil.isLocationOnPath(mLastLocation1, temp, true, 10) == true) {
                Toast.makeText(this, "NavgationComplete", Toast.LENGTH_LONG);

            }
            if (x == -1) {
                navErrorCount += 1;
                if (navErrorCount > 11) {
                    Toast.makeText(this, "Have to reroute", Toast.LENGTH_SHORT).show();
                    navCount = 0;
                    navErrorCount = 0;
                    //pauseNavigationAndRecord();
                    reroute();
                }
            } else {
                navCount = x;

            }
            Log.d("OnLocationChanged()", "next" + next);
        }
//            if (mLastLocation1.latitude == dest.latitude && mLastLocation1.longitude == dest.longitude) {
//                //endNavigation(true);
//            }
//            if (mLastLocation1.latitude == next.latitude && mLastLocation1.longitude == next.longitude) {
//                navCount += 1;
//                navErrorCount = 0;
//            } else if (navErrorCount < 11) {
//                navErrorCount += 1;
//            } else {
//                Toast.makeText(this, "Have to reroute", Toast.LENGTH_SHORT).show();
//                navCount = 0;
//          0      //pauseNavigationAndRecord();
//                //reroute(points,navCount);
//            }
            if (mLocationMarker != null) {
                mLocationMarker.remove();
            }
            //mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            updateUI(mLastLocation1);

        }catch(NullPointerException e){
            Log.e(LOGTAG,"points is null");
        }
    }

    private void updateUI(LatLng mLastLocation1) {
        //mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
        //mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));

        switch (mViewPager.getCurrentItem()) {
            case 0://toast2.cancel();
                //toast1.show();
                break;

            case 1://toast1.cancel();
                //toast2.show();
                mLocationMarkerOptions = new MarkerOptions().position(mLastLocation1).title("Current Location").icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(100, 100))).flat(true);
                MapFragment1 MF1temp = (MapFragment1) N1.myFragments.get(1);
                mLocationMarker = MF1temp.addLocationMarker(mLocationMarkerOptions);
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void createLocationRequest() {
        Log.d("Location Request", "created");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        //mLocationRequest.setSmallestDisplacement(5);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, mAccelerometerReading,
                    0, mAccelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, mMagnetometerReading,
                    0, mMagnetometerReading.length);

        }
        updateOrientationAngles();
        //gm= new GeomagneticField((float)mLastLocation.getLatitude(),(float)mLastLocation.getLongitude(),(float)mLastLocation.getAltitude(),mLastLocation.getTime());
        //double degree= Math.toDegrees(mOrientationAngles[0]);//+ gm.getDeclination();
        //degree=Math.round(degree);
        double degree = Math.round(mOrientationAngles[0] * (180.00 / 3.14));
        //if (points != null && mLastLocation!=null && mLocationMarker!=null)
        try{
            if(isNavigationOn==true){
            next = points.get(navCount+1);
            Location next1 = new Location("");
            next1.setLatitude(next.latitude);
            next1.setLongitude(next.longitude);
            d_required = mLastLocation.bearingTo(next1);}
            //double d_required = getBearing(mLastLocation1, points.get(navCount));


//Required in case of Road Detection
//            double temp=d_required-degree;
//            if(temp<-180)
//                temp+=360;
//            else if(temp>180)
//                temp-=360;
//            Log.e("OnSensorChanged:","Required:"+ d_required +"degree:" + degree + "temp:" + temp);

            switch (mViewPager.getCurrentItem()) {
                case 0:
//Required in case of Road Detection
//                    if (temp>-30 && temp<=30) {
//                        toast4.cancel();
//                        toast3.show();
//                    } else {
//                        toast3.cancel();
//                        toast4.show();
//                    }
                    if(isNavigationOn==true){
                    ARFragment ARtemp=(ARFragment)N1.myFragments.get(0);
                    ARtemp.render(d_required,degree);}
                    break;
                case 1://toast1.cancel();
                    //toast2.show();
                    mLocationMarker.setAnchor((float) 0.5, (float) 0.5);
                    mLocationMarker.setRotation((float) degree);
                    break;
            }
            //orientationTextView.setText(String.valueOf(degree));//+ " $$$$ " + String.valueOf(mOrientationAngles[1]) + " $$$$ " + String.valueOf(mOrientationAngles[2]));
            //}
        }catch(NullPointerException e){
            Log.e(LOGTAG,e.getMessage());
        }
    }

    public void updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles.

        mSensorManager.getRotationMatrix(mRotationMatrix, null,
                mAccelerometerReading, mMagnetometerReading);
        int screenRotation = this.getWindowManager().getDefaultDisplay().getRotation();
        int axisX, axisY;
        boolean isUpSideDown = mAccelerometerReading[2] < 0;

        switch (screenRotation) {
            case Surface.ROTATION_0:
                axisX = (isUpSideDown ? SensorManager.AXIS_MINUS_X : SensorManager.AXIS_X);
                axisY = (Math.abs(mAccelerometerReading[1]) > 6.0f ?
                        (isUpSideDown ? SensorManager.AXIS_MINUS_Z : SensorManager.AXIS_Z) :
                        (isUpSideDown ? SensorManager.AXIS_MINUS_Y : SensorManager.AXIS_Y));
                break;
            case Surface.ROTATION_90:
                axisX = (isUpSideDown ? SensorManager.AXIS_MINUS_Y : SensorManager.AXIS_Y);
                axisY = (Math.abs(mAccelerometerReading[0]) > 6.0f ?
                        (isUpSideDown ? SensorManager.AXIS_Z : SensorManager.AXIS_MINUS_Z) :
                        (isUpSideDown ? SensorManager.AXIS_X : SensorManager.AXIS_MINUS_X));
                break;
            case Surface.ROTATION_180:
                axisX = (isUpSideDown ? SensorManager.AXIS_X : SensorManager.AXIS_MINUS_X);
                axisY = (Math.abs(mAccelerometerReading[1]) > 6.0f ?
                        (isUpSideDown ? SensorManager.AXIS_Z : SensorManager.AXIS_MINUS_Z) :
                        (isUpSideDown ? SensorManager.AXIS_Y : SensorManager.AXIS_MINUS_Y));
                break;
            case Surface.ROTATION_270:
                axisX = (isUpSideDown ? SensorManager.AXIS_Y : SensorManager.AXIS_MINUS_Y);
                axisY = (Math.abs(mAccelerometerReading[0]) > 6.0f ?
                        (isUpSideDown ? SensorManager.AXIS_MINUS_Z : SensorManager.AXIS_Z) :
                        (isUpSideDown ? SensorManager.AXIS_MINUS_X : SensorManager.AXIS_X));
                break;
            default:
                axisX = (isUpSideDown ? SensorManager.AXIS_MINUS_X : SensorManager.AXIS_X);
                axisY = (isUpSideDown ? SensorManager.AXIS_MINUS_Y : SensorManager.AXIS_Y);
        }

        SensorManager.remapCoordinateSystem(mRotationMatrix, axisX, axisY, outRotationMatrix);
        //mSensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, outRotationMatrix);
        // "mRotationMatrix" now has up-to-date information.

        mSensorManager.getOrientation(outRotationMatrix, mOrientationAngles);

        // "mOrientationAngles" now has up-to-date information.

    }

    double radians(double n) {
        return n * (Math.PI / 180);
    }

    double degrees(double n) {
        return n * (180 / Math.PI);
    }

    double getBearing(LatLng src, LatLng dest) {
        double startLat = radians(src.latitude);
        double startLong = radians(src.longitude);
        double endLat = radians(dest.latitude);
        double endLong = radians(dest.longitude);

        double dLong = endLong - startLong;

        double dPhi = Math.log(Math.tan(endLat / 2.0 + Math.PI / 4.0) / Math.tan(startLat / 2.0 + Math.PI / 4.0));
        if (Math.abs(dLong) > Math.PI) {
            if (dLong > 0.0)
                dLong = -(2.0 * Math.PI - dLong);
            else
                dLong = (2.0 * Math.PI + dLong);
        }

        return (degrees(Math.atan2(dLong, dPhi)) + 360.0) % 360.0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public Bitmap resizeMapIcons(int width, int height) {
        Bitmap imageBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mylocationmarker);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
}
