package com.bios.walkietalkie2.utils

import android.content.Context.SENSOR_SERVICE
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity

class LocationPointerHelper(
    private val activity: AppCompatActivity
) {


//    private val sensorManger by lazy { activity.getSystemService(SENSOR_SERVICE) as SensorManager }
//    private fun startListening() {
//        sensorManger.registerListener(
//            object : SensorEventListener {
//                override fun onSensorChanged(sensorEvent: SensorEvent?) {
//                    sensorEvent?.let(::onSensorChanged)
//                }
//
//                override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit
//            },
//            sensorManger.getDefaultSensor(Sensor.TYPE_ORIENTATION),
//            SensorManager.SENSOR_DELAY_GAME
//
//
//        )
//    }
//
//    private fun onSensorChanged(event: SensorEvent) {
//
//    }
    /*
    * mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
    *
    * */
}

/*
*
* @Override
    public void onSensorChanged(SensorEvent event) {
        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);
        degree += geoField.getDeclination();

        float bearing = location.bearingTo(target);
        degree = (bearing - degree) * -1;
        degree = normalizeDegree(degree);

        tvHeading.setText("Heading: " + Float.toString(degree) + " degrees");

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        image.startAnimation(ra);
        currentDegree = -degree;
    }

private float normalizeDegree(float value) {
        if (value >= 0.0f && value <= 180.0f) {
            return value;
        } else {
            return 180 + (180 + value);
        }
    }
* */

