package com.droiddigger.mh_lushan.findlocation;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends FragmentActivity {

    private View mLayout;
    private int REQUEST_LOCATION_FINE = 1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

//        if (requestCode == REQUEST_LOCATION_FINE) {
//            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Snackbar.make(mLayout, "Permission Available",
//                        Snackbar.LENGTH_LONG).show();
//            } else {
//                Snackbar.make(mLayout, "permission was NOT granted",
//                        Snackbar.LENGTH_SHORT).setAction("OK", new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        ActivityCompat.requestPermissions(MainActivity.this,
//                                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                                REQUEST_LOCATION_FINE);
//                    }
//                }).show();
//
//            }
//
//        } else {
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("LSN", "Running Test");

        mLayout = findViewById(R.id.activity_main);

        GPSTracker tracker = new GPSTracker(this);
        Location location = tracker.getLocation(this);
        Toast.makeText(MainActivity.this, location + "", Toast.LENGTH_SHORT).show();

    }

    public class GPSTracker extends Service implements LocationListener {

        // Get Class Name
        private String TAG = com.droiddigger.mh_lushan.findlocation.MainActivity.class.getName();

        private final Context mContext;

        // flag for GPS Status
        boolean isGPSEnabled = false;

        // flag for network status
        boolean isNetworkEnabled = false;

        // flag for GPS Tracking is enabled
        boolean isGPSTrackingEnabled = false;

        Location location;
        double latitude;
        double longitude;

        // How many Geocoder should return our GPSTracker
        int geocoderMaxResults = 1;

        // The minimum distance to change updates in meters
        private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

        // The minimum time between updates in milliseconds
        private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute

        // Declaring a Location Manager
        protected LocationManager locationManager;

        // Store LocationManager.GPS_PROVIDER or LocationManager.NETWORK_PROVIDER information
        private String provider_info;

        public GPSTracker(Context context) {
            this.mContext = context;
//            AskPermission();
            getLocation(context);
            Log.d("LSN", " got it" + location);
        }

        /**
         * Try to get my current location by GPS or Network Provider
         */


//        public void AskPermission() {
//            if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                requestLocationPermission();
//
//            } else {
////            us.setMyLocationButtonEnabled(true);
////            mMap.setMyLocationEnabled(true);
//            }
//
//        }

        private void requestLocationPermission() {

            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(mLayout, "Need Location Permission to access your location",
                        Snackbar.LENGTH_LONG)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                        REQUEST_LOCATION_FINE);
                            }
                        })
                        .show();
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_FINE);
            }
        }

        public Location getLocation(Context context) {



            try {
                locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
                Log.d("LSN", "Location Manager: " + locationManager + "");
                //getting GPS status
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                //getting network status
                isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                // Try to get location if you GPS Service is enabled
                if (isGPSEnabled) {
                    this.isGPSTrackingEnabled = true;

                    Log.d(TAG, "Application use GPS Service");

                /*
                 * This provider determines location using
                 * satellites. Depending on conditions, this provider may take a while to return
                 * a location fix.
                 */

                    provider_info = LocationManager.GPS_PROVIDER;

                } else if (isNetworkEnabled) { // Try to get location if you Network Service is enabled
                    this.isGPSTrackingEnabled = true;

                    Log.d(TAG, "Application use Network State to get GPS coordinates");

                /*
                 * This provider determines location based on
                 * availability of cell tower and WiFi access points. Results are retrieved
                 * by means of a network lookup.
                 */
                    provider_info = LocationManager.NETWORK_PROVIDER;

                }

                // Application can use GPS or Network Provider

                if (!provider_info.isEmpty()) {
                    locationManager.requestLocationUpdates(
                            provider_info,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            this
                    );

                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(provider_info);
                        updateGPSCoordinates();
                        Toast.makeText(context, location.toString(), Toast.LENGTH_SHORT).show();
                    }
                }


            } catch (Exception e) {
                //e.printStackTrace();
                Log.e(TAG, "Impossible to connect to LocationManager", e);
            }
            return location;
        }

        /**
         * Update GPSTracker latitude and longitude
         */
        public void updateGPSCoordinates() {
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

            }
            Log.d("LSN", location.toString());
        }

        public double getLatitude() {
            if (location != null) {
                latitude = location.getLatitude();
            }

            return latitude;
        }

        public double getLongitude() {
            if (location != null) {
                longitude = location.getLongitude();
            }

            return longitude;
        }

        public boolean getIsGPSTrackingEnabled() {

            return this.isGPSTrackingEnabled;
        }


        public void showSettingsAlert() {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

            //Setting Dialog Title
            alertDialog.setTitle("GPSAlertDialogTitle");

            //Setting Dialog Message
            alertDialog.setMessage("GPSAlertDialogMessage");

            //On Pressing Setting button
            alertDialog.setPositiveButton("Okay", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    mContext.startActivity(intent);
                }
            });

            //On pressing cancel button
            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        }

        /**
         * Get list of address by latitude and longitude
         *
         * @return null or List<Address>
         */
        public List<Address> getGeocoderAddress(Context context) {
            if (location != null) {

                Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);

                try {
                    /**
                     * Geocoder.getFromLocation - Returns an array of Addresses
                     * that are known to describe the area immediately surrounding the given latitude and longitude.
                     */
                    List<Address> addresses = geocoder.getFromLocation(latitude, longitude, this.geocoderMaxResults);

                    return addresses;
                } catch (IOException e) {
                    //e.printStackTrace();
                    Log.e(TAG, "Impossible to connect to Geocoder", e);
                }
            }

            return null;
        }

        /**
         * Try to get AddressLine
         *
         * @return null or addressLine
         */
        public String getAddressLine(Context context) {
            List<Address> addresses = getGeocoderAddress(context);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressLine = address.getAddressLine(0);

                return addressLine;
            } else {
                return null;
            }
        }

        /**
         * Try to get Locality
         *
         * @return null or locality
         */
        public String getLocality(Context context) {
            List<Address> addresses = getGeocoderAddress(context);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String locality = address.getLocality();

                return locality;
            } else {
                return null;
            }
        }

        /**
         * Try to get Postal Code
         *
         * @return null or postalCode
         */
        public String getPostalCode(Context context) {
            List<Address> addresses = getGeocoderAddress(context);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String postalCode = address.getPostalCode();

                return postalCode;
            } else {
                return null;
            }
        }

        /**
         * Try to get CountryName
         *
         * @return null or postalCode
         */
        public String getCountryName(Context context) {
            List<Address> addresses = getGeocoderAddress(context);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String countryName = address.getCountryName();

                return countryName;
            } else {
                return null;
            }
        }

        @Override
        public void onLocationChanged(Location location) {
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

        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }
    }


}
