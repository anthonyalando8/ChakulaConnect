package com.chakulaconnect;

import java.util.HashMap;

public class DonationModel {
    HashMap<String, Object> foodDetails;
    HashMap<String, Object> storageHand;
    HashMap<String, LocationModel> location;
    String donor;

    public String getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(String donationDate) {
        this.donationDate = donationDate;
    }

    String donationDate;

    public String getDonationId() {
        return donationId;
    }

    public void setDonationId(String donationId) {
        this.donationId = donationId;
    }

    String donationId;

    public String getDonor() {
        return donor;
    }

    public void setDonor(String donor) {
        this.donor = donor;
    }

    public DonationModel(HashMap<String, Object> foodDetails, HashMap<String, Object> storageHand, HashMap<String, LocationModel> location, String donor, String donationId, String donationDate) {
        this.foodDetails = foodDetails;
        this.storageHand = storageHand;
        this.location = location;
        this.donor = donor;
        this.donationId = donationId;
        this.donationDate = donationDate;
    }
    public DonationModel(){
        //Required
    }

    public HashMap<String, Object> getFoodDetails() {
        return foodDetails;
    }

    public void setFoodDetails(HashMap<String, Object> foodDetails) {
        this.foodDetails = foodDetails;
    }

    public HashMap<String, Object> getStorageHand() {
        return storageHand;
    }

    public void setStorageHand(HashMap<String, Object> storageHand) {
        this.storageHand = storageHand;
    }

    public HashMap<String, LocationModel> getLocation() {
        return location;
    }

    public void setLocation(HashMap<String, LocationModel> location) {
        this.location = location;
    }
}
