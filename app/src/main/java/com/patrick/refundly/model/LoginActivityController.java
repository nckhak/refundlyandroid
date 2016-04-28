package com.patrick.refundly.model;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.domain.Notification;
import com.patrick.refundly.domain.User;
import com.patrick.refundly.view.FragmentContainer;
import com.patrick.refundly.view.LoginActivity;
import com.patrick.refundly.view.StartActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Khaled on 25-04-2016.
 */
public class LoginActivityController {

    private StartActivity _activity;
    private LoginActivity _activityLogin;

    public LoginActivityController(StartActivity activity){
        this._activity = activity;
    }

    public LoginActivityController(LoginActivity activity){
        this._activityLogin = activity;
    }

    public void getUser(){

        new AsyncTask(){
            User user;

            @Override
            protected Object doInBackground(Object[] params) {
                user = getUser_BG();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (user != null){
                    System.out.println("LoginActivityController: Success");
                    System.out.println("LoginActivityController: Setting user object in getUser() into controller");
                    Controller.controller.newUser(user.getUserName(), user.getEmail());
                    Controller.controller.getUser().setRole(user.getRole());
                    Controller.controller.getUser().setPhoneNumber(user.getPhoneNumber());
                    Controller.controller.getUser().setDeviceId(user.getDeviceId());
                    UserChanged();
                }else{
                    System.out.println("LoginActivityController: Failure");
                    _activity.getProgressBar().setVisibility(View.GONE);
                    _activity.getLoadingText().setText("En fejl er opstået\nServer: Mangel på data");
                }
                super.onPostExecute(o);
            }
        }.execute();


    }

    public void UserChanged(){

        String serverUserRole = Controller.controller.getUser().getRole();
        if (serverUserRole.equals("S")){
            _activity.finish();
            Intent intent = new Intent(_activity, StartActivity.class);
            intent.putExtra("Standby", true);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            _activity.startActivity(intent);
            return;
        }


        User userObj = _activity.getUserObj();
        getDeviceId("start");

        String currentDeviceId = Controller.controller.getDeviceId();
        String serverDeviceId = Controller.controller.getUser().getDeviceId();

        boolean deviceIdChanged = false;
        boolean userChanged = false;
        boolean redirectToMap = false;

        if (!currentDeviceId.equals(serverDeviceId)){
            System.out.println("LoginActivityController: DeviceId has been changed");
            deviceIdChanged = true;
        }


        System.out.println("LoginActivityController: Comparing:\n" + userObj.toString() + "\n" + "with\n" + Controller.controller.getUser().toString());


        if (userObj.isEqual(Controller.controller.getUser())){
            System.out.println("LoginActivityController: Nothing has been changed");
        }else{
            System.out.println("LoginActivityController: The user information is not up to date");
            userChanged = true;
        }



        if (deviceIdChanged || userChanged) {

            //Creating a shared preference
            final SharedPreferences mPrefs = _activity.getSharedPreferences("PREFERENCE", _activity.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = mPrefs.edit();


            if (userChanged){
                System.out.println("LoginActivityController: User in controller");
                System.out.println("LoginActivityController: "+Controller.controller.getUser().toString());
                System.out.println("LoginActivityController: Clearing user object in shared preferences");
                prefsEditor.remove("User");
                prefsEditor.commit();
                Gson gson = new Gson();
                User obj = new User();
                obj.setEmail(Controller.controller.getUser().getEmail());
                obj.setRole(Controller.controller.getUser().getRole());
                obj.setUserName(Controller.controller.getUser().getUserName());
                obj.setDeviceId(Controller.controller.getDeviceId());
                String json = gson.toJson(obj);
                prefsEditor.putString("User", json);
                prefsEditor.commit();
                System.out.println("LoginActivityController: New user saved in shared preferences");
                System.out.println("LoginActivityController: "+json);
                redirectToMap = true;
            }

            if (deviceIdChanged){
                System.out.println("LoginActivityController: DeviceId changed. Handle code");
            }

        }

        _activity.getProgressBar().setVisibility(View.GONE);
        Intent i = new Intent(_activity, LoginActivity.class);
        _activity.startActivity(i);


        /*if (redirectToMap){
            Intent i = new Intent(_activity, FragmentContainer.class);
            _activity.startActivity(i);
        }else{
            Intent i = new Intent(_activity, LoginActivity.class);
            _activity.startActivity(i);
        }*/



    }

    //Åbner mapscreen, og fjerner LoginActivity fra stacken, så man ikke kan gå tilbage hertil
    public void goToMapscreen(boolean collection){

        if(Controller.controller.getUser().getRole().equals("C")) {
            Intent intent = new Intent(_activity, LocationUpdateService.class);
            _activity.startService(intent);
        }

        Intent i = new Intent(_activity, FragmentContainer.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        if (collection) { i.putExtra("NotificationIntent", true); }

        _activity.startActivity(i);
        _activity.finish();
    }

    public void getDeviceId(String activity) {

        GCMClientManager gcmmanager;

        if (activity.equals("login")){
            gcmmanager = new GCMClientManager(_activityLogin, _activityLogin.getString(R.string.senderId));
        }
        else if(activity.equals("start")){
            gcmmanager = new GCMClientManager(_activity, _activity.getString(R.string.senderId));
        }
        else{
            return;
        }



        gcmmanager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {

                if (isNewRegistration) {
                    System.out.println("/////////////////////////////////////////////////////////////");
                    System.out.println("/////////////////////////////////////////////////////////////");
                    System.out.println("//////////////////////NEW REGISTRATION///////////////////////");
                    System.out.println("/////////////////////////////////////////////////////////////");
                    System.out.println("/////////////////////////////////////////////////////////////");
                    SendNewDeviceId();
                }

                Log.d("Registration id", registrationId);
                Controller.controller.setDeviceId(registrationId);
            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
            }
        });
    }

    private void SendNewDeviceId(){
        new AsyncTask(){
            boolean success;
            @Override
            protected Object doInBackground(Object[] params) {
                success = SendNewDeviceId_BG();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                if (success){
                    System.out.println("LoginActivityController: SendDeviceId ended as expected!");
                }else{
                    System.out.println("LoginActivityController: SendDeviceId ended NOT as expected!");
                }
                super.onPostExecute(o);
            }
        }.execute();
    }

    private boolean SendNewDeviceId_BG(){

        String urlString = "http://refundlystaging.azurewebsites.net/api/account/UpdateDeviceId?" +
                           "email=" + Controller.controller.getUser().getEmail() +
                           "&deviceId=" + Controller.controller.getDeviceId();

        try {

            JSONObject object;
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();

            if(sc == 200) {
                InputStream is = con.getInputStream();
                object = new JSONObject(ReadStringAndClose(is));

                if (!object.getBoolean("Error")){
                    System.out.println("Connection: Deviceid blev opdateret korrekt på serveren");
                    return true;
                }else{
                    System.out.println("Connection: Servicen returnerede fejlbeskeden: >> " + object.getString("message"));
                    return false;
                }

            }else{
                System.out.println("Connection: Server returnerede fejl: " + sc);
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

    private User getUser_BG() {

        final SharedPreferences mPrefs =  getActivity().getSharedPreferences("PREFERENCE", getActivity().MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        User userObj = gson.fromJson(userJson, User.class);
        System.out.println("Connection: User obj in SP email: " +userObj.getEmail());

        if (userObj == null) return null;

        JSONObject object;
        String urlString = "http://refundlystaging.azurewebsites.net/api/account/getuserbyemail?email=" +userObj.getEmail();

        try{

            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();

            if(sc == 200) {
                InputStream is = con.getInputStream();
                object = new JSONObject(ReadStringAndClose(is));

                if (!object.getBoolean("Error")){

                    User serverUser = new User();
                    serverUser.setRole(object.getString("Role"));
                    serverUser.setUserName(object.getString("Username"));
                    serverUser.setPhoneNumber(object.getString("PhoneNumber"));
                    serverUser.setDeviceId(object.getString("DeviceId"));
                    //Taking the used email (POST)
                    serverUser.setEmail(userObj.getEmail());

                    con.disconnect();
                    is.close();


                    return serverUser;
                }
                else{
                    System.out.println("RefundlyStaging returnede fejl:\n "+ object.getString("message"));
                    return null;
                }

            }else{
                System.out.println("Server returnerede fejl: " + sc);
                return null;
            }

        }catch (JSONException e){
            System.out.println("--------JSONException--------");
            e.printStackTrace();
            return null;

        }catch (IOException e){
            System.out.println("--------IOException--------");
            e.printStackTrace();
            return null;
        }

    }

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

    private Activity getActivity(){
        return _activity;
    }

}
