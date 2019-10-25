package com.emrhmrc.addcontactlist.models;

public class Number {
    private int Id;
    private String Number1;

    public Number() {
    }

    public Number(int id, String number1) {
        Id = id;
        Number1 = number1;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getNumber1() {
        return Number1;
    }

    public void setNumber1(String number1) {
        Number1 = number1;
    }
}
