package com.patrick.refundly.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.patrick.refundly.R;

public class newCollectorScreen extends AppCompatActivity implements View.OnClickListener{

    private Button newUserButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_collector_screen);

        newUserButton = (Button)findViewById(R.id.newCollectorButton);
        newUserButton.setOnClickListener(this);
    }

    public void userConfirmed(){
        Intent i=new Intent(this, MainScreen.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onClick(View v) {
        if(v==newUserButton){
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Tak for din oprettelse. Vi ringer dig op snarest muligt, så du kan tage din konto i brug.");
            dlgAlert.setTitle("Bruger oprettet");
            dlgAlert.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            userConfirmed();
                        }
                    });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();
        }
    }
}
