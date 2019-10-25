package com.emrhmrc.addcontactlist.models;

import com.emrhmrc.genericrecycler.models.BaseModel;

public class Contact extends BaseModel {

    private String name;
    private String number;

    public Contact(String name, String number) {
        this.name = name;
        this.number = number;
    }

    public Contact() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        //notifyPropertyChanged();
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }


    @Override
    public String toString() {
        return "Contact{" +
                "name='" + name + '\'' +
                ", number='" + number + '\'' +
                '}';
    }
}
