package com.patrick.refundly;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.patrick.refundly.domain.Collection;
import com.patrick.refundly.domain.User;

/**
 * Created by patrick on 3/21/16.
 */
public class Controller extends Application{
    public static Controller controller;
    User mUser;
    Collection mCollection;

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.mDeviceId = deviceId;
    }

    String mDeviceId;

    public void onCreate() {
        super.onCreate();
        //Do Application initialization over here
        controller = this;
        mCollection = new Collection();
    }

    public void newUser(String name, String id, String email){
        mUser = new User();
        mUser.setUserName(name);
        mUser.setmGoogleId(id);
        mUser.setEmail(email);
    }

    public User getUser(){
        return mUser;
    }

    public Collection getCollection(){
        return mCollection;
    }

    public boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm.getActiveNetworkInfo() != null)
            return cm.getActiveNetworkInfo().isConnected();
        else
            return false;
    }

}
