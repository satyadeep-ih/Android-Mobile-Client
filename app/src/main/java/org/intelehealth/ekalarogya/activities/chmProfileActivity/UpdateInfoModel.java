package org.intelehealth.ekalarogya.activities.chmProfileActivity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UpdateInfoModel {
    @SerializedName("phoneNumber")
    @Expose
    private String phoneNumber;

    @SerializedName("emailId")
    @Expose
    private String emailId;

    @SerializedName("gender")
    @Expose
    private String gender;

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    @SerializedName("whatsapp")
    @Expose
    private String whatsapp;

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }
}
