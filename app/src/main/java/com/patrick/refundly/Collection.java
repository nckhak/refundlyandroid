package com.patrick.refundly;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by patrick on 3/21/16.
 */
public class Collection {
    LatLng latLng = new LatLng(55.699930, 12.589216);
    String details = "TO FucKING POSER! MASSER AF PANT";
    int bagSize = 2;

    public Collection(){

    }

    protected LatLng getPosition(){
        return latLng;
    }
}
