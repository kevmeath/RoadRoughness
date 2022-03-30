package km.roadroughness;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

import km.roadroughness.databinding.ActivityRecordRouteBinding;
import km.roadroughness.db.AppDatabase;
import km.roadroughness.db.Route;
import km.roadroughness.db.RouteDAO;
import km.roadroughness.util.RRMath;

public class RecordRouteActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    // Map
    private GoogleMap mMap;
    private ActivityRecordRouteBinding binding;
    private Route route;
    private PolylineOptions polylineOptions = new PolylineOptions();

    // Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            for (Location location: locationResult.getLocations()) {

                // Don't record data when stopped.
                currentSpeed = location.getSpeed();
                if (currentSpeed > 0) {
                    // If there is at least 1 location recorded, calculate roughness from the last
                    // location to the current location.
                    if (route.getLocations() != null) {

                        // Get distance travelled since the last location
                        float distance = location.distanceTo(lastLocation);

                        // Calculate roughness vertical distance travelled in meters per horizontal
                        // distance travelled in kilometers. Add result to a list.
                        float roughness = verticalDistance / (distance / 1000F);
                        route.addRoughness(roughness);

                        // Reset verticalDistance
                        verticalDistance = 0;
                    }
                    else {
                        route.addRoughness(0);
                    }

                    // Add location to a list.
                    lastLocation = location;
                    route.addLocation(new double[] {location.getLatitude(), location.getLongitude()});
                    updateMap(new LatLng(location.getLatitude(), location.getLongitude()));
                }
            }
        }
    };
    private final int REQUEST_CODE_LOCATION = 1;
    private boolean locationPermissionGranted;

    private float currentSpeed = 0;

    // Sensors
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotation;
    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;

    private float[] accelData = {0,0,0};
    private float[] accelDataFiltered = {0,0,0};
    private float[] rotData;

    // Roughness calculation
    private float verticalDistance = 0;
    private long previousTime = 0;
    private float velocity = 0;

    // Route
    private Location lastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityRecordRouteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // fusedLocationProviderClient for getting location updates.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Make sure we have permission to get location updates.
        getLocationPermission();

        // Get sensors necessary for roughness calculation.
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Add toggle button to start/stop recording.
        ToggleButton buttonRecord = (ToggleButton) findViewById(R.id.buttonRecord);
        buttonRecord.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    start();
                } else {
                    stop();
                }
            }
        });
    }

    /**
     * Sets up sensor and location listeners which then begin recording data.
     * Called when the start button is pressed.
     */
    private void start() {
        route = new Route();
        setupSensorListener();

        if (locationPermissionGranted) {
            setupLocationUpdates();
        }
        else {
            getLocationPermission();
        }
    }

    /**
     * Terminates sensor and location updates.
     * Called when the stop button is pressed.
     */
    private void stop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, rotation);
        saveRoute();
    }

    private void saveRoute() {
        EditText routeName = new EditText(this);
        new AlertDialog.Builder(this).setTitle("Save Route").setView(routeName)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        AppDatabase db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "saved-routes").allowMainThreadQueries().build();
                        RouteDAO routeDAO = db.routeDAO();

                        String name = routeName.getText().toString();
                        if (name.isEmpty()) name = String.valueOf(System.currentTimeMillis()/1000);
                        route.setName(name);

                        routeDAO.insertRoute(route);

                        dialogInterface.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .show();
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
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(53, 6)));

        if (locationPermissionGranted) {
            mMap.setMyLocationEnabled(true);
        }
        else {
            getLocationPermission();
        }
    }

    private void setupLocationUpdates() {
        // Make a location updates request
        if (locationPermissionGranted) {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
        else {
            getLocationPermission();
        }
    }

    private void setupSensorListener() {
        // Register sensor listener
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
        }
        if (rotation != null) {
            sensorManager.registerListener(this, rotation, SENSOR_DELAY);
        }
    }

    private void updateMap(LatLng point) {
        // Add a line connecting to the next point on the route
        polylineOptions.add(point);
        mMap.addPolyline(polylineOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(point));
    }

    private void getLocationPermission() {
        // Check if location permission has already been granted. If not, request permission.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
        else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        }
    }

    /**
     * Permission request result callback
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode == REQUEST_CODE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // Get time interval since last sensor event
        long currentTime = event.timestamp;
        if (previousTime == 0) previousTime = event.timestamp;
        float interval = (currentTime - previousTime) / 1000000000f;
        previousTime = currentTime;

        if (currentSpeed == 0) return;

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            final float alpha = 0.8f;  // Low-pass filter constant

            // Low-pass filter
            accelData[0] = alpha * accelData[0] + (1 - alpha) * event.values[0];
            accelData[1] = alpha * accelData[1] + (1 - alpha) * event.values[1];
            accelData[2] = alpha * accelData[2] + (1 - alpha) * event.values[2];

            // High-pass filter
            accelDataFiltered[0] = event.values[0] - accelData[0];
            accelDataFiltered[1] = event.values[1] - accelData[1];
            accelDataFiltered[2] = event.values[2] - accelData[2];

        }
        else if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            rotData = event.values;
        }

        float vertAccel = 0;

        if (accelDataFiltered != null && rotData != null) {
            float[] rotMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotMatrix, rotData);
            float[] rotTrans = RRMath.transposeMatrix(rotMatrix);
            float[] worldAccel = RRMath.multiplyMatrix(accelDataFiltered, rotTrans);
            vertAccel = worldAccel[2];
        }

        if (vertAccel >= 0.1f || vertAccel <= -0.1f) {
            // Vertical velocity of device
            velocity = Math.abs(RRMath.calcVelocity(velocity, vertAccel, interval));

            // Accumulated vertical distance
            // This will be used in calculation of roughness
            verticalDistance += RRMath.calcDistance(velocity, vertAccel, interval);
        }
        else {
            velocity = 0;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure sensor and location updates are stopped before exiting the activity.
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, rotation);
    }
}