package com.patrick.refundly;
import android.app.Application;
import android.content.Context;

import com.patrick.refundly.model.*;

/**
 * Created by patrick on 3/21/16.
 */
public class Controller extends Application{
    public static Controller controller;
    Collector mCollector;
    Poster mPoster;
    Collection mcollection;

    public void onCreate() {
        super.onCreate();
        //Do Application initialization over here
        controller = this;
        mcollection = new Collection();
    }

    public void newCollector(String name, String password, String phonenumber, String email){
        mCollector = new Collector();
        mCollector.setUserName(name);
        mCollector.setPhoneNumber(phonenumber);
        mCollector.setEmail(email);
    }

    public void newPoster(){
    }

    public boolean checkLogin(String username, String password){
        return true;
    }

    public Collection getCollection(){
        return mcollection;
    }
}
