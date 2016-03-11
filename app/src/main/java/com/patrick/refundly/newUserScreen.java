package com.patrick.refundly;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class newUserScreen extends AppCompatActivity implements View.OnClickListener{

    ImageView newCollectorIcon, newPosterIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_screen);

        newCollectorIcon = (ImageView) findViewById(R.id.newCollectorIcon);
        newCollectorIcon.setOnClickListener(this);
        newPosterIcon = (ImageView) findViewById(R.id.newPosterIcon);
        newPosterIcon.setOnClickListener(this);
    }

    @Override
    public void onClick(View v)
    {
        if(v==newPosterIcon)
        {

        }
        if(v==newCollectorIcon)
        {
            Intent i = new Intent(this, newCollectorScreen.class);
            startActivity(i);
        }
    }
}
