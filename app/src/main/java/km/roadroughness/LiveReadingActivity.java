package km.roadroughness;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import km.roadroughness.util.RRMath;

public class LiveReadingActivity extends AppCompatActivity implements SensorEventListener, OnChartValueSelectedListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor rotation;

    private static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;

    private LineChart chart;

    private float[] accelData = {0,0,0};
    private float[] rotData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_reading);
        setTitle("Sensor Chart");

        // Get sensors
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotation = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        // Register sensor listeners
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
        }
        if (rotation != null) {
            sensorManager.registerListener(this, rotation, SENSOR_DELAY);
        }

        // Set up chart
        chart = findViewById(R.id.chart);
        chart.setOnChartValueSelectedListener(this);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        chart.setData(data);

        Legend legend = chart.getLegend();

        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextColor(Color.WHITE);

        XAxis x = chart.getXAxis();
        x.setTextColor(Color.WHITE);
        x.setDrawGridLines(false);
        x.setAvoidFirstLastClipping(true);
        x.setEnabled(true);

        YAxis y = chart.getAxisLeft();
        y.setTextColor(Color.WHITE);
        y.setAxisMaximum(10f);
        y.setAxisMinimum(-10f);
        y.setDrawGridLines(true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void addEntry(float value, String label) {
        LineData data = chart.getData();

        if (data != null) {
            ILineDataSet set = data.getDataSetByLabel(label, true);

            if (set == null) {
                set = createSet(label);
                data.addDataSet(set);
            }

            data.addEntry(new Entry(set.getEntryCount(), value), 0);
            data.notifyDataChanged();

            chart.notifyDataSetChanged();
            chart.setVisibleXRangeMaximum(120);
            chart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet(String label) {
        LineDataSet set = new LineDataSet(null, label);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }

    private float[] accelDataFiltered = {0,0,0};
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
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

        float vertAccel;

        if (accelDataFiltered != null && rotData != null) {
            float[] rotMatrix = new float[9];
            SensorManager.getRotationMatrixFromVector(rotMatrix, rotData);
            float[] rotTrans = RRMath.transposeMatrix(rotMatrix);
            float[] worldAccel = RRMath.multiplyMatrix(accelDataFiltered, rotTrans);
            vertAccel = worldAccel[2];
            addEntry(vertAccel, getString(R.string.data_vertAccel));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, accelerometer);
        sensorManager.unregisterListener(this, rotation);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {}

    @Override
    public void onNothingSelected() {}
}