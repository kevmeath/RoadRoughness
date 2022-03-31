package km.roadroughness;

import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import km.roadroughness.databinding.ActivityViewMapBinding;
import km.roadroughness.db.AppDatabase;
import km.roadroughness.db.Route;
import km.roadroughness.db.RouteDAO;

public class ViewMapActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityViewMapBinding binding;

    private int selectedIndex = 0;

    private PolylineOptions polylineOptions = new PolylineOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityViewMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        selectRoute();
    }

    private void selectRoute() {
        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "saved-routes").allowMainThreadQueries().build();
        RouteDAO routeDAO = db.routeDAO();

        String[] routeNames = routeDAO.getAllNames();

        new AlertDialog.Builder(ViewMapActivity.this)
                .setTitle(R.string.dialog_select_route)
                .setItems(routeNames, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int index) {
                        selectedIndex = index;
                        showRouteOnMap(routeDAO.getRoute(routeNames[selectedIndex]));
                    }
                }).show();
    }

    private void showRouteOnMap(Route route) {
        double[][] locations = route.getLocations();
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(locations[0][0], locations[0][1])));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        float[] roughness = route.getRoughness();
        float max = 0;
        for (Float r: roughness) {
            if (r > max) {
                max = r;
            }
        }

        for (int i = 1; i < locations.length; i++) {
            LatLng lastLocation = new LatLng(locations[i-1][0], locations[i-1][1]);
            LatLng currentLocation = new LatLng(locations[i][0], locations[i][1]);
            float r = roughness[i];
            int colour = getColourScale(r/max);
            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.color(colour);
            polylineOptions.add(lastLocation);
            polylineOptions.add(currentLocation);
            mMap.addPolyline(polylineOptions);
        }
    }

    private int getColourScale(float percentage) {
        int A = 255;
        int R = percentage >= 0.5 ? 255 : (int) percentage * 255;
        int G = percentage <= 0.5 ? 255 : (int) (1 - percentage) * 255;
        int B = 0;
        return (A & 0xff) << 24 | (R & 0xff) << 16 | (G & 0xff) << 8 | B;
    }
}