package km.roadroughness.db;

import android.location.Location;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "routes")
public class Route {

    @PrimaryKey
    private String name;
    private Location[] route;
    private Float[] roughness;

    public Route(String name, Location[] route, Float[] roughness) {
        this.name = name;
        this.route = route;
        this.roughness = roughness;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location[] getRoute() {
        return route;
    }

    public void setRoute(Location[] route) {
        this.route = route;
    }

    public Float[] getRoughness() {
        return roughness;
    }

    public void setRoughness(Float[] roughness) {
        this.roughness = roughness;
    }
}
