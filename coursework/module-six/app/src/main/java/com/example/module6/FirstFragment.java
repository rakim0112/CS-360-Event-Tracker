
package com.example.module6;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

public class FirstFragment extends Fragment
        implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private TextView sensorStatus;
    private TextView xValue;
    private TextView yValue;
    private TextView zValue;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(
                R.layout.fragment_first, container, false);

        sensorStatus = view.findViewById(R.id.sensorStatus);
        xValue = view.findViewById(R.id.xValue);
        yValue = view.findViewById(R.id.yValue);
        zValue = view.findViewById(R.id.zValue);

        // Obtain the Android SensorManager service.
        sensorManager = (SensorManager)
                requireContext().getSystemService(
                        Context.SENSOR_SERVICE);

        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(
                    Sensor.TYPE_ACCELEROMETER);
        }

        if (accelerometer == null) {
            sensorStatus.setText(
                    "Accelerometer not available");
        } else {
            sensorStatus.setText(
                    "Accelerometer connected");
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Start reading sensor data when screen is active.
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(
                    this,
                    accelerometer,
                    SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onPause() {
        // Stop listening to save device resources.
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }

        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType()
                != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        // Read acceleration on the three axes.
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // Display updated readings on the screen.
        xValue.setText(String.format(
                Locale.US, "X: %.2f m/s²", x));

        yValue.setText(String.format(
                Locale.US, "Y: %.2f m/s²", y));

        zValue.setText(String.format(
                Locale.US, "Z: %.2f m/s²", z));
    }

    @Override
    public void onAccuracyChanged(
            Sensor sensor, int accuracy) {
        // No special accuracy handling required.
    }
}
