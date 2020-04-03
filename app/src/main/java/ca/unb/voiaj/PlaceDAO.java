package ca.unb.voiaj;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Update;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

// Represents the Data access object for the UNBPlace table
@Dao
public interface PlaceDAO {

    @Query("SELECT * FROM Place_table WHERE id = :id")
    List<Place> getPlace(String id);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertPlace(Place place);

    @Update()
    void updatePlace(Place place);
}
