package com.patrick.refundly.model;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.view.MapFragmentCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Khaled on 17-04-2016.
 */
public class MapFragmentController {

    //Fragment
    private MapFragmentCollector fragment;

    public MapFragmentController(){

    }

    public MapFragmentController(MapFragmentCollector fragment){
        this.fragment = fragment;

    }

    public Bitmap Resize(int image) {
        System.out.println("Resizing: " + image);
        Bitmap original = BitmapFactory.decodeResource(fragment.getResources(), image);
        Bitmap b = Bitmap.createScaledBitmap(original, fragment.getmMarkerWidth(), (int) (fragment.getmMarkerWidth() * fragment.getmMarkerratio()), false);
        return b;
    }

    public void FindRouteByGoogleMaps(LatLng destination){
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                Uri.parse("http://maps.google.com/maps?" +
                        "daddr=" + destination.latitude + "," + destination.longitude));
        getActivity().startActivity(intent);
    }

    //Laver en forespørgsel til google om at få sidst kendte position.
    //Svar modtages i onLocationChanged
    public void CreateLocationRequest() {
        fragment.setmLocationRequest(new LocationRequest());

        //Antal millisekunder der ønskes mellem gps opdateringer
        fragment.getmLocationRequest().setInterval(10000);
        fragment.getmLocationRequest().setFastestInterval(5000);
        fragment.getmLocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    //Starter konstant opdatering af position
    public void StartLocationUpdates() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        System.out.println("Requesting location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                fragment.getmGoogleApiClient(), fragment.getmLocationRequest(), fragment);
        System.out.println("Done");
    }


    //Opdaterer markører og kamera på kortet
    public void UpdateMap() {
        double dLatitude = fragment.getmLastLocation().getLatitude();
        double dLongitude = fragment.getmLastLocation().getLongitude();

        UpdateMapMarkers(new LatLng(dLatitude, dLongitude));

        if(fragment.isNotificationIntent() && !fragment.HasMovedCameraToCollection()){
            double latitude = Controller.controller.getNotification().getLatitude();
            double longtitude = Controller.controller.getNotification().getLongtitude();
            LatLng position = new LatLng(latitude, longtitude);
            fragment.getmMap().animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
            fragment.setHasMovedCameraToCollection(true);
            return;

        }else if (!fragment.isNotificationIntent() && !fragment.isHasCenteredCamera()) {
            fragment.getmMap().animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(dLatitude, dLongitude), 15));
            fragment.setHasCenteredCamera(true);
        }
/*
        if(fragment.isNotificationIntent()  && !fragment.HasMovedCameraToCollection()){
            Marker marker = fragment.getmCollectionMarker();
            LatLng position = marker.getPosition();

            fragment.getmMap().animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(
                                    position.latitude, position.longitude)
                            ,15)
            );

            fragment.setHasMovedCameraToCollection(true);
            return;
        }*/



/*        if(fragment.HasActiveCollection() && !fragment.HasMovedCameraToCollection()){
            Marker marker = fragment.getmCollectionMarker();
            LatLng position = marker.getPosition();

            fragment.getmMap().animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(
                    position.latitude, position.longitude)
                    ,15)
            );

            fragment.setHasMovedCameraToCollection(true);
        }*/


    }

    public void StopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                fragment.getmGoogleApiClient(), fragment);
    }


    public void FindCollection(){

        System.out.println("Finding last collection created");

        new AsyncTask(){
            boolean success;

            @Override
            protected Object doInBackground(Object[] params) {
                success = FindCollection_BG();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (success){
                    ShowToast("Opsamlingssted fundet!", Toast.LENGTH_LONG);
                    AddCollectionMarker();
                    UpdateMap();
                }else{
                    ShowToast("Der skete en fejl, tryk igen", Toast.LENGTH_LONG);
                }
                super.onPostExecute(o);
            }
        }.execute();
    }

    public void AddCollectionMarker() {
        fragment.setHasActiveCollection(true);

        MarkerOptions icon = new MarkerOptions().position(new LatLng(1, 1)).icon(BitmapDescriptorFactory.fromBitmap(Resize(R.drawable.collection)));
        fragment.setmCollectionMarker(fragment.getmMap().addMarker(icon));
        fragment.getmCollectionMarker().setDraggable(false);
        fragment.getmCollectionMarker().setVisible(false);

    }


    public void MoveMarkerToLowerScreen(final Marker marker){

        fragment.setContainer_height(fragment.getMapview().getHeight());

        Projection projection = fragment.getmMap().getProjection();

        LatLng markerLatLng = new LatLng(marker.getPosition().latitude,
                marker.getPosition().longitude);

        Point markerScreenPosition = projection.toScreenLocation(markerLatLng);

        Point pointHalfScreenAbove = new Point(markerScreenPosition.x,
                markerScreenPosition.y - (fragment.getContainer_height() / 10));

        LatLng aboveMarkerLatLng = projection
                .fromScreenLocation(pointHalfScreenAbove);

        CameraUpdate center = CameraUpdateFactory.newLatLng(aboveMarkerLatLng);
        fragment.getmMap().animateCamera(center, 500, new GoogleMap.CancelableCallback() {

            @Override
            public void onFinish() {
                ShowCustomInfoWindows(marker);
            }

            @Override
            public void onCancel() {
            }
        });


    }

    public void ShowCustomInfoWindows(Marker marker){
        View popupView = fragment.getLayoutInflater(null).inflate(R.layout.marker_info_window, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.update();

        TextView posterComment = (TextView) popupView.findViewById(R.id.commentText);
        TextView bagCount = (TextView) popupView.findViewById(R.id.bagCountText);
        TextView distance = (TextView) popupView.findViewById(R.id.distanceText);
        ImageButton googleMapsBtn = (ImageButton) popupView.findViewById(R.id.googleMapBtn);
        googleMapsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickWarning(getActivity());
            }
        });


        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        popupView.measure(size.x, size.y);


        posterComment.setText(Controller.controller.getNotification().getPostercomment());
        bagCount.setText(""+Controller.controller.getNotification().getBagcount());
        distance.setText(""+Controller.controller.getNotification().getDistance() + " meter.");



        fragment.setmWidth(popupView.getMeasuredWidth());
        fragment.setmHeight(popupView.getMeasuredHeight());
        fragment.setmMarker(marker);
        fragment.setmPopupWindow(popupWindow);

        fragment.getmPopupWindow().setOutsideTouchable(true);
        fragment.getmPopupWindow().setFocusable(true);

        fragment.getmPopupWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        UpdatePopup();
    }


    private void UpdatePopup() {
        if (fragment.getmMarker() != null && fragment.getmPopupWindow() != null) {
            // marker is visible
            if (fragment.getmMap().getProjection().getVisibleRegion().latLngBounds.contains(fragment.getmMarker().getPosition())) {
                if (!fragment.getmPopupWindow().isShowing()) {
                    fragment.getmPopupWindow().showAtLocation(fragment.getView(), Gravity.NO_GRAVITY, 0, 0);
                }
                Point p = fragment.getmMap().getProjection().toScreenLocation(fragment.getmMarker().getPosition());
                fragment.getmPopupWindow().update(p.x - fragment.getmWidth() / 2, p.y - fragment.getmHeight(), -1, -1);

            } else { // marker outside screen
                fragment.getmPopupWindow().dismiss();
            }
        }
    }

    public void ShowToast(CharSequence text, int duration){
        Context context = fragment.getContext();
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private boolean FindCollection_BG(){
        JSONObject object;

        String urlString = "http://refundlystaging.azurewebsites.net/api/collection/GetLastCollectionCreated";

        try{

            URL url = new URL(urlString.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();
            System.out.println("SC: "+ sc);

            if(sc == 200) {
                InputStream is = con.getInputStream();
                object = new JSONObject(ReadStringAndClose(is));
                System.out.println("------OBJECT-------");
                System.out.println(object.toString());

                if (!object.getBoolean("Error")) {
                    Controller.controller.getCollection().setCollectionId(object.getInt("CollectionId"));
                    Controller.controller.getCollection().setPosertComment(object.getString("PosterComment"));
                    Controller.controller.getCollection().setBagCount(object.getInt("BagCount"));
                    Controller.controller.getCollection().setLatitude(object.getDouble("Latitude"));
                    Controller.controller.getCollection().setLongtitude(object.getDouble("Longtitude"));
                    con.disconnect();
                    is.close();

                    double latitude = Controller.controller.getCollection().getLatitude();
                    double longtitude = Controller.controller.getCollection().getLongtitude();
                    URL addressURL = new URL("http://maps.googleapis.com/maps/api/geocode/json?latlng="+latitude+","+longtitude+"&sensor=true");
                    HttpURLConnection addressCon = (HttpURLConnection) addressURL.openConnection();
                    int addressSc = addressCon.getResponseCode();
                    System.out.println("Address SC: "+ addressSc);

                    if(addressSc == 200){
                        InputStream inputStream = addressCon.getInputStream();
                        object = new JSONObject(ReadStringAndClose(inputStream));

                        if (object.getString("status").equals("OK")){

                            String street_number = "";
                            String route = "";
                            String locality = "";
                            String postal_code = "";

                            JSONArray results = object.getJSONArray("results");
                            JSONObject obj = results.getJSONObject(0);

                            JSONArray addressComponentsArray = obj.getJSONArray("address_components");

                            JSONObject addressComponents;
                            JSONArray types;
                            String type;

                            for (int i = 0; i < addressComponentsArray.length(); i++){

                                addressComponents = addressComponentsArray.getJSONObject(i);
                                types = addressComponents.getJSONArray("types");

                                type = types.getString(0);

                                switch (type){

                                    case"street_number":
                                        street_number = addressComponents.getString("long_name");
                                        break;
                                    case"route":
                                        route = addressComponents.getString("long_name");
                                        break;
                                    case"locality":
                                        locality = addressComponents.getString("long_name");
                                        break;
                                    case"postal_code":
                                        postal_code = addressComponents.getString("long_name");
                                        break;
                                    default:
                                        break;

                                }

                            }

                            String address = route + " " + street_number + "\n" +
                                             postal_code + " " + locality;

                            Controller.controller.getCollection().setAddress(address);
                            addressCon.disconnect();
                            System.out.println(address);
                        }
                    }

                    return true;
                }
                else{
                    System.out.println("Json error er true");
                    return false;
                }


            }else{
                System.out.println("Server returnerede fejl: " + sc);
                return false;
            }


        }catch (JSONException e){
            System.out.println("--------JSONException--------");
            e.printStackTrace();
            return false;

        }catch (IOException e){
            System.out.println("--------IOException--------");
            e.printStackTrace();
            return false;
        }

    }

    //Kode fra AndroidElementer - læser data fra api
    private String ReadStringAndClose(InputStream is)
            throws IOException{
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] data = new byte[2048];
        int len = 0;
        while((len= is.read(data,0,data.length))>=0){
            bos.write(data);
        }
        is.close();
        /*System.out.println("--------READINPUTSTREAM--------");
        System.out.println(data);
        System.out.println(new String(bos.toByteArray(), "UTF-8"));
        System.out.println("----------------");*/
        return new String(bos.toByteArray(),"UTF-8");
    }


    //Opdaterer markører på kort
    private void UpdateMapMarkers(LatLng latlng){
        fragment.getmUserMarker().setPosition(latlng);
        fragment.getmUserMarker().setVisible(true);

        /*if (fragment.HasActiveCollection()){
            LatLng collectionPosition = Controller.controller.getCollection().getPosition();
            fragment.getmCollectionMarker().setPosition(collectionPosition);
            fragment.getmCollectionMarker().setVisible(true);
        }*/
/*
        if (fragment.isNotificationIntent()){
            AddCollectionMarker();
            LatLng collectionPosition = Controller.controller.getNotification().getPosition();
            fragment.getmCollectionMarker().setPosition(collectionPosition);
            fragment.getmCollectionMarker().setVisible(true);
        }*/

    }

    private Activity getActivity(){
        return fragment.getActivity();
    }

    public  void onClickWarning(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Refundly");
        builder.setMessage("Denne funktion åbner en ny applikation.");
        builder.setPositiveButton("Tillad", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FindRouteByGoogleMaps(fragment.getmCollectionMarker().getPosition());
            }
        });
        builder.setNegativeButton("Annuller", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}

        });
        builder.show();
    }
}
