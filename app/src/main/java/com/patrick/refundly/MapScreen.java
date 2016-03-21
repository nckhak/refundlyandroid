package com.patrick.refundly;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapScreen extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;
    private Marker mUserMarker, mCollectionMarker;
    private int mMarkerWidth = 200;
    private double mMarkerratio = 1.2027;
    private boolean hasCenteredCamera = false;
    private boolean hasCollection = true;
    Collection collection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_screen);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        //TIL TEST AF COLLECTION MARKER!
        collection = new Collection();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Fejl: onMapReady - kunne ikke få adgang til user permissions");
            return;
        }
        //mMap.setMyLocationEnabled(true);

        MarkerOptions marker = new MarkerOptions().position(new LatLng(1,1)).icon(BitmapDescriptorFactory.fromBitmap(resize(R.drawable.collector)));
        mUserMarker = mMap.addMarker(marker);
        mUserMarker.setVisible(false);

        if(hasCollection) {
            MarkerOptions collectionMarker = new MarkerOptions().position(collection.getPosition()).icon(BitmapDescriptorFactory.fromBitmap(resize(R.drawable.collection)));
            mCollectionMarker = mMap.addMarker(collectionMarker);
            mCollectionMarker.setVisible(false);
        }
        createLocationRequest();


    }

    private Bitmap resize(int image) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), image);
        Bitmap b = Bitmap.createScaledBitmap(original, mMarkerWidth, (int) (mMarkerWidth * mMarkerratio), false);
        return b;
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateMap();

    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        hasCenteredCamera = false;
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    private void updateMap(){
        double dLatitude = mLastLocation.getLatitude();
        double dLongitude = mLastLocation.getLongitude();

        updateMapMarkers(new LatLng(dLatitude,dLongitude));
        if(!hasCenteredCamera) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 15));
            hasCenteredCamera = true;
        }
    }

    private void updateMapMarkers(LatLng latlng){
        mUserMarker.setPosition(latlng);
        mUserMarker.setVisible(true);

        if(hasCollection) {
            mCollectionMarker.setPosition(collection.getPosition());
            mCollectionMarker.setVisible(true);
        }
    }
}
