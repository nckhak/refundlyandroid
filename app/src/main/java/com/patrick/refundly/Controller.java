package com.patrick.refundly;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

import com.patrick.refundly.domain.Collection;
import com.patrick.refundly.domain.CreateCollection;
import com.patrick.refundly.domain.Notification;
import com.patrick.refundly.domain.User;

/**
 * Created by patrick on 3/21/16.
 */
public class Controller extends Application{
    public static Controller controller;
    User mUser;
    Collection mCollection;
    CreateCollection currentCollection;
    Notification notification;

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
        notification = new Notification();
    }

    public void newUser(String name, String email){
        mUser = new User();
        mUser.setUserName(name);
        mUser.setEmail(email);
    }

    public User getUser(){
        return mUser;
    }

    public void RemoveUser(){
        mUser = null;
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

    public void setNewCollection(int posterId, double latitude, double longitude, int collectionSize, String posterComment){

        currentCollection = new CreateCollection();
        currentCollection.setPosterId(posterId);
        currentCollection.setLatitude(latitude);
        currentCollection.setLongitude(longitude);
        currentCollection.setCollectionSize(collectionSize);
        currentCollection.setPosterComment(posterComment);
   }

    public CreateCollection getCurrentCollection(){
        return currentCollection;
    }

    public Notification getNotification(){
        return notification;
    }


}
