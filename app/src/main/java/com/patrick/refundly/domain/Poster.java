package com.patrick.refundly.domain;

/**
 * Created by patrick on 3/21/16.
 */
public class Poster {
    private int mAccountId, mAreaCode;
    private String mFirstName, mLastName, mRoad, mPhoneNr, mCity;

    public Poster(){

    }

    public int getAccountId() {
        return mAccountId;
    }

    public void setAccountId(int mAccountId) {
        this.mAccountId = mAccountId;
    }

    public int getAreaCode() {
        return mAreaCode;
    }

    public void setAreaCode(int mAreaCode) {
        this.mAreaCode = mAreaCode;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String mLastName) {
        this.mLastName = mLastName;
    }

    public String getRoad() {
        return mRoad;
    }

    public void setRoad(String mRoad) {
        this.mRoad = mRoad;
    }

    public String getPhoneNr() {
        return mPhoneNr;
    }

    public void setPhoneNr(String mPhoneNr) {
        this.mPhoneNr = mPhoneNr;
    }

    public String getCity() {
        return mCity;
    }

    public void setCity(String city) {
        mCity = city;
    }
}
