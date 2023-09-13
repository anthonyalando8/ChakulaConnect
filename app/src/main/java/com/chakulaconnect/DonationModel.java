package com.chakulaconnect;

import java.util.HashMap;

public class DonationModel {
    HashMap<String, Object> foodDetails;
    HashMap<String, Object> storageHand;
    HashMap<String, String> imagesUri;
    LocationModel location;
    String donor;
    String reference;

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

    public LocationModel getLocation() {
        return location;
    }

    public void setLocation(LocationModel location) {
        this.location = location;
    }

    public HashMap<String, String> getImagesUri() {
        return imagesUri;
    }

    public void setImagesUri(HashMap<String, String> imagesUri) {
        this.imagesUri = imagesUri;
    }

    public DonationModel(HashMap<String, Object> foodDetails, HashMap<String, Object> storageHand, LocationModel location, String donor,
                         String donationId, String donationDate, HashMap<String, String > imagesUri, String reference) {
        this.foodDetails = foodDetails;
        this.storageHand = storageHand;
        this.location = location;
        this.donor = donor;
        this.donationId = donationId;
        this.donationDate = donationDate;
        this.imagesUri = imagesUri;
        this.reference = reference;
    }
    public DonationModel(){
        //Required
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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
}
