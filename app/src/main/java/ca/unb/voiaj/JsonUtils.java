package ca.unb.voiaj;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import ca.unb.voiaj.GooglePlace;

public class JsonUtils {
    private static final String TAG = "JsonUtils";
    private static final String JSON_KEY_TITLE = "name";
    private static final String JSON_KEY_LAT = "lat";
    private static final String JSON_KEY_LNG = "lng";

    private ArrayList<GooglePlace> googlePlacesArray;

    // Initializer to read our data source (JSON file) into an array of course objects
    public JsonUtils() {
        processJSON();
    }

    private void processJSON() {
        googlePlacesArray = new ArrayList<>();

        String jsonString = loadJSONFromURL();

        try {
            //JsonParser parser = Json.createParser(new StringReader(jsonString));
            JSONObject jsonObject = new JSONObject(loadJSONFromURL());

            JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_TITLE);
            for (int i=0; i < jsonArray.length(); i++) {
                // Create a JSON Object from individual JSON Array element
                JSONObject elementObject = jsonArray.getJSONObject(i);

                // Get data from individual JSON Object
                GooglePlace place = new GooglePlace.Builder(elementObject.getString(JSON_KEY_TITLE),
                        elementObject.getString(JSON_KEY_LAT),
                        elementObject.getString(JSON_KEY_LNG))
                        .build();

                googlePlacesArray.add(place);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromURL() {
        HttpURLConnection connection = null;
        String url = "https://maps.googleapis.com/maps/api/place/findplacefromtext//json" +
                "?input=Landmark" +
                "&input&location=45.9636,66.6431" +
                "&radius=2000" +
                "&key=AIzaSyAmTIQ7Pljct5SqiAl4b5EPqqFCQ46K6HY";
        String data = null;

        try {
            connection = (HttpURLConnection) new URL(url)
                    .openConnection();
            Log.d(TAG, "Connection" + connection.toString());
            return convertStreamToString(connection.getInputStream());
        } catch (MalformedURLException exception) {
            Log.e(TAG, "MalformedURLException");
            return null;
        } catch (IOException exception) {
            Log.e(TAG, "IOException");
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != connection)
                connection.disconnect();
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();
    }

    public ArrayList<GooglePlace> getGooglePlace() {
        return googlePlacesArray;
    }
}
