package ca.unb.voiaj;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;


public class MainActivity  extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int ERROR_DIALOG_REQUEST = 9001;
    PlaceViewModel view;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        view = new ViewModelProvider(this).get(PlaceViewModel.class);
        super.onCreate(savedInstanceState);
        if(isServiceOK()) {
            Intent intent = new Intent(MainActivity.this, MapsActivity.class);
            startActivity(intent);
        }

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
        jsonDBUtils jsondbUtils = new jsonDBUtils(getApplicationContext());
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
