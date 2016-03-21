package com.patrick.refundly.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by patrick on 3/21/16.
 */
public class Collection {
    LatLng mLatLng = new LatLng(55.699930, 12.589216);
    String mDetails = "TO FucKING POSER! MASSER AF PANT";
    int mBagSize = 2;
    boolean mIsPublic = false;

    public Collection(){

    }

    public LatLng getPosition(){
        return mLatLng;
    }
}
