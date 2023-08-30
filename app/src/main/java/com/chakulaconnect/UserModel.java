package com.chakulaconnect;

import java.util.HashMap;

public class UserModel {
    public UserModel(HashMap<String, String> accountDetails, HashMap<String, Boolean> account_role, HashMap<String, Object> moreInfo, HashMap<String, UserActivityModel> Activity ) {
        this.accountDetails = accountDetails;
        this.account_role = account_role;
        this.moreInfo = moreInfo;
        this.Activity = Activity;
    }
    public UserModel(){

    }

    public HashMap<String, String> getAccountDetails() {
        return accountDetails;
    }

    public void setAccountDetails(HashMap<String, String> accountDetails) {
        this.accountDetails = accountDetails;
    }

    public HashMap<String, Boolean> getAccount_role() {
        return account_role;
    }

    public void setAccount_role(HashMap<String, Boolean> account_role) {
        this.account_role = account_role;
    }

    public HashMap<String, Object> getMoreInfo() {
        return moreInfo;
    }

    public void setMoreInfo(HashMap<String, Object> moreInfo) {
        this.moreInfo = moreInfo;
    }

    HashMap<String, String> accountDetails;
    HashMap<String, Boolean> account_role;
    HashMap<String, Object> moreInfo;
    HashMap<String, UserActivityModel> Activity;

    public HashMap<String, UserActivityModel> getActivity() {
        return Activity;
    }

    public void setActivity(HashMap<String, UserActivityModel> activity) {
        Activity = activity;
    }
}
