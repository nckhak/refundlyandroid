package com.patrick.refundly.domain;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by patrick on 3/21/16.
 */
public class Collection {

    private int collectionId;
    private String posertComment;
    private int bagCount;
    private double latitude;
    private double longtitude;

    private String address;


    public Collection(){}

    public void setCollectionId(int collectionId) {
        this.collectionId = collectionId;
    }

    public void setPosertComment(String posertComment) {
        this.posertComment = posertComment;
    }

    public void setBagCount(int bagCount) {
        this.bagCount = bagCount;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public void setAddress(String address) { this.address = address; }


    public LatLng getPosition(){
        return new LatLng(this.latitude, this.longtitude);
    }

    public int getCollectionId() {
        return collectionId;
    }

    public String getPosertComment() {
        return posertComment;
    }

    public int getBagCount() {
        return bagCount;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public String getAddress() {
        return address;
    }
}
