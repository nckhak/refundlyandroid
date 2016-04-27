package com.patrick.refundly.view;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.accounts.Account;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.gson.Gson;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.domain.Collection;
import com.patrick.refundly.domain.Notification;
import com.patrick.refundly.domain.User;
import com.patrick.refundly.model.GCMClientManager;
import com.patrick.refundly.model.LocationUpdateService;
import com.patrick.refundly.model.LoginActivityController;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ResourceBundle;

/*
Basis for kode vedrørende google-login er taget fra undervisningsappen
Android-Elementer af Jacob Nordfalk.
 */

public class LoginActivity extends AppCompatActivity
{
    private int selected;
    private JSONObject profile;
    private Account[] accounts;

    private LoginActivityController model;

    private boolean hasCollection = false;

    //Email kan trækkes ud fra accounts, og access[1] bruges derfor ikke pt.
    private String[] access = {
      "oauth2:https://www.googleapis.com/auth/userinfo.profile",
      "oauth2:https://www.googleapis.com/auth/userinfo.email"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        model = new LoginActivityController(this);

        //Creating a shared preference
        final SharedPreferences mPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);

        //Getting collection in SP
        Gson gson = new Gson();
        String notificationJson = mPrefs.getString("Collection", "");
        Notification notificationObj = gson.fromJson(notificationJson, Notification.class);

        //Giving controller information about an active collection for the user (if exists)
        if (notificationObj == null){

            System.out.println("This user has not an active collection");
            hasCollection = false;

        }else{

            System.out.println("An active collection found");
            System.out.println(notificationObj.getBagcount());
            System.out.println(notificationObj.getAddress());
            System.out.println(notificationObj.getLatitude());
            System.out.println(notificationObj.getLongtitude());
            System.out.println(notificationObj.getPostercomment());
            System.out.println(notificationObj.getMessage());


            Controller.controller.getNotification().setBagcount(notificationObj.getBagcount());
            Controller.controller.getNotification().setAddress(notificationObj.getAddress());
            Controller.controller.getNotification().setLatitude(notificationObj.getLatitude());
            Controller.controller.getNotification().setLongtitude(notificationObj.getLongtitude());
            Controller.controller.getNotification().setPostercomment(notificationObj.getPostercomment());
            Controller.controller.getNotification().setMessage(notificationObj.getMessage());
            hasCollection = true;
        }



        /*
        Test af REST interface
        testAPI();
        */
        if(Controller.controller.isConnected()) {

            System.out.println("//////////////////////////1//////////////////////////");

            if(Controller.controller.getUser() == null) {
                System.out.println("//////////////////////////2//////////////////////////");
                AccountManager accountManager = AccountManager.get(this);
                accounts = accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

                if (accounts.length == 0) {
                    Toast.makeText(this, "Du skal være logget ind på en google-konto for at bruge Refundly", Toast.LENGTH_LONG).show();
                }
                if (accounts.length > 0) {
                    System.out.println("//////////////////////////3//////////////////////////");
                    selectAccountDialog().show();
                }
            }
            else {
                model.getDeviceId("login");
                testAPI();
            }
        }else
            problemDialog(1);

    }


    public void testAPI()
    {
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                    getJsonFromServer();
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                System.out.println("FÆRDIG!");
            }

        }.execute();
    }

    public void getJsonFromServer(){
        try{
            String email = Controller.controller.getUser().getEmail();
            String deviceid = Controller.controller.getDeviceId();
            System.out.println("");
            URL url = new URL("http://refundlystaging.azurewebsites.net/api/account/loginuser/?email="+email+"&deviceId="+deviceid);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();

            int sc = con.getResponseCode();
            if(sc==200) {
                InputStream is = con.getInputStream();
                profile = new JSONObject(readStringAndClose(is));
                System.out.println("----------------");
                System.out.println(profile.toString());

                //Tjek for fejl fra serveren
                if ((boolean) profile.get("Error") == true) {
                    problemDialog(3);
                }
                else if ((boolean) profile.get("Error") == false) {
                    Controller.controller.getUser().setRole((String) profile.get("Role"));
                    Controller.controller.getUser().setId((int) profile.get("AccountId"));
                    Controller.controller.getUser().setAccountId(profile.get("AccountId").toString());
                    System.out.println(Controller.controller.getUser().toString());

                    final SharedPreferences mPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();
                    Gson gson = new Gson();
                    User obj = new User();

                    obj.setEmail(Controller.controller.getUser().getEmail());
                    obj.setId(Controller.controller.getUser().getId());
                    obj.setRole(Controller.controller.getUser().getRole());
                    obj.setUserName(Controller.controller.getUser().getUserName());

                    String json = gson.toJson(obj);

                    prefsEditor.putString("User", json);
                    prefsEditor.commit();
                    System.out.println("User saved in SP");

                    if (hasCollection){
                        goToMapscreen(true);
                    }else{
                        goToMapscreen(false);
                    }

                }

            }else{
                System.out.println("Server returnerede fejl: " + sc);
            }

        }catch (IOException e) {
            System.out.println("IOException");
            e.printStackTrace();
            problemDialog(1);
        } catch (JSONException e) {
            System.out.println("JSONException");
            e.printStackTrace();
        }
    }



    //Åbner mapscreen, og fjerner LoginActivity fra stacken, så man ikke kan gå tilbage hertil
    public void goToMapscreen(boolean collection){

            if(Controller.controller.getUser().getRole().equals("C")) {
                Intent intent = new Intent(this, LocationUpdateService.class);
                startService(intent);
            }

            Intent i = new Intent(this, FragmentContainer.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

            if (collection) { i.putExtra("NotificationIntent", true); }

            startActivity(i);
            finish();
    }

    //Dialog til valg af googlekonti
    public Dialog selectAccountDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vælg konto");
        String[] names = new String[accounts.length];
        for (int i = 0; i < accounts.length; i++) {
            names[i] = accounts[i].name;
        }
        builder.setItems(names, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                        selected = which;
                        getAuthToken();
                    }
                });
        return builder.create();
    }

    //Starter baggrundstråd. Opretter nyt user objekt, og går til mapscreen, hvis der blev returneret data fra google
    public void getAuthToken(){
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object... params) {
                getAuthTokenBg();
                return null;
            }

            //Kører når baggrundstråden er færdig
            @Override
            protected void onPostExecute(Object o) {
                if (profile != null) {
                    try {
                        Controller.controller.newUser(
                                profile.getString("name"),
                                accounts[selected].name
                        );
                        System.out.println("LOGGET FUCKING IND NU!");
                        System.out.println(Controller.controller.getUser().toString());
                        model.getDeviceId("login");
                        testAPI();

                    } catch (JSONException e) {
                        System.out.println("Parsning af JSON kiksede");
                        e.printStackTrace();
                        problemDialog(3);
                    }
                } else
                    problemDialog(3);
            }

        }.execute();
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

    //Let redigeret kode fra AndroidElementer - Kald til google api og modtagelse af returneret JSON objekt.
    private void getAuthTokenBg(){
        try{
            String token = GoogleAuthUtil.getToken(this, accounts[selected].name, access[0]);

            URL url = new URL("https://www.googleapis.com/oauth2/v1/userinfo?access_token="+token);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            int sc = con.getResponseCode();
            if(sc==200){
                InputStream is = con.getInputStream();
                profile = new JSONObject(readStringAndClose(is));
                System.out.println("----------------");
                System.out.println(profile.toString());
                System.out.println("----------------");
            }else if(sc==401){
                GoogleAuthUtil.invalidateToken(this,token);
                System.out.println("Server auth fejl: \n"+readStringAndClose(con.getErrorStream()));
            }else{
                System.out.println("Server returnerede fejl: "+sc);
            }

        } catch (UserRecoverableAuthException e) {
            System.out.println("Bruger skal logge ind");
            startActivityForResult(e.getIntent(),1001);
        } catch (GoogleAuthException e) {
            System.out.println("authexception");
            e.printStackTrace();
        } catch (IOException e) {
            //System.out.println("IOException");
            e.printStackTrace();
            problemDialog(1);
        } catch (JSONException e) {
            System.out.println("JSONException");
            e.printStackTrace();
        }
    }

    //Kode fra Androidelementer. Modtager svar fra permission dialog
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == 1001){
            if(resultCode == RESULT_OK){
                System.out.println("---------------");
                System.out.println("AUTH SUCCESSFULD!");
                System.out.println("---------------");
            }
            if(resultCode == RESULT_CANCELED){
                System.out.println("---------------");
                System.out.println("AUTH CANCELLED!");
                System.out.println("---------------");
                problemDialog(2);
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //Dialoger til fejlmeddelelser
    public void problemDialog(int j){
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        if(j==1) {
            dlgAlert.setMessage("Du skal oprette forbindelse til internettet, for at kune bruge denne app.");
            dlgAlert.setTitle("Ingen internetadgang");
        }
        else if(j==2) {
            dlgAlert.setMessage("Du skal give adgang til dine oplysninger for at kunne bruge denne app.");
            dlgAlert.setTitle("Manglende brugeroplysninger");
        }
        else{
            dlgAlert.setMessage("Beklager, et eller andet gik helt i kage. Dræb appen, og prøv igen.");
            dlgAlert.setTitle("Noget gik galt");
        }
        dlgAlert.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }



}
