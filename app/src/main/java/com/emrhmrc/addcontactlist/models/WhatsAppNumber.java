package com.emrhmrc.addcontactlist.models;

import com.emrhmrc.genericrecycler.models.BaseModel;

public class WhatsAppNumber extends BaseModel {

    private int Id;
    private int MemberId;

    public WhatsAppNumber(String name, String number) {
        Name = name;
        Number = number;
    }

    public int getMemberId() {
        return MemberId;
    }

    private int WhatsAppId;
    private String Name;
    private String Number;

    public WhatsAppNumber() {
    }

    public void setMemberId(int memberId) {
        MemberId = memberId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public int getWhatsAppId() {
        return WhatsAppId;
    }

    public void setWhatsAppId(int whatsAppId) {
        WhatsAppId = whatsAppId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getNumber() {
        return Number;
    }

    public void setNumber(String number) {
        Number = number;
    }


}
