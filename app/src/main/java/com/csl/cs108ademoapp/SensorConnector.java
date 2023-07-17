package com.csl.cs108ademoapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

import com.csl.cs108library4a.Cs108Library4A;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.SENSOR_SERVICE;

public class SensorConnector {
    final boolean DEBUG = false;

    public String getTimeStamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return sdf.format(new Date());
    }

    public LocationDevice mLocationDevice;
    public SensorDevice mSensorDevice;

    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private Cs108Library4A mCsLibrary4A = MainActivity.csLibrary4A;

    SensorConnector(Context context) {
        mCsLibrary4A = MainActivity.csLibrary4A;
        mLocationDevice = new LocationDevice(context);
        mSensorDevice = new SensorDevice(context);
    }

    public class LocationDevice {
        private Context mContext;
        private LocationManager locationManager;
        private Location location;

        public String getLocation() {
            if (location != null) {
                return Location.convert(location.getLatitude(), Location.FORMAT_DEGREES) + ", " + Location.convert(location.getLongitude(), Location.FORMAT_DEGREES);
            } else {
                return "";
            }
        }

        private boolean onStatus = false;

        LocationDevice(Context context) {
            mContext = context;

            PackageManager mPackageManager;
            mPackageManager = (PackageManager) context.getPackageManager();
            if (!(mPackageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION))) {
                if (DEBUG) mCsLibrary4A.appendToLog("there is NO Feature_Location");
                Toast.makeText(context.getApplicationContext(), "there is NO LOCATION_FEATURE in this phone !!! Please use another phone.", Toast.LENGTH_LONG).show();
            } else locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        }

        public void turnOn(boolean onStatus) {
            if (locationManager != null) {
                if (true) mCsLibrary4A.appendToLog("permission.ACCESS_FINE_LOCATION = " + ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION));
                if (true) mCsLibrary4A.appendToLog("permission.ACCESS_COARSE_LOCATION = " + ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION));
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(mContext.getApplicationContext(), "LOCATION_FEATURE permission is NOT GRANTED in this phone !!! Please go to Phone Setup and enable Location Services and Relaunch.", Toast.LENGTH_SHORT).show();
                } else if (onStatus && this.onStatus == false) {
                    this.onStatus = onStatus;
                    if (true) mCsLibrary4A.appendToLog("LocationDevice.setRfidOn(): ON with LocationManager: ON");
                    if (true) mCsLibrary4A.appendToLog("LocationManager.PASSIVE_PROVIDER = " + LocationManager.PASSIVE_PROVIDER);
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                } else if (onStatus == false && this.onStatus) {
                    this.onStatus = onStatus;
                    if (DEBUG) mCsLibrary4A.appendToLog("LocationDevice.setRfidOn(): OFF");
                    locationManager.removeUpdates(locationListener);
                }
            }
        }

        private LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (true) mCsLibrary4A.appendToLog("onLocationChanged(): " + location.getProvider());
                location = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }

    public class SensorDevice {
        private Context mContext;
        private SensorManager mSensorManager;
        private float[] mOrientation = new float[3];

        public String getEcompass() {
            String s0, s1, s2;
            synchronized (mOrientation) {
                float azimuthInRadians = mOrientation[0];
                if (azimuthInRadians == 0) return null;
                float azimuthInDegress = (float) (Math.toDegrees(azimuthInRadians) + 360) % 360;
                s0 = String.format("%.1f", azimuthInDegress);
            }
            return s0;  // + ", " + s1 + ", " + s2;
        }

        private boolean onStatus = false;

        SensorDevice(Context context) {
            mContext = context;

            mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
            List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            if (DEBUG) mCsLibrary4A.appendToLog("SensorDevice(): List of sensors");
            for (int i = 0; i < deviceSensors.size(); i++) {
                if (DEBUG) mCsLibrary4A.appendToLog(deviceSensors.get(i).getType() + "," + deviceSensors.get(i).getName());
            }
        }

        public void turnOn(boolean onStatus) {
            if (onStatus && this.onStatus == false) {
                Sensor mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                Sensor mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                if (mSensorManager.registerListener(sensorEventListener, mAccelerometer, SensorManager.SENSOR_DELAY_UI) == false) {
                    Toast.makeText(mContext.getApplicationContext(), "ACCELEROMETER is NOT supported in this phone !!! Please use another phone.", Toast.LENGTH_LONG).show();
                    mAccelerometer = null;
                }
                if (mSensorManager.registerListener(sensorEventListener, mMagnetometer, SensorManager.SENSOR_DELAY_UI) == false) {
                    if (false) Toast.makeText(mContext.getApplicationContext(), "MAGNETOMETER is NOT supported in this phone !!! Please use another phone.", Toast.LENGTH_LONG).show();
                    mMagnetometer = null;
                }
                if (mAccelerometer == null || mMagnetometer == null) {
                    mSensorManager.unregisterListener(sensorEventListener);
                } else {
                    this.onStatus = onStatus;
                    if (DEBUG) mCsLibrary4A.appendToLog("SensorDevice.setRfidOn(): ON");
                }
            } else if (this.onStatus && onStatus == false) {
                this.onStatus = onStatus;
                if (DEBUG) mCsLibrary4A.appendToLog("SensorDevice.setRfidOn(): OFF");
                mSensorManager.unregisterListener(sensorEventListener);
            }
        }

        private SensorEventListener sensorEventListener = new SensorEventListener() {
            private float[] mLastAccelerometer = new float[3];
            private boolean mLastAccelerometerSet = false;
            private float[] mLastMagnetometer = new float[3];
            private boolean mLastMagnetometerSet = false;

            private float[] mR = new float[9];

            @Override
            public void onSensorChanged(SensorEvent event) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    if (event.values.length == mLastAccelerometer.length) {
                        System.arraycopy(event.values, 0, mLastAccelerometer, 0, mLastAccelerometer.length);
                        mLastAccelerometerSet = true;
//                    appendToLog("onSensorChanged(): updated mAccelerometer");
                    } else {
                        if (DEBUG) mCsLibrary4A.appendToLog("onSensorChanged(): mAccelerometer: " + event.values.length);
                    }
                } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                    if (event.values.length == mLastMagnetometer.length) {
                        System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                        mLastMagnetometerSet = true;
//                    appendToLog("onSensorChanged(): updated mMagnetometer");
                    } else {
                        if (DEBUG) mCsLibrary4A.appendToLog("onSensorChanged(): mMagnetometer: " + event.values.length);
                    }
                } else {
                    if (DEBUG) mCsLibrary4A.appendToLog("onSensorChanged(): " + event.sensor.getType() + "," + event.sensor.getName());
                }

                if (mLastAccelerometerSet && mLastMagnetometerSet) {
                    SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
                    synchronized (mOrientation) {
                        SensorManager.getOrientation(mR, mOrientation);
                    }
                    mLastAccelerometerSet = false;
                    mLastMagnetometerSet = false;
                    if (DEBUG) mCsLibrary4A.appendToLog("onSensorChanged(): updated mOrientation with mOrientation=" + mOrientation[0]);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
    }
}