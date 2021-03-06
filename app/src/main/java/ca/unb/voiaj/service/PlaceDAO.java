package ca.unb.voiaj.service;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

import ca.unb.voiaj.service.Place;

// Represents the Data access object for the UNBPlace table
@Dao
public interface PlaceDAO {

    @Query("SELECT * FROM Place_table WHERE id = :id")
    List<Place> getPlace(String id);

    @Query("SELECT * FROM Place_table")
    List<Place> getAllPlaces();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlace(Place place);

    @Update()
    void updatePlace(Place place);
}
