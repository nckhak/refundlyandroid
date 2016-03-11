package com.patrick.refundly;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class gpsScreen extends AppCompatActivity implements View.OnClickListener
{

    ImageView collectorIcon, collectionIcon1, collectionIcon2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps_screen);

        collectorIcon = (ImageView) findViewById(R.id.collectorIcon);
        collectionIcon1 = (ImageView) findViewById(R.id.collectionIcon1);
        collectionIcon2 = (ImageView) findViewById(R.id.collectionIcon2);

        collectorIcon.setOnClickListener(this);
        collectionIcon1.setOnClickListener(this);
        collectionIcon2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(v==collectorIcon)
        {

        }
        if(v==collectionIcon1)
        {

        }
        if(v==collectionIcon2)
        {

        }
    }
}
