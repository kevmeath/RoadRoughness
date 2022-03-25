package km.roadroughness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Road Roughness");
    }

    /** Called when the user taps the Live Reading button */
    public void liveReading(View view) {
        // Start live reading activity
        Intent intent = new Intent(this, LiveReadingActivity.class);
        startActivity(intent);
    }
    /** Called when the user taps the Record Route button */
    public void recordRoute(View view) {
        // Start record route activity
        Intent intent = new Intent(this, RecordRouteActivity.class);
        startActivity(intent);
    }
    /** Called when the user taps the View Map button */
    public void viewMap(View view) {
        // Start view map activity
    }
}