package com.patrick.refundly.domain;

import com.patrick.refundly.Controller;

/**
 * Created by patrick on 3/21/16.
 */
public class User {
    private String mUserName;
    private String mPhoneNumber;
    private String mEmail;
    private String mRole;
    private String mGoogleId;
    private int Id;
    private String mAccountId;
    private boolean mActive;
    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAccountId() {
        return mAccountId;
    }


    public void setAccountId(String mAccountId) {
        this.mAccountId = mAccountId;
    }


    public String getmGoogleId() {
        return mGoogleId;
    }

    public void setmGoogleId(String mGoogleId) {
        this.mGoogleId = mGoogleId;
    }

    public User(){
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String mUserName) {
        this.mUserName = mUserName;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String mPhoneNumber) {
        this.mPhoneNumber = mPhoneNumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getRole() {
        return mRole;
    }

    public void setRole(String mRole) {
        this.mRole = mRole;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean Active) {
        this.mActive = Active;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String toString(){
        return "Navn: "+mUserName+". Email: "+mEmail+". ID: "+ mGoogleId+
                ". Deviceid: "+Controller.controller.getDeviceId()+". Account id: "+mAccountId+". Role: "+mRole+ ". Phonenumber: " +mPhoneNumber;
    }

    public boolean isEqual(User obj) {

        int count = 0;

        count += this.mRole.equals(obj.mRole) ? 1: 0;
        count += this.mUserName.equals(obj.mUserName) ? 1: 0;

        return count > 1;
    }

}
