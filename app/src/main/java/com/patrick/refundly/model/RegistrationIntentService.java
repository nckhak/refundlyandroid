package com.patrick.refundly.model;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.patrick.refundly.R;

import java.io.IOException;

/**
 * Created by patrick on 4/6/16.
 */
public class RegistrationIntentService extends IntentService {


    public RegistrationIntentService(String name) {
        super(name);
    }
    // ...

    @Override
    public void onHandleIntent(Intent intent) {
        System.out.println("--------- ONHANDLEINTENT! -----------");
        System.out.println("------------------------------------------------------");
        // ...
        InstanceID instanceID = InstanceID.getInstance(this);
        try {
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            System.out.println("--------- DETTE ER INSTANCE ID FRA APPEN!! -----------");
            System.out.println(instanceID.getId());
            System.out.println("------------------------------------------------------");
        } catch (IOException e) {
            e.printStackTrace();
        }


        // ...
    }

    // ...
}


