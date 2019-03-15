package com.example.dar.share;

public class AddUserInformation {
    public String Fname;
    public String Lname;
    public String Gender;
    public String ContactNumber;
    public String EmergencyContact;
    public String CurRoom;

    public AddUserInformation(String Fname, String Lname, String Gender, String Number, String GuardianNumber){
        this.Fname = Fname;
        this.Lname = Lname;
        this.Gender = Gender;
        this.ContactNumber = Number;
        this.EmergencyContact = GuardianNumber;
        this.CurRoom = "0";

    }
}