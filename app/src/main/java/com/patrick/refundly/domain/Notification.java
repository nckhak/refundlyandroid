package com.patrick.refundly.domain;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Khaled on 20-04-2016.
 */
public class Notification {

    private String message;
    private String postercomment;
    private String address;
    private double longtitude;
    private double latitude;
    private int bagcount;


    public Notification() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPostercomment() {
        return postercomment;
    }

    public void setPostercomment(String postercomment) {
        this.postercomment = postercomment;
    }

    public double getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(double longtitude) {
        this.longtitude = longtitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getBagcount() {
        return bagcount;
    }

    public void setBagcount(int bagcount) {
        this.bagcount = bagcount;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getPosition() {
        return new LatLng(latitude, longtitude);
    }


}
