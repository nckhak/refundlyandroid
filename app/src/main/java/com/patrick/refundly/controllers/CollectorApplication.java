package com.patrick.refundly.controllers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.patrick.refundly.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class CollectorApplication extends Activity implements View.OnClickListener{

    Button applyBtn;
    EditText phoneNumberEditText;
    TextView phoneNumberErrorTextView;

    String phonenumber;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector_application);

        applyBtn = (Button) findViewById(R.id.applyCollectorBtn);
        phoneNumberEditText = (EditText) findViewById(R.id.applyPhoneEditText);
        phoneNumberErrorTextView = (TextView) findViewById(R.id.applyPhoneErrorTextView);

        applyBtn.setOnClickListener(this);
        phoneNumberErrorTextView.setVisibility(View.INVISIBLE);
        findViewById(R.id.loadingPanel).setVisibility(View.INVISIBLE);

    }

    @Override
    public void onClick(View v) {
        if(v==applyBtn){
            phoneNumberErrorTextView.setVisibility(View.INVISIBLE);

            if(phoneNumberEditText.length() == 8){
                /*Intent i = new Intent(this, LoginActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();*/
                email = Controller.controller.getUser().getEmail();
                phonenumber = phoneNumberEditText.getText().toString();
                startLoadingView();
                updateApplicationOnServer();
            }else{
                phoneNumberErrorTextView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void startLoadingView(){
        findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
    }

    public void endLoadingView(){
        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
    }

    public void goToLoginScreen(){
        endLoadingView();
        finish();
        Intent i = new Intent(this, LoginActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
    }


    public void updateApplicationOnServer(){

        System.out.println("Finding last collection created");

        new AsyncTask(){
            boolean success;

            @Override
            protected Object doInBackground(Object[] params) {
                success = FindCollection_BG(email, phonenumber);
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                email = null;
                phonenumber = null;
                if(success){
                    System.out.println("DONE MED DET!");
                    goToLoginScreen();
                }
                else
                    endLoadingView();
            }
        }.execute();
    }

    private boolean FindCollection_BG(String email, String phonenumber){
        JSONObject object;

        String urlString = "http://refundlystaging.azurewebsites.net/api/Account/ChangeRoleByEmail?email="+email+"&phonenumber="+phonenumber;
        System.out.println(urlString);

        try{

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
                    Controller.controller.getUser().setRole(object.getString("Role"));
                    con.disconnect();
                    is.close();

                    final SharedPreferences mPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
                    SharedPreferences.Editor prefsEditor = mPrefs.edit();

                    JSONObject user = new JSONObject(mPrefs.getString("User", null));
                    user.put("mRole", Controller.controller.getUser().getRole());

                    prefsEditor.putString("User", user.toString());
                    prefsEditor.commit();
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
}
