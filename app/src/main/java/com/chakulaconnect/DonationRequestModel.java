package com.chakulaconnect;

import java.util.HashMap;

public class DonationRequestModel {
    HashMap<String, Object> foodDetails;
    HashMap<String, String> imagesUri;
    LocationModel location;
    String reference;

    public HashMap<String, String> getImagesUri() {
        return imagesUri;
    }

    public void setImagesUri(HashMap<String, String> imagesUri) {
        this.imagesUri = imagesUri;
    }

    String recipient;
    String requestId;
    String requestDate;

    public LocationModel getLocation() {
        return location;
    }

    public void setLocation(LocationModel location) {
        this.location = location;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public DonationRequestModel(HashMap<String, Object> foodDetails, LocationModel location, String recipient, String requestId,
                                String requestDate, HashMap<String, String> imagesUri, String reference) {
        this.foodDetails = foodDetails;
        this.location = location;
        this.recipient = recipient;
        this.requestId = requestId;
        this.requestDate = requestDate;
        this.imagesUri = imagesUri;
        this.reference = reference;

    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public DonationRequestModel(){
        //Required empty constructor
    }

    public HashMap<String, Object> getFoodDetails() {
        return foodDetails;
    }

    public void setFoodDetails(HashMap<String, Object> foodDetails) {
        this.foodDetails = foodDetails;
    }
}
