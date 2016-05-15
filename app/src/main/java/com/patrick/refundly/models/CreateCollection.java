package com.patrick.refundly.models;

/**
 * Created by Khaled on 14-04-2016.
 */
public class CreateCollection {

    private int posterId;
    private double latitude;
    private double longitude;
    private int collectionSize;
    private String posterComment;

    public int getPosterId() {
        return posterId;
    }

    public void setPosterId(int posterId) {
        this.posterId = posterId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getCollectionSize() {
        return collectionSize;
    }

    public void setCollectionSize(int collectionSize) {
        this.collectionSize = collectionSize;
    }

    public String getPosterComment() {
        return posterComment;
    }

    public void setPosterComment(String posterComment) {
        this.posterComment = posterComment;
    }
}
