package com.patrick.refundly.services;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class LocationUpdateService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private double lat, longn;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    @Override
    public void onCreate() {
        super.onCreate();

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getApplication())
                    .addConnectionCallbacks(LocationUpdateService.this)
                    .addOnConnectionFailedListener(LocationUpdateService.this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(300000);
        mLocationRequest.setFastestInterval(250000);
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("LocationUpdateService - Har ikke tilladelse til Fine eller Coarse location!");
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean stopService(Intent name) {
        return super.stopService(name);
    }

    private void UpdateWithNewLocation(final Location loc) {

        double latitude = loc.getLatitude(); // Updated lat
        double longitude = loc.getLongitude(); // Updated long

        String response = null;
        if (lat != latitude || longn != longitude) {
            if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            System.out.println("---- LOLOLOL LOCATION UPDATE!! \nlat: " + latitude + "\nlong: " + longitude);
            lat = latitude;
            longn = longitude;
            updateGPSOnServer();
        }
        else
        {
            System.out.println("No lat and longitude found");
        }
}

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, LocationUpdateService.this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //Handle

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        //Handle
    }


    public void updateGPSOnServer(){

        System.out.println("Sending user GPS position to server");

        new AsyncTask(){
            boolean success;

            @Override
            protected Object doInBackground(Object[] params) {
                success = UpdateGps_BG();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                if(success){
                    System.out.println("DONE MED DET!");
                }
                else
                    System.out.println("Fejl i det!");
            }
        }.execute();
    }

    private boolean UpdateGps_BG() {

        JSONObject object;
        final SharedPreferences mPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();

        try{
        JSONObject user = new JSONObject(mPrefs.getString("User", null));
            if(user == null){
                return false;
            }
        int id = user.getInt("Id");

        prefsEditor.putString("User", user.toString());

        String urlString = "http://refundlystaging.azurewebsites.net/api/GPSLog/UpdateGPS?id="+id+"&latitude="+lat+"&longitude="+longn;
        System.out.println(urlString);


            URL url = new URL(urlString.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();
            System.out.println("SC: "+ sc);

            if(sc == 200) {
                InputStream is = con.getInputStream();
                object = new JSONObject(ReadStringAndClose(is));
                /*System.out.println("------OBJECT-------");
                System.out.println(object.toString());*/

                if (!object.getBoolean("Error")) {
                    System.out.println("Opdateret GPS position!");
                    return true;
                }
                else{
                    System.out.println("Fejl ved opdatering af GPS position!");
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

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            double latitude = location.getLatitude(); // Updated lat
            double longitude = location.getLongitude(); // Updated long

            String response = null;
            if (lat != latitude || longn != longitude) {
                if (ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplication(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                //System.out.println("---- LOLOLOL LOCATION UPDATE!! \nlat: " + latitude + "\nlong: " + longitude);
                lat = latitude;
                longn = longitude;
                updateGPSOnServer();
            }
        }
    }
}

