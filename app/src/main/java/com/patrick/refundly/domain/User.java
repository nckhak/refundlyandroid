package com.patrick.refundly.domain;

/**
 * Created by patrick on 3/21/16.
 */
public class User {
    private String mUserName, mPhoneNumber, mEmail, mRole, mId;
    private boolean mActive;

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
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

    public String toString(){
        return "Navn: "+mUserName+"Email: "+mEmail+". ID: "+mId;
    }

}
