package ca.unb.voiaj.view;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import ca.unb.voiaj.R;
import ca.unb.voiaj.service.Place;
import ca.unb.voiaj.service.JsonDBUtils;
import ca.unb.voiaj.viewmodel.PlaceViewModel;


public class MainActivity  extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    PlaceViewModel view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = new ViewModelProvider(this).get(PlaceViewModel.class);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(isServiceOK() && (savedInstanceState == null)) {
            Fragment homeFragment = new MapsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.content_frame, homeFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //toolbar.setOnMenuItemClickListener(mainOnToolbarMenuItemSelectedListener);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigationView);
        navigation.setOnNavigationItemSelectedListener(mainOnNavigationItemSelectedListener);

        // Initialize shared preference that determines whether or not this is Voiaj's first run.
        // If this is the first run, the defValue will be true, indicating a first run
        Boolean firstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("firstRun", true);

        // If this is the first run, then load JSON data into the database and notify the user with a greeting that data is being loaded
        if (firstRun) {
            Log.d(TAG, "onCreate: First run enters!");
            Toast.makeText(MainActivity.this, "Welcome to Voiaj! We're loading location data as we speak.", Toast.LENGTH_LONG).show();
            ArrayList<Place> placesList = loadJSONData();
            for(Place p : placesList) {
                new loadRoom().execute(p);
            }
        }

        // Set the shared preference to false, since any following instance is not the first run of the application
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit().putBoolean("isFirstRun", false).commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mainOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment fragment;

            switch(menuItem.getItemId()) {
                case R.id.navigation_places:
                    fragment = new PlacesFragment();
                    loadFragment(fragment);
                    return true;
                case R.id.navigation_map:
                    fragment = new MapsFragment();
                    loadFragment(fragment);
                    return true;
//                case R.id.navigation_settings:
//                    fragment = new InventoryFragment();
//                    loadFragment(fragment);
//                    return true;
            }
            return false;
        }
    };

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.content_frame, fragment);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        transaction.commit();
    }

    public boolean isServiceOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occurred but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    // Loads contents of JSON data from both JSON files into Place objects to be inserted into the database
    public ArrayList<Place> loadJSONData(){
        JsonDBUtils jsondbUtils = new JsonDBUtils(getApplicationContext());
        return jsondbUtils.getPlaces();
    }

    public class loadRoom extends AsyncTask<Place, Void, Void> {
        protected Void doInBackground(Place... params){
            Place places = params[0];
            view.addPlace(places);
            return null;
        }
    }
    /*
    // Loads the places objects into the room database
    public void loadRoom(ArrayList<Place> placeList){
        PlaceViewModel view = new ViewModelProvider(this).get(PlaceViewModel.class);

        for (Place p: placeList) {
            view.addPlace(p);
        }
    }*/
}
