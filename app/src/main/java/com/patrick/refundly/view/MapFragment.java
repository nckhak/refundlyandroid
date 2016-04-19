package com.patrick.refundly.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.domain.CreateCollection;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MapFragment extends android.support.v4.app.Fragment implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnCameraChangeListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener {

    private ImageButton imageButton;
    private ImageButton btnBottle1;
    private ImageButton btnBottle2;
    private ImageButton btnBottle3;
    private EditText popupComment;

    private Marker currentMarker;

    //View for the map
    private View mapview;

    //Variabler til google location services
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private LocationRequest mLocationRequest;

    //Variabler til mapvisning og markører
    private GoogleMap mMap;
    private Marker mUserMarker;

    //variabler til skalering af ikoner på map
    private final int mMarkerWidth = 200;
    private final double mMarkerratio = 1.2027;

    //Viser om appen har centreret kameraet (bliver sat til true efter første gang)
    //Ellers ville appen centrere kamera hver gang gps position opdateres
    private boolean hasCenteredCamera = false;




    //variabler til Pop-window for Posteren
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


        MarkerOptions marker = new MarkerOptions().position(new LatLng(1, 1)).icon(BitmapDescriptorFactory.fromBitmap(resize(R.drawable.collector)));
        mUserMarker = mMap.addMarker(marker);
        mUserMarker.setDraggable(true);
        mUserMarker.setVisible(false);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnCameraChangeListener(this);
        mMap.setOnMapClickListener(this);
        mMap.setOnMarkerDragListener(this);


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

    }



    @Override
    public void onClick(View v) {
//        if (v == popupComment) {
//            Context context = getContext();
//            CharSequence text = "OnFieldClick!";
//            int duration = Toast.LENGTH_SHORT;
//
//            Toast toast = Toast.makeText(context, text, duration);
//            toast.show();
//        }


        switch(v.getId()){

            case R.id.btnBottle1:
                CreateCollection(1);
                break;

            case R.id.btnBottle2:
                CreateCollection(2);
                break;

            case R.id.btnBottle3:
                CreateCollection(3);
                break;
        }

    }

    public void CreateCollection(int size){

        LatLng position = currentMarker.getPosition();
        String comment = popupComment.getText().toString();


        int posterId = Controller.controller.getUser().getId();
        double latitude = position.latitude;
        double longitude = position.longitude;
        int collectionSize = size;
        String posterComment = (comment.isEmpty() || comment == null) ? "(No%20comment)" : comment.replace(" ", "%20");

        Controller.controller.setNewCollection(posterId, latitude, longitude, collectionSize, posterComment);

        System.out.println("Creating collection");
        final Object[] result = new Object[1];

        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                /*CreateCollection_BG();*/
                if(CreateCollection_BG()){
                    System.out.println("BG TRUE");
                    result[0] = "Done";
                }else{
                    System.out.println("BG FALSE");
                    result[0] = "";
                }
                System.out.println("Result = " + result[0].toString());
                return result;
            }

            //Kører når baggrundstråden er færdig
            @Override
            protected void onPostExecute(Object o) {
                String res = result[0].toString();
                System.out.println("Result Exe = " +res);

                Context context = getContext();
                int duration = Toast.LENGTH_SHORT;
                CharSequence text;

                if (res.equals("Done")){
                    text = "Collection created successfully!";
                    System.out.println(text);
                }else{
                    text = "An error occured!";
                    System.out.println(text);
                }

                mPopupWindow.dismiss();
                mPopupWindow = null;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }

        }.execute();

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        //Used to create a collection later on
        currentMarker = marker;

        mPopupWindow = null;

        MoveMarkerToLowerScreen(marker);
        System.out.println("POPUP CREATED!");
        return true;

    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
//        if (mPopupWindow != null){
//            mPopupWindow.dismiss();
//            mPopupWindow = null;
//        }
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mPopupWindow = null;
        System.out.println("ON MAP CLICKED");
    }

    public void MoveMarkerToLowerScreen(final Marker marker){
        currentMarker = marker;
        container_height = mapview.getHeight();

        Projection projection = mMap.getProjection();

        LatLng markerLatLng = new LatLng(marker.getPosition().latitude,
                marker.getPosition().longitude);

        Point markerScreenPosition = projection.toScreenLocation(markerLatLng);

        Point pointHalfScreenAbove = new Point(markerScreenPosition.x,
                markerScreenPosition.y - (container_height / 10));

        LatLng aboveMarkerLatLng = projection
                .fromScreenLocation(pointHalfScreenAbove);

        CameraUpdate center = CameraUpdateFactory.newLatLng(aboveMarkerLatLng);
        mMap.animateCamera(center, 500, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                MarkerActionClick(marker);
            }

            @Override
            public void onCancel() {
            }
        });


    }

    private void MarkerActionClick(Marker marker){
        View popupView = getLayoutInflater(null).inflate(R.layout.popup_layout, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.update();

        popupComment = (EditText) popupView.findViewById(R.id.userComment);
        btnBottle1 = (ImageButton) popupView.findViewById(R.id.btnBottle1);
        btnBottle2 = (ImageButton) popupView.findViewById(R.id.btnBottle2);
        btnBottle3 = (ImageButton) popupView.findViewById(R.id.btnBottle3);


        btnBottle1.setOnClickListener(this);
        btnBottle2.setOnClickListener(this);
        btnBottle3.setOnClickListener(this);

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        popupView.measure(size.x, size.y);

        mWidth = popupView.getMeasuredWidth();
        mHeight = popupView.getMeasuredHeight();
        mMarker = marker;
        mPopupWindow = popupWindow;

        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);

        mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


        updatePopup();
    }

    private void updatePopup() {
        if (mMarker != null && mPopupWindow != null) {
            // marker is visible
            if (mMap.getProjection().getVisibleRegion().latLngBounds.contains(mMarker.getPosition())) {
                if (!mPopupWindow.isShowing()) {
                    mPopupWindow.showAtLocation(getView(), Gravity.NO_GRAVITY, 0, 0);
                }
                Point p = mMap.getProjection().toScreenLocation(mMarker.getPosition());
                mPopupWindow.update(p.x - mWidth / 2, p.y - mHeight, -1, -1);

            } else { // marker outside screen
                mPopupWindow.dismiss();
            }
        }
    }

    //Opret opsamlingsted skal køres i tråd
    public boolean CreateCollection_BG(){

        CreateCollection collection = Controller.controller.getCurrentCollection();

        int posterId = collection.getPosterId();
        double latitude = collection.getLatitude();
        double longitude = collection.getLongitude();
        int collectionSize = collection.getCollectionSize();
        String posterComment = collection.getPosterComment();

        System.out.println(posterId + " " + latitude + " " + longitude + " " + collectionSize + " " + posterComment);

        JSONObject object;
        String urlString = "http://refundlystaging.azurewebsites.net/api/collection/CreateCollection?" +
                            "posterId=" +posterId+ "&latitude=" +latitude+ "&longitude=" +longitude+
                            "&collectionSize=" +collectionSize+ "&posterComment=" +posterComment;


        try{

            URL url = new URL(urlString.toString());
            System.out.println("Opretter forbindelse til API");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            System.out.println("Forbindelse oprettet");

            int sc = con.getResponseCode();

            System.out.println("SC: "+ sc);

            if(sc==200) {
                InputStream is = con.getInputStream();
                object = new JSONObject(readStringAndClose(is));
                System.out.println("----------------");
                System.out.println(object.toString());

                //Tjek for fejl fra serveren
                if ((boolean) object.get("Error") == true) {
                    System.out.println("Json error er true");
                    return false;
                }
                else if ((boolean) object.get("Error") == false) {
                    return true;
                }

                return false;

            }else{
                System.out.println("Server returnerede fejl: " + sc);
                return false;
            }

        }catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            System.out.println("JSONException");
            e.printStackTrace();
            return false;
        }

    }


    //Kode fra AndroidElementer - læser data fra google api
    public static String readStringAndClose(InputStream is)
            throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while((len= is.read(data,0,data.length))>=0){
            bos.write(data);
        }
        is.close();
        System.out.println("--------READINPUTSTREAM--------");
        System.out.println(data);
        System.out.println(new String(bos.toByteArray(), "UTF-8"));
        System.out.println("----------------");
        return new String(bos.toByteArray(),"UTF-8");
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        stopLocationUpdates();
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }
}
