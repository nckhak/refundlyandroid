package com.patrick.refundly.view;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.domain.Notification;
import com.patrick.refundly.domain.User;
import com.patrick.refundly.model.LoginActivityController;

public class StartActivity extends Activity {

    private LoginActivityController model;
    private User userObj;
    private ProgressBar progressBar;
    private TextView text;

    private boolean standby;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        model = new LoginActivityController(this);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        text = (TextView) findViewById(R.id.loadingText);


        standby = getIntent().getBooleanExtra("Standby", false);

        if (standby){
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            text.setText("Denne bruger er på standby.");
            progressBar.setVisibility(View.GONE);
            return;
        }


        //Creating a shared preference
        final SharedPreferences mPrefs = getSharedPreferences("PREFERENCE", MODE_PRIVATE);
        Gson gson = new Gson();
        String userJson = mPrefs.getString("User", "");
        System.out.println("KHALED: "+userJson);
        userObj = gson.fromJson(userJson, User.class);
        System.out.println("KHALED: "+userObj);

        if (userJson.equals(""))
        {
            System.out.println("StartActivity: User obj does not exists i shared preferences");
            System.out.println("StartActivity: Redirecting to LoginActivity.");
            finish();
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            progressBar.setVisibility(View.GONE);
            return;


        }
        else {

            System.out.println("StartActivity: Getting user from database to see if any change has been made");
            model.getUser();

        }

    }



    public User getUserObj(){
        return userObj;
    }

    public ProgressBar getProgressBar(){
        return progressBar;
    }

    public TextView getLoadingText(){
        return text;
    }

}
