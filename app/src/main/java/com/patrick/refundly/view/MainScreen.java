package com.patrick.refundly.view;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.patrick.refundly.R;

public class MainScreen extends AppCompatActivity implements View.OnClickListener
{

    Button loginButton, newUserButton;
    EditText userNameText, passwordText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        loginButton = (Button)findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);
        newUserButton = (Button)findViewById(R.id.newUserButton);
        newUserButton.setOnClickListener(this);
        userNameText = (EditText) findViewById(R.id.userNameTextEdit);
        userNameText.setOnClickListener(this);
        passwordText = (EditText) findViewById(R.id.passwordTextEdit);
        passwordText.setOnClickListener(this);


    }

    @Override
    public void onClick(View v)
    {
        if(v==userNameText)
        {
            //if(userNameText.getText().toString().equals("Brugernavn"))
              //  userNameText.getText().clear();
        }
        if(v==passwordText)
        {
            //passwordText.getText().clear();
        }
        if(v==loginButton)
        {
            if(userNameText.getText().toString().equals("hat") && passwordText.getText().toString().equals("hej"))
                System.out.println("KORREKTLOGIN!");
            else
                System.out.println("FORKERTLOGIN!");

            Intent i = new Intent(this, MapScreen.class);
            startActivity(i);
        }
        if(v==newUserButton)
        {
            Intent i = new Intent(this, newUserScreen.class);
            startActivity(i);
        }
    }
}
