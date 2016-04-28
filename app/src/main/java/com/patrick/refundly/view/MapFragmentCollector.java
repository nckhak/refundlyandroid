package com.patrick.refundly.view;



import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.domain.Collection;
import com.patrick.refundly.domain.Notification;
import com.patrick.refundly.domain.User;
import com.patrick.refundly.model.MapFragmentController;



public class MapFragmentCollector extends Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMapClickListener {

    //Fragment logik
    private MapFragmentController model;


    //View for the map
    private View mapview;

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

    //Bruges til at indikere om brugeren har en aktiv opsamling
    private boolean hasActiveCollection = false;

    //Bruges således at kameraet kun rykker til collection markøren én gang.
    private boolean hasMovedCameraToCollection = false;

    private boolean notificationIntent = false;

    //variabler til PopupWindow
    private Marker mMarker;
    private PopupWindow mPopupWindow;
    private int mWidth;
    private int mHeight;
    private int container_height;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SupportMapFragment supportMapFragment = SupportMapFragment.newInstance();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.map, supportMapFragment).commit();
        supportMapFragment.getMapAsync(this);

        mapview = inflater.inflate(R.layout.fragment_map, container,false);


        //Opretter googleapiclient, hvis den ikke findes
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        model = new MapFragmentController(this);
        return mapview;
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(55.676097, 12.568337), 11));

        int ic_currentPosition = R.drawable.ic_currentmarker;
        MarkerOptions marker = new MarkerOptions().position(new LatLng(1, 1)).icon(BitmapDescriptorFactory.fromBitmap(model.Resize(ic_currentPosition)));
        mUserMarker = mMap.addMarker(marker);
        mUserMarker.setDraggable(false);
        mUserMarker.setVisible(false);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapClickListener(this);

        notificationIntent = getActivity().getIntent().getBooleanExtra("NotificationIntent", false);

        if (notificationIntent){
            model.AddCollectionMarker();
            LatLng collectionPosition = Controller.controller.getNotification().getPosition();
            mCollectionMarker.setPosition(collectionPosition);
            mCollectionMarker.setVisible(true);

            //Creating shared preferences
            final SharedPreferences mPrefs = getActivity().getSharedPreferences("PREFERENCE", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();
            Gson gson = new Gson();
            Notification obj = new Notification();

            //Creating notification object and assigning it to the object in the controller
            Notification notification = Controller.controller.getNotification();

            //fill our new object with data to be saved in SP
            obj.setBagcount(notification.getBagcount());
            obj.setLongtitude(notification.getLongtitude());
            obj.setLatitude(notification.getLatitude());
            obj.setPostercomment(notification.getPostercomment());
            obj.setAddress(notification.getAddress());
            obj.setCollectionId(notification.getCollectionId());
            obj.setDistance(notification.getDistance());

            //Parsing our object to a json string
            String json = gson.toJson(obj);
            System.out.println("JSON"+json);

            prefsEditor.putString("Collection", json);
            prefsEditor.commit();
            System.out.println("Collection saved in SP");

        }

    }



    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        model.UpdateMap();

    }

    //Når GoogleApiClient er connected
    @Override
    public void onConnected(Bundle bundle) {
        model.CreateLocationRequest();
        model.StartLocationUpdates();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getActivity(), "onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }

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
            model.StopLocationUpdates();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            model.StartLocationUpdates();
        }
        getActivity()
                .setTitle(R.string.app_name);
    }


    @Override
    public void onClick(View v) {
    }


    @Override
    public boolean onMarkerClick(Marker marker) {


        if(marker.equals(mCollectionMarker)){
            mPopupWindow = null;

            model.MoveMarkerToLowerScreen(marker);
            System.out.println("InfoWindow CREATED!");
            return true;
        }

        return false;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
    }

    @Override
    public void onMapClick(LatLng latLng) {
    }





    /*This section must not include anything
    * but setters and getters for the private
    * attributes.      /Peace be with you\   */


    public GoogleApiClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }


    public Location getmLastLocation() {
        return mLastLocation;
    }


    public LocationRequest getmLocationRequest() {
        return mLocationRequest;
    }

    public void setmLocationRequest(LocationRequest mLocationRequest) {
        this.mLocationRequest = mLocationRequest;
    }

    public GoogleMap getmMap() {
        return mMap;
    }

    public View getMapview() {
        return mapview;
    }

    public void setMapview(View mapview) {
        this.mapview = mapview;
    }

    public Marker getmUserMarker() {
        return mUserMarker;
    }

    public Marker getmCollectionMarker() {
        return mCollectionMarker;
    }

    public void setmCollectionMarker(Marker maker){
        this.mCollectionMarker = maker;
    }

    public int getmMarkerWidth() {
        return mMarkerWidth;
    }

    public double getmMarkerratio() {
        return mMarkerratio;
    }

    public boolean isHasCenteredCamera() {
        return hasCenteredCamera;
    }

    public void setHasCenteredCamera(boolean hasCenteredCamera) {
        this.hasCenteredCamera = hasCenteredCamera;
    }

    public void setHasActiveCollection(boolean hasActiveCollection){
        this.hasActiveCollection = hasActiveCollection;
    }

    public boolean HasActiveCollection(){
        return hasActiveCollection;
    }

    public int getmHeight() {
        return mHeight;
    }

    public void setmHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public int getmWidth() {
        return mWidth;
    }

    public void setmWidth(int mWidth) {
        this.mWidth = mWidth;
    }

    public int getContainer_height() {
        return container_height;
    }

    public void setContainer_height(int container_height) {
        this.container_height = container_height;
    }

    public PopupWindow getmPopupWindow() {
        return mPopupWindow;
    }

    public void setmPopupWindow(PopupWindow mPopupWindow) {
        this.mPopupWindow = mPopupWindow;
    }

    public Marker getmMarker() {
        return mMarker;
    }

    public void setmMarker(Marker mMarker) {
        this.mMarker = mMarker;
    }

    public boolean HasMovedCameraToCollection() {
        return hasMovedCameraToCollection;
    }

    public void setHasMovedCameraToCollection(boolean hasMovedCameraToCollection) {
        this.hasMovedCameraToCollection = hasMovedCameraToCollection;
    }

    public boolean isNotificationIntent() {
        return notificationIntent;
    }


}
