package com.emrhmrc.addcontactlist.models;

import com.emrhmrc.genericrecycler.models.BaseModel;

public class Member extends BaseModel {
    private int Id;
    private String Name;
    private String Surname;
    private String Email;
    private String UserId;
    private String ImageUri;
    private String Password;

    public Member() {
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;

    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getSurname() {
        return Surname;
    }

    public void setSurname(String surname) {
        Surname = surname;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getImageUri() {
        return ImageUri;
    }

    public void setImageUri(String imageUri) {
        ImageUri = imageUri;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

}
