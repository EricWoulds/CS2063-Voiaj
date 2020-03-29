package ca.unb.voiaj;

import android.content.Context;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import static android.util.Log.d;

public class JsonUtils {
    private static final String INPUT_JSON_FILE = "MockData.json";

    private static final String TAG = "JsonUtils";
    private static final String JSON_KEY_TITLE = "name";
    private static final String JSON_KEY_TITLES = "results";
    private static final String JSON_KEY_GEO = "geometry";
    private static final String JSON_KEY_LOCATION = "location";
    private static final String JSON_KEY_LAT = "lat";
    private static final String JSON_KEY_LNG = "lng";

    private ArrayList<GooglePlace> googlePlacesArray;

    public JsonUtils(Context context) {
        processJSON(context);
    }

    private void processJSON(Context context) {
        googlePlacesArray = new ArrayList<>();
        try {
            //JsonParser parser = Json.createParser(new StringReader(jsonString));

            d(TAG, "processJson: loader != null");

            if(loadJSONFromURL(context) != null) {

                JSONObject jsonObject = new JSONObject(loadJSONFromURL(context));

                JSONArray jsonArray = jsonObject.getJSONArray(JSON_KEY_TITLES);

                for (int i = 0; i < jsonArray.length(); i++) {
                    // Create a JSON Object from individual JSON Array element
                    JSONObject elementObject = jsonArray.getJSONObject(i);
                    JSONObject Geo = elementObject.getJSONObject(JSON_KEY_GEO);
                    JSONObject Loc = Geo.getJSONObject(JSON_KEY_LOCATION);

                    // Get data from individual JSON Object
                    GooglePlace place = new GooglePlace.Builder(elementObject.getString(JSON_KEY_TITLE),
                            Loc.getString(JSON_KEY_LAT),
                            Loc.getString(JSON_KEY_LNG))
                            .build();

                    googlePlacesArray.add(place);
                }
            }
            } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String loadJSONFromURL(Context context) {
        /*HttpURLConnection connection = null;
        try {
            URL url = new URL("https://maps.googleapis.com/maps/api/place/textsearch/json?" +
                    "query=Landmark" +
                    "&location=45.9636,66.6431" +
                    "&radius=2000" +
                    "&key=AIzaSyAmTIQ7Pljct5SqiAl4b5EPqqFCQ46K6HY");
            connection = (HttpURLConnection) url
                    .openConnection();
            d(TAG, "Connection" + connection.toString());
            return convertStreamToString(connection.getInputStream());
        } catch (MalformedURLException exception) {
            Log.e(TAG, "MalformedURLException");
            return null;
        } catch (IOException exception) {
            Log.e(TAG, "IOException" + exception.toString());
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != connection)
                connection.disconnect();
        }*/
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

    /*private String convertStreamToString(InputStream is) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();

        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
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
*/
    public ArrayList<GooglePlace> getGooglePlace() {
        return googlePlacesArray;
    }
}
