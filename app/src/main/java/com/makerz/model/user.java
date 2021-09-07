package com.makerz.model;

// Model for various activities.
public class user {

    private String codename;
    private String fullname;
    private String Email;

    // Empty constructor.
    public user() {
    }

    // Constructor.
    public user(String codename, String fullname, String email) {
        this.codename = codename;
        this.fullname = fullname;
        this.Email = email;
    }

    // Getter and setters.
    public String getCodename() {
        return codename;
    }

    public void setCodename(String codename) {
        this.codename = codename;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
