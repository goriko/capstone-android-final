package com.example.dar.share;

import java.util.Date;

public class Message {

    public String MessageText;
    public String MessageUser;
    public String Time;
    public String Date;

    public Message(String MessageText, String MessageUser, String time, String date){
        this.MessageText = MessageText;
        this.MessageUser = MessageUser;
        this.Time = time;
        this.Date = date;
    }

}