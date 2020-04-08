package ca.unb.voiaj.service;

import android.app.Application;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import ca.unb.voiaj.service.AppDatabase;
import ca.unb.voiaj.service.Place;
import ca.unb.voiaj.service.PlaceDAO;

public class PlacesRepository {
    private PlaceDAO placeDao;
    private static final String TAG = "PlacesRepository";

    public PlacesRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
         placeDao = db.placeDao();
    }

    // Calls the DAO to get a place by id
    public List<Place> getPlaceByIdRepo(final String id) {
        Future<List<Place>> future = AppDatabase.databaseWriterExecutor.submit(new Callable<List<Place>>() {
            public List<Place> call() throws Exception {
                List<Place> list = placeDao.getPlace(id);
                return list;
            }
        });
        try{
            return future.get();
        }
        catch (ExecutionException | InterruptedException e){
            return new ArrayList<>();
        }
    }

    public List<Place> getPlaces() {
        Future<List<Place>> future = AppDatabase.databaseWriterExecutor.submit(new Callable<List<Place>>() {
            public List<Place> call() throws Exception {
                List<Place> list = placeDao.getAllPlaces();
                return list;
            }
        });
        try{
            return future.get();
        }
        catch (ExecutionException | InterruptedException e){
            return new ArrayList<>();
        }
    }

    // Calls the DAO to insert a place
    public void insertPlaceRepo(Place newPlace) {
        placeDao.insertPlace(newPlace);
        Log.d(TAG, "insertPlaceRepo: Inserting:" + newPlace.getName());
    }

    // Calls the DAO to update a place with its new instance
    public void updatePlaceRepo(String id, String address, String name, boolean visited, String longitude, String latitude){
        Place updatedPlace = new Place();

        updatedPlace.setId(id);
        updatedPlace.setFormattedAddress(address);
        updatedPlace.setName(name);
        updatedPlace.setVisited(visited);
        updatedPlace.setLongitude(longitude);
        updatedPlace.setLatitude(latitude);

        placeDao.updatePlace(updatedPlace);
    }
}
