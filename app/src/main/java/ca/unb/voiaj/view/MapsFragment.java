package ca.unb.voiaj.view;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.List;

import ca.unb.voiaj.R;
import ca.unb.voiaj.service.Place;
import ca.unb.voiaj.view.utils.GooglePlace;
import ca.unb.voiaj.view.utils.JsonUtils;
import ca.unb.voiaj.viewmodel.PlaceViewModel;

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
    private Marker m;
    private SharedPreferences prefs;
    private PlaceViewModel pview;
    private List<Place> p;

    public MapsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_style));
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.d(TAG, "Clicked on marker: ");

                LatLng marked = marker.getPosition();

                String name = marker.getTitle();

                //if marker is visited
                if(marker.getAlpha() == 0.99f){
                    marker.remove();
                    Marker made = mMap.addMarker(new MarkerOptions().position(marked)
                            .icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                            .alpha(1f)
                            .title(name));

                    made.showInfoWindow();
                    //checking db for Place with equal LatLng
                    UpdateAsync(marked);
                } else{
                    //if marker is unvisited
                    marker.remove();
                    Marker made = mMap.addMarker(new MarkerOptions().position(marked)
                            .icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                            .alpha(0.99f)
                            .title(name));
                    made.showInfoWindow();
                    //checking db for Place with equal LatLng
                    UpdateAsync(marked);
                }
                return false;
            }
        });

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
    public void UpdateAsync(final LatLng check){
        new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < p.size(); i++){
                    LatLng l = new LatLng(Double.valueOf(p.get(i).getLatitude()), Double.valueOf(p.get(i).getLongitude()));
                    if (check.equals(l)) {
                        Log.d(TAG, "Marker equals to db value");
                        if(p.get(i).isVisited()){
                            pview.updatePlace(p.get(i).getId(), p.get(i).getFormattedAddress(), p.get(i).getName(), false, p.get(i).getLongitude(), p.get(i).getLatitude());
                        }
                        else{
                            pview.updatePlace(p.get(i).getId(), p.get(i).getFormattedAddress(), p.get(i).getName(), true, p.get(i).getLongitude(), p.get(i).getLatitude());
                        }
                    }
                }
            }
        }).start();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle(fragmentName);
        pview = new ViewModelProvider(this).get(PlaceViewModel.class);
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
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
                            if(!prefs.getBoolean("firstTime", false)) {
                                Downloader dTask = new Downloader();
                                dTask.execute();
                                SharedPreferences.Editor editor = prefs.edit();
                                editor.putBoolean("firstTime", true);
                                editor.commit();
                            }else{
                                Log.d(TAG, "Load from DB");
                                doAsync async = new doAsync();
                                async.execute();

                            }

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


    public class doAsync extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... voids) {
            //getting info from DB in background
            p = pview.getPlace();
            return "Done";
        }

        @Override
        protected void onPostExecute(String s) {
            //setting all markers once complete
            for (int i = 0; i < p.size(); i++) {
                LatLng loc = new LatLng(Double.valueOf(p.get(i).getLatitude()), Double.valueOf(p.get(i).getLongitude()));
                if (p.get(i).isVisited() == true) {
                    Marker made = mMap.addMarker(new MarkerOptions().position(loc)
                            .icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)))
                            .alpha(1f)
                            .title(p.get(i).getName()));
                    made.showInfoWindow();
                } else {
                    Marker made = mMap.addMarker(new MarkerOptions().position(loc)
                            .icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))
                            .alpha(1f)
                            .title(p.get(i).getName()));
                    made.showInfoWindow();
                }
            }
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
                m = mMap.addMarker(new MarkerOptions().position(newMarker)
                        .alpha(0.99f)
                        .title(name));
                m.showInfoWindow();
            }

        }
    }
}