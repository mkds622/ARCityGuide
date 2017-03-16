package com.project.meetu.arcityguide;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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
import java.util.Map;

import static android.R.attr.max;
import static com.facebook.FacebookSdk.getApplicationContext;

/**
 * Created by Meetu on 08-03-2017.
 */
public class coordinateTask extends AsyncTask<String,Void, ArrayList<LatLng>> {

    public ArrayList<LatLng> points ;
    MapFragment mapFragment;
    GoogleMap Map1;
    GoogleMapOptions map1options;
    Activity context;
    private final String LOG_TAG = coordinateTask.class.getSimpleName();
    //
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;
    String resultString = null;


    coordinateTask(Activity context,MapFragment mapFragment,GoogleMap Map1){
        this.context=context;
        this.mapFragment=mapFragment;
        this.Map1= Map1;
    }

    @Override
    protected ArrayList<LatLng> doInBackground(String... params) {
        try {
            final String base_url = "https://maps.googleapis.com/maps/api/directions/json?";
            final String org_str = "origin";
            final String dest_str = "destination";
            final String mode_str="mode";
            final String key_str = "key";
            final String apikey = BuildConfig.Google_Maps_Geocoding_Api_Key;
            Uri builturi = Uri.parse(base_url).buildUpon()
                    .appendQueryParameter(org_str, params[0].toString().replace(" ", ""))
                    .appendQueryParameter(dest_str, params[1].toString().replace(" ", ""))
                    .appendQueryParameter(mode_str, "driving")
                    .appendQueryParameter(key_str, apikey).build();
            Log.e(LOG_TAG, builturi.toString());
            URL url = new URL(builturi.toString());
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                Log.v(LOG_TAG, "V1");
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.

            }
            resultString = buffer.toString();
            ArrayList<LatLng> points =jsonParse(resultString);
            Log.v(LOG_TAG,points.toString());
            return points;

        } catch (IOException e) {
            Log.w(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.w(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return points;
    }
    protected void onPostExecute(final ArrayList<LatLng> points) {

        //MapFragment mapFragment=MapFragment.newInstance(map1options);
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
        //UiSettings ui=Map1.getUiSettings();
        //ui.setAllGesturesEnabled(true);
        Toast.makeText(context, "MapReady", Toast.LENGTH_SHORT).show();
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
    public static ArrayList<LatLng> jsonParse(String RjsonString) {
        final String LOG_TAG = LoginActivity.class.getSimpleName();
        try {
            JSONObject resp = new JSONObject(RjsonString);
            JSONArray routeObject = resp.getJSONArray("routes");
            JSONObject routes = routeObject.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            ArrayList<LatLng> points = decodePoly(encodedString);
            return points;

        } catch (JSONException e) {
            Log.e(LOG_TAG, "JSON EXCEPTION @ JSONPARSE");
        }
        return null;
    }

    private static ArrayList<LatLng> decodePoly(String encoded) {

        Log.i("Location", "String received: " + encoded);
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }

        for (int i = 0; i < poly.size(); i++) {
            Log.i("Location", "Point sent: Latitude: " + poly.get(i).latitude + " Longitude: " + poly.get(i).longitude);
        }
        return poly;
    }

}