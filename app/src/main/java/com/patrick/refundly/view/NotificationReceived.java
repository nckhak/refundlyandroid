package com.patrick.refundly.view;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.patrick.refundly.Controller;
import com.patrick.refundly.R;
import com.patrick.refundly.model.NotificationReceivedController;



public class NotificationReceived extends AppCompatActivity implements View.OnClickListener {

    TextView message;
    TextView bagcount;
    TextView postercomment;
    TextView address;

    Button getBtn, declineBtn, addressBtn;

    double latitude;
    double longtitude;


    NotificationReceivedController model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_received);
        model = new NotificationReceivedController(this);

        message = (TextView) findViewById(R.id.textViewMessage);
        bagcount = (TextView) findViewById(R.id.textViewBagCount);
        postercomment = (TextView) findViewById(R.id.textViewPosterComment);
        address = (TextView) findViewById(R.id.textViewAddress);

        getBtn = (Button) findViewById(R.id.buttonHent);
        declineBtn = (Button) findViewById(R.id.buttonAfvis);
        addressBtn = (Button) findViewById(R.id.buttonAddress);

        if (addressBtn != null) {addressBtn.setVisibility(View.GONE);}


        getBtn.setOnClickListener(this);
        declineBtn.setOnClickListener(this);

        latitude = Controller.controller.getNotification().getLatitude();
        longtitude = Controller.controller.getNotification().getLongtitude();

        message.setText(Controller.controller.getNotification().getMessage());
        bagcount.setText(""+ Controller.controller.getNotification().getBagcount());
        postercomment.setText(Controller.controller.getNotification().getPostercomment());

        model.getAddress();


    }

    @Override
    public void onClick(View v) {

        if (v == getBtn){
            finish();
            Intent intent = new Intent(this, FragmentContainer.class);
            intent.putExtra("NotificationIntent", true);
            startActivity(intent);
        }
        else if (v == declineBtn){
            finish();
        }
        else if(v == addressBtn){

            model.getAddress();

        }


    }



    /*This section must not include anything
    * but setters and getters for the private
    * attributes.      /Peace be with you\   */

    public TextView getAddress() {
        return address;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public double getLatitude() {
        return latitude;
    }


    public Button getAddressBtn() {
        return addressBtn;
    }

    public Button getGetBtn() {
        return getBtn;
    }
}
