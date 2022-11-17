package com.bios.walkietalkie2.utils

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.location.Location
import android.os.Build
import android.view.Surface
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class CompassSensorEventListener(
    private val activity: AppCompatActivity,
    private val locationFrom: () -> Location?,
    private val locationTo: () -> Location?,
    private val onRotationChanged: (Float) -> Unit
) : SensorEventListener {

    private val sensorManager by lazy { activity.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometerSensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private val magneticFieldSensor by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }
    private var geoMagneticField: GeomagneticField? = null
    private var accelerometerData = FloatArray(3)
    private var magneticData = FloatArray(3)
    private var temporaryRotationMatrix = FloatArray(9)
    private var rotationMatrix = FloatArray(9)
    private var orientationData = FloatArray(3)
    private var azimuth = 0f
    private val DEGREES_360 = 360

    init {
        activity.addLifeCycleObserver(
            onResume = { startListening() }
        )
    }

    private fun startListening() {
        sensorManager.registerListener(
            this,
            accelerometerSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager.registerListener(
            this,
            magneticFieldSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val sensorType = event?.sensor?.type ?: return
        when (sensorType) {
            Sensor.TYPE_ACCELEROMETER -> {
                accelerometerData = event.values
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                magneticData = event.values
            }
        }

        SensorManager.getRotationMatrix(temporaryRotationMatrix, null, accelerometerData, magneticData)
        countDeviceAngle()
        SensorManager.getOrientation(rotationMatrix, orientationData)
        azimuth = Math.toDegrees(orientationData[0].toDouble()).toFloat()

        val locationFrom = locationFrom.invoke() ?: return
        val locationTo = locationTo.invoke() ?: return

        geoMagneticField = GeomagneticField(
            locationFrom.latitude.toFloat(),
            locationFrom.longitude.toFloat(),
            locationFrom.altitude.toFloat(),
            System.currentTimeMillis()
        )
        azimuth -= geoMagneticField!!.declination
        var bearTo = locationFrom.bearingTo(locationTo)
        if (bearTo < 0) {
            bearTo = bearTo + DEGREES_360
        }

        var rotation = bearTo - azimuth
        if (rotation < 0) {
            rotation = rotation + DEGREES_360
        }
        onRotationChanged.invoke(rotation)
        /*
        *      GeomagneticField geomagneticField = new GeomagneticField(
                (float) userLocation.getLatitude(),
                (float) userLocation.getLongitude(),
                (float) userLocation.getAltitude(), System.currentTimeMillis());

        float azimuth = compassSensorManager.getAzimuth();
        azimuth -= geomagneticField.getDeclination();

        float bearTo = userLocation.bearingTo(objectLocation);
        if (bearTo < 0) bearTo = bearTo + DEGREES_360;

        float rotation = bearTo - azimuth;
        if (rotation < 0) rotation = rotation + DEGREES_360;

        rotateImageView(this, drawableResource, rotation);
        *
        * */
    }

    private fun countDeviceAngle() {
        when (getDisplay()?.rotation) {
            Surface.ROTATION_0 ->
                SensorManager.remapCoordinateSystem(
                    temporaryRotationMatrix,
                    SensorManager.AXIS_Z,
                    SensorManager.AXIS_Y,
                    rotationMatrix
                )
            Surface.ROTATION_90 -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_Y,
                SensorManager.AXIS_MINUS_Z,
                rotationMatrix
            )
            Surface.ROTATION_180 -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_MINUS_Z,
                SensorManager.AXIS_MINUS_Y,
                rotationMatrix
            )
            Surface.ROTATION_270 -> SensorManager.remapCoordinateSystem(
                temporaryRotationMatrix,
                SensorManager.AXIS_MINUS_Y,
                SensorManager.AXIS_Z,
                rotationMatrix
            )
        }
    }


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) = Unit

    private fun getDisplay() =
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            activity.display
        } else {
            activity.windowManager.defaultDisplay
        }
}
