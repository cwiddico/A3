package com.csci443.cwiddico.locatr;

/**
 * Created by cwiddico on 3/14/2018.
 */

public class GalleryItem {
    private String mCaption;
    private String mId;
    private String mUrl;
    private double mLat;
    private double mLon;
    @Override
    public String toString() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public void setId(String mId) {
        this.mId = mId;
    }
    public String getId(){return mId;}

    public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }

    public String getUrl() {
        return mUrl;
    }
    public double getLat() {
        return mLat;
    }
    public void setLat(double lat) {
        mLat = lat;
    }
    public double getLon() {
        return mLon;
    }
    public void setLon(double lon) {
        mLon = lon;
    }
}
