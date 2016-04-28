package com.patrick.refundly.model;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;

import com.patrick.refundly.Controller;
import com.patrick.refundly.view.NotificationReceived;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Khaled on 20-04-2016.
 */
public class NotificationReceivedController {

    private NotificationReceived _activity;

    public NotificationReceivedController(NotificationReceived activity){
        this._activity = activity;
    }


    public void LockCollection(){
        new AsyncTask(){
            boolean success;

            @Override
            protected Object doInBackground(Object[] params) {
                success = LockCollection_BG();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
            }
        }.execute();
    }

    private boolean LockCollection_BG(){
        System.out.println("Collection: \nId = " +Controller.controller.getUser().getId()+ "\n CollectorId =" + Controller.controller.getNotification().getCollectionId());

        String urlString = "http://refundlystaging.azurewebsites.net/api/collection/UpdateCollectorCollectionId?" +
                "Id="+Controller.controller.getNotification().getCollectionId()+
                "&" +
                "collectorId=" +Controller.controller.getUser().getId();

        try {

            URL url = new URL(urlString.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();

            if (sc == 200){
                System.out.println("Collection: updated & locked!");
                return true;

            }else{
                System.out.println("Collection: error from server, sc = " +sc);
                return false;
            }


        }catch (IOException e){
            System.out.println("--------IOException--------");
            e.printStackTrace();
            return false;
        }

    }

    /*public void getAddress(){
        _activity.getAddress().setText("Henter adresse..");

        new AsyncTask(){
            boolean success;

            @Override
            protected Object doInBackground(Object[] params) {
                success = getAddress_BG();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (success){
                    _activity.getAddress().setText(Controller.controller.getNotification().getAddress());
                    _activity.getGetBtn().setEnabled(true);
                    _activity.getAddressBtn().setVisibility(View.GONE);
                }else{
                    _activity.getAddress().setText("Fejl i afhentning af adresse :(\nPrøv igen.");
                    _activity.getGetBtn().setEnabled(false);
                    _activity.getAddressBtn().setVisibility(View.VISIBLE);
                }
                super.onPostExecute(o);
            }
        }.execute();
    }*/

    private boolean getAddress_BG() {
        JSONObject object;
        String urlString = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                +_activity.getLatitude()+
                ","
                +_activity.getLongtitude()+
                "&sensor=true";

        try{

            URL url = new URL(urlString.toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();

            if(sc == 200) {
                InputStream is = con.getInputStream();
                object = new JSONObject(ReadStringAndClose(is));

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

                    Controller.controller.getNotification().setAddress(address);
                    con.disconnect();
                    is.close();
                    System.out.println(address);

                    return true;
                }
                else{
                    System.out.println("Google api server status: "+object.getString("status"));
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
        return new String(bos.toByteArray(),"UTF-8");
    }

}
