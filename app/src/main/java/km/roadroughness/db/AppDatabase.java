package km.roadroughness.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Route.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract RouteDAO routeDAO();
}
