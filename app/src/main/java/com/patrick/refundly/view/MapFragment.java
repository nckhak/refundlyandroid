package com.patrick.refundly.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
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
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;

public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    Button newCollectionBtn;

    //Variabler til google location services
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    //Variabler til mapvisning og markører
    private GoogleMap mMap;
    private Marker mUserMarker, mCollectionMarker;

    //variabler til skalering af ikoner på map
    private final int mMarkerWidth = 200;
    private final double mMarkerratio = 1.2027;

    //Viser om appen har centreret kameraet (bliver sat til true efter første gang)
    //Ellers ville appen centrere kamera hver gang gps position opdateres
    private boolean hasCenteredCamera = false;

    //Har brugeren adgang til en opsamling
    private boolean hasCollection = true;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);

        View mapview = inflater.inflate(R.layout.fragment_map, container,false);
        newCollectionBtn = (Button) mapview.findViewById(R.id.btnFollow);
        newCollectionBtn.setOnClickListener(this);

        //Opretter googleapiclient, hvis den ikke findes
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        return mapview;
    }

    /*
    Køres når kortet er indlæst fra google.
    Sætter markers for poster og collector.

    Der bør senere laves et tjek for at kunne differentiere mellem posters og collectors
    De skal ikke kunne se de samme funktioner
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.676097, 12.568337), 11));
        /*
        //Udkommenteret for at fjerne den blå prik der viser current position.
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("Fejl: onMapReady - kunne ikke få adgang til user permissions");
            return;
        }
        mMap.setMyLocationEnabled(true);
        */


        MarkerOptions marker = new MarkerOptions().position(new LatLng(1,1)).icon(BitmapDescriptorFactory.fromBitmap(resize(R.drawable.collector)));
        mUserMarker = mMap.addMarker(marker);
        mUserMarker.setDraggable(true);
        mUserMarker.setVisible(false);

        if(hasCollection) {
            MarkerOptions collectionMarker = new MarkerOptions().position(Controller.controller.getCollection().getPosition()).icon(BitmapDescriptorFactory.fromBitmap(resize(R.drawable.collection)));
            mCollectionMarker = mMap.addMarker(collectionMarker);
            mCollectionMarker.setVisible(false);
        }
    }

    //Bruges til at ændre størrelsen på mapmarkers
    private Bitmap resize(int image) {
        Bitmap original = BitmapFactory.decodeResource(getResources(), image);
        Bitmap b = Bitmap.createScaledBitmap(original, mMarkerWidth, (int) (mMarkerWidth * mMarkerratio), false);
        return b;
    }

    //Laver en forespørgsel til google om at få sidst kendte position.
    //Svar modtages i onLocationChanged
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        //Antal millisekunder der ønskes mellem gps opdateringer
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Starter konstant opdatering af position
    protected void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    //Stopper konstant opdatering af position
    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }

    //Trigger ved ændring af gps position
    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        updateMap();

    }

    //Når GoogleApiClient er connected
    @Override
    public void onConnected(Bundle bundle) {
        createLocationRequest();
        startLocationUpdates();
    }

    //Hvis forbindelsen til GoogleApiClient stoppes (Eller måske internet, who knows?)
    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

    //Hvis der ikke kan oprettes forbindelse til GoogleApiClient (eller internet,måske)
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(getActivity(), "onConnectionFailed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        hasCenteredCamera = false;
        super.onStart();
    }

    @Override
    public void onStop() {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
        getActivity()
                .setTitle(R.string.app_name);
    }

    //Opdaterer markører og kamera på kortet
    private void updateMap(){
        double dLatitude = mLastLocation.getLatitude();
        double dLongitude = mLastLocation.getLongitude();

        updateMapMarkers(new LatLng(dLatitude, dLongitude));
        if(!hasCenteredCamera) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 15));
            hasCenteredCamera = true;
        }
    }

    //Opdaterer markører på kort
    private void updateMapMarkers(LatLng latlng){
        mUserMarker.setPosition(latlng);
        mUserMarker.setVisible(true);

        if(hasCollection) {
            mCollectionMarker.setPosition(Controller.controller.getCollection().getPosition());
            mCollectionMarker.setVisible(true);
        }
    }

    @Override
    public void onClick(View v) {
        if(v==newCollectionBtn){
            System.out.println("loltrold!");
        }
    }
}
