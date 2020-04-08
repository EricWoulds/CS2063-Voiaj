package ca.unb.voiaj.viewmodel;

import androidx.lifecycle.AndroidViewModel;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import android.app.Application;
import java.util.List;

import ca.unb.voiaj.service.Place;
import ca.unb.voiaj.service.PlacesRepository;

// View Model for Places implemented for scalability, with help from Lab 6
public class PlaceViewModel extends AndroidViewModel {
    private PlacesRepository placesRepository;

    public PlaceViewModel(@NonNull Application application) {
        super(application);
        placesRepository = new PlacesRepository(application);
    }

    public List<Place> getPlaceById(String id){
        List<Place> places = placesRepository.getPlaceByIdRepo(id);
        return places;
    }
    public List<Place> getPlace(){
        List<Place> places = placesRepository.getPlaces();
        return places;
    }
    public void addPlace(Place newPlace){
        placesRepository.insertPlaceRepo(newPlace);
    }

    public void updatePlace(String id, String address, String name, boolean visited, String longitude, String latitude){
        placesRepository.updatePlaceRepo(id, address, name, visited, longitude, latitude);
    }
}
