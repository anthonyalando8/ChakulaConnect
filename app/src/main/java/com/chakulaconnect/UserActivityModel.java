package com.chakulaconnect;

public class UserActivityModel {
    String activityTitle;
    String activityDescription;
    String activityFlag;

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public UserActivityModel(String activityTitle, String activityDescription, String activityFlag, String activityId, Long activityDate) {
        this.activityTitle = activityTitle;
        this.activityDescription = activityDescription;
        this.activityFlag = activityFlag;
        this.activityId = activityId;
        this.activityDate = activityDate;
    }

    String activityId;
    Long activityDate;
    public UserActivityModel() {
        //Empty constructor
    }

    public String getActivityTitle() {
        return activityTitle;
    }

    public void setActivityTitle(String activityTitle) {
        this.activityTitle = activityTitle;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public String getActivityFlag() {
        return activityFlag;
    }

    public void setActivityFlag(String activityFlag) {
        this.activityFlag = activityFlag;
    }

    public Long getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(Long activityDate) {
        this.activityDate = activityDate;
    }
}
