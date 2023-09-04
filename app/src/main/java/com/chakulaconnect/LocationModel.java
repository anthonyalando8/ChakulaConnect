package com.chakulaconnect;

import android.content.Context;

public class LocationModel {
    String country, county, city, streetAddress, strLongitude, strLatitude;

    public LocationModel(String country, String county, String city, String streetAddress, String strLongitude, String strLatitude) {
        this.country = country;
        this.county = county;
        this.city = city;
        this.streetAddress = streetAddress;
        this.strLongitude = strLongitude;
        this.strLatitude = strLatitude;
    }

    public LocationModel(){
        //Required empty constructor
    }
    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStrLongitude() {
        return strLongitude;
    }

    public void setStrLongitude(String strLongitude) {
        this.strLongitude = strLongitude;
    }

    public String getStrLatitude() {
        return strLatitude;
    }

    public void setStrLatitude(String strLatitude) {
        this.strLatitude = strLatitude;
    }
}
