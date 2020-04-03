package ca.unb.voiaj;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

// Loads JSON contents and returns an array of Places, taken from Lab Exam
public class jsonDBUtils {
    private static final String INPUT_JSON_FILE = "MockData.json";
    private static final String INPUT_JSON_FILE_UNB = "UNBMockData.json";
    private static final String JSON_KEY_GEO = "geometry";
    private static final String JSON_KEY_LOCATION = "location";
    private static final String JSON_KEY_TITLES = "results";
    private static final String KEY_PLACE_ID = "id";
    private static final String KEY_ADDRESS = "formatted_address";
    private static final String KEY_NAME = "name";
    private static final String KEY_LATITUDE = "lat";
    private static final String KEY_LONGITUDE = "lng";

    private ArrayList<Place> places;

    // Initializer to read our data source (JSON file) into an array of host city objects
    public jsonDBUtils(Context context) {
        processJSON(context);
    }

    private void processJSON(Context context) {
        places = new ArrayList<>();

        try {
            // Create a JSON Object from file contents String
            JSONObject jsonObject = new JSONObject(loadJSONFromAssets(context));

            // Create a JSON Array from the JSON Object
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_TITLES);

            for (int i=0; i < jsonArray.length(); i++) {
                // Create a JSON Object from individual JSON Array element
                JSONObject elementObject = jsonArray.getJSONObject(i);
                JSONObject Geo = elementObject.getJSONObject(JSON_KEY_GEO);
                JSONObject Loc = Geo.getJSONObject(JSON_KEY_LOCATION);

                // Get data from individual JSON Object and create a Place object
                Place place = new Place();

                place.setId(elementObject.getString(KEY_PLACE_ID));
                place.setFormattedAddress(elementObject.getString(KEY_ADDRESS));
                place.setName(elementObject.getString(KEY_NAME));
                place.setVisited(false);
                place.setLatitude(Loc.getString(KEY_LATITUDE));
                place.setLongitude(Loc.getString(KEY_LONGITUDE));

                // Add new place to places ArrayList
                places.add(place);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromAssets(Context context) {
        try {
            InputStream is = context.getAssets().open(INPUT_JSON_FILE);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

    }

    public ArrayList<Place> getPlaces() {
        return places;
    }
}
