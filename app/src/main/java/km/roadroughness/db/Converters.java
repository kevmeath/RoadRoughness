package km.roadroughness.db;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Converters {
    @TypeConverter
    public static double[][] stringToLocations(String value) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(value, double[][].class);
    }

    @TypeConverter
    public static String locationsToString(double[][] locations) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(locations);
    }

    @TypeConverter
    public static float[] stringToRoughness(String value) {
        Gson gson = new GsonBuilder().create();
        return gson.fromJson(value, float[].class);
    }

    @TypeConverter
    public static String roughnessToString(float[] roughness) {
        Gson gson = new GsonBuilder().create();
        return gson.toJson(roughness);
    }
}
