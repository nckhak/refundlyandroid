package com.patrick.refundly.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.patrick.refundly.R;

public class newPosterScreen extends Activity implements View.OnClickListener{

    Button newPosterButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_poster_screen);

        newPosterButton = (Button)findViewById(R.id.newPosterButton);
        newPosterButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==newPosterButton){
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Din bruger er nu oprettet og klar til brug.");
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

    public void userConfirmed(){
        Intent i=new Intent(this, MainScreen.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }
}
