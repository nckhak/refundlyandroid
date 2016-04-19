package com.patrick.refundly.view;

import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.accounts.Account;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.model.GCMClientManager;

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


    //Email kan trækkes ud fra accounts, og access[1] bruges derfor ikke pt.
    private String[] access = {
      "oauth2:https://www.googleapis.com/auth/userinfo.profile",
      "oauth2:https://www.googleapis.com/auth/userinfo.email"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*
        Test af REST interface
        testAPI();
        */
        if(Controller.controller.isConnected()) {
            if(Controller.controller.getUser() == null) {
                AccountManager accountManager = AccountManager.get(this);
                accounts = accountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);

                if (accounts.length == 0) {
                    Toast.makeText(this, "Du skal være logget ind på en google-konto for at bruge Refundly", Toast.LENGTH_LONG).show();
                }
                if (accounts.length > 0) {
                    selectAccountDialog().show();
                }
            }
            else {
                getDeviceId();
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
                    goToMapscreen();
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

    public void getDeviceId(){
        GCMClientManager gcmmanager = new GCMClientManager(this, "257762151236");
        gcmmanager.registerIfNeeded(new GCMClientManager.RegistrationCompletedHandler() {
            @Override
            public void onSuccess(String registrationId, boolean isNewRegistration) {

                Log.d("Registration id", registrationId);
                Controller.controller.setDeviceId(registrationId);
            }

            @Override
            public void onFailure(String ex) {
                super.onFailure(ex);
            }
        });
    }


    //Åbner mapscreen, og fjerner LoginActivity fra stacken, så man ikke kan gå tilbage hertil
    public void goToMapscreen(){
            Intent i = new Intent(this, FragmentContainer.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
    }

    //Dialog til valg af googlekonti
    public Dialog selectAccountDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vælg konto");
        String[] names = new String[accounts.length];
        for(int i=0; i<accounts.length;i++){
            names[i]=accounts[i].name;
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
                                profile.getString("id"),
                                accounts[selected].name
                        );
                        System.out.println("LOGGET FUCKING IND NU!");
                        System.out.println(Controller.controller.getUser().toString());
                        getDeviceId();
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
