package com.github.danielwojciechowski.ttmobile;

import java.util.Date;

public class GeoPoint {
    private double latitude;
    private double longitude;
    private Date date;

    public GeoPoint(double latitude, double longitude, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public GeoPoint() {
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}

