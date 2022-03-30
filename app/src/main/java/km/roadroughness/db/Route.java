package km.roadroughness.db;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "routes")
public class Route {

    @PrimaryKey
    @NonNull
    private String name = new Date(System.currentTimeMillis()).toString();

    private double[][] locations;
    private float[] roughness;

    public Route(String name, double[][] locations, float[] roughness) {
        this.setName(name);
        this.setLocations(locations);
        this.setRoughness(roughness);
    }

    public Route() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double[][] getLocations() {
        return locations;
    }

    public float[] getRoughness() {
        return roughness;
    }

    public void addRoughness(float r) {
        if (getRoughness() == null) {
            setRoughness(new float[] {r});
        }

        int size = getRoughness().length + 1;
        float[] newRoughness = new float[size];

        for (int i = 0; i < size - 1; i++) {
            newRoughness[i] = getRoughness()[i];
        }
        newRoughness[size - 1] = r;

        setRoughness(newRoughness);
    }

    public void addLocation(double[] location) {
        if (getLocations() == null) {
            setLocations(new double[][] {location});
        }

        int size = getLocations().length + 1;
        double[][] newLocations = new double[size][2];

        for (int i = 0; i < size - 1; i++) {
            newLocations[i] = getLocations()[i];
        }
        newLocations[size - 1] = location;

        setLocations(newLocations);
    }

    public void setLocations(double[][] locations) {
        this.locations = locations;
    }

    public void setRoughness(float[] roughness) {
        this.roughness = roughness;
    }
}
