package km.roadroughness.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RouteDAO {

    @Query("SELECT * FROM routes")
    List<Route> getAll();

    @Query("SELECT name FROM routes")
    String[] getAllNames();

    @Query("SELECT * FROM routes WHERE name = :name")
    Route getRoute(String name);

    @Insert
    void insertRoute(Route route);
}
