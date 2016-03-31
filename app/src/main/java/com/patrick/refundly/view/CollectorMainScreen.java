package com.patrick.refundly.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.patrick.refundly.Controller;
import com.patrick.refundly.R;

public class CollectorMainScreen extends AppCompatActivity implements View.OnClickListener{

    Button mActive, mToMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collector_main_screen);

        mActive = (Button)findViewById(R.id.activeCollectorButton);
        if(!Controller.controller.getUser().isActive()){
            mActive.setBackground(getDrawable(R.drawable.rounded_btn_red));
            mActive.setText("Nej");
        }else
            mActive.setText("Ja");
        mActive.setOnClickListener(this);

        mToMap = (Button)findViewById(R.id.toMapButton);
        mToMap.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if(v==mActive){
            if(Controller.controller.getUser().isActive()){
                mActive.setBackground(getDrawable(R.drawable.rounded_btn_red));
                mActive.setText("Nej");
                Controller.controller.getUser().setActive(false);
            }else {
                mActive.setBackground(getDrawable(R.drawable.rounded_btn));
                mActive.setText("Ja");
                Controller.controller.getUser().setActive(true);
            }
        }
        if(v==mToMap){
            Intent i = new Intent(this, MapScreen.class);
            startActivity(i);
        }

    }
}
