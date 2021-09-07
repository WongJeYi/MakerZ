package com.makerz.model;

public class GroupMembers {

    // Model for ViewMemberAdapter and ViewGroupMembers activity.

    public String codename, status;


    // Empty constructor.
    public GroupMembers() {
    }

    // Constructor.
    public GroupMembers(String codename, String status) {
        this.codename = codename;
        this.status = status;
    }

    // Getters and setters.
    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
