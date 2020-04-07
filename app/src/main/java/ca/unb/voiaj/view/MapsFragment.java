package ca.unb.voiaj.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.AsyncTask;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;

import ca.unb.voiaj.R;
import ca.unb.voiaj.view.utils.GooglePlace;
import ca.unb.voiaj.view.utils.JsonUtils;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private String fragmentName = "Map";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 9002;
    private static final float DEFAULT_ZOOM = 16f;

    private GoogleMap mMap;
    private boolean perGranted = false;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationManager locationManager;

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private ArrayList<GooglePlace> mGooglePlaces;
    public Context context;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (perGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }else{
            mMap.setMyLocationEnabled(false);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        getActivity().setTitle(fragmentName);
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        locationManager =(LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        getLocationPermission();
        return view;
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
        try {
                if (perGranted) {
                    mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style));

                    final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
                    location.addOnCompleteListener(new OnCompleteListener<Location>() {

                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: found location!");
                                Location currentLocation = (Location) task.getResult();
                                if(currentLocation != null){
                                    moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                            DEFAULT_ZOOM);
                                }

                                Downloader dTask = new Downloader();
                                dTask.execute();

                            } else {
                                Log.d(TAG, "onComplete: current location is null");
                                Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

        } catch (SecurityException e) {
            Log.d(TAG, "getDeviceLocation: " + e.getMessage());
        }
    }


    private void moveCamera(LatLng latLng, float zoom) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void startMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }
    private double getDistance(LatLng userLocation, LatLng marker) {
        float[] dist = new float[1];

        Location.distanceBetween(userLocation.latitude,userLocation.longitude, marker.latitude, marker.longitude, dist);

        return dist[0] / 1000;
    }



    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                perGranted = true;
                startMap();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public  class Downloader extends AsyncTask<Void, Void, ArrayList<GooglePlace>>{

        @Override
        protected ArrayList<GooglePlace> doInBackground(Void... params) {
            JsonUtils Json = new JsonUtils(getActivity());
            Log.d(TAG,"JSON processed");
            mGooglePlaces = Json.getGooglePlace();
            return mGooglePlaces;
        }
        protected void onPostExecute(ArrayList<GooglePlace> result) {

            for(int i = 0; i < mGooglePlaces.size(); i++){
                final GooglePlace gPlace = mGooglePlaces.get(i);
                String name = gPlace.getName();
                String lat = gPlace.getLatitude();
                String lng = gPlace.getLongitude();

                Double latd = Double.valueOf(lat);
                Double lngd = Double.valueOf(lng);
                Log.d(TAG, "LAT "+ latd.toString() + " LNG "+ lngd.toString());

                LatLng newMarker = new LatLng(latd, lngd);
                mMap.addMarker(new MarkerOptions().position(newMarker)
                        .title(name));
            }
        }
    }
}