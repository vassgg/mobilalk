package com.example.flightticketbooking;

import java.util.Date;

public class FlightItem {
    private String id;
    private String date;
    private String from;
    private String to;
    private String price;
    private String bookedBy;
    public FlightItem(){}
    public FlightItem(String date, String from, String to, String price, String bookedBy) {
        this.date = date;
        this.from = from;
        this.to = to;
        this.price = price;
        this.bookedBy = bookedBy;
    }

    public String _getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }
    public String getTo(){return to;}

    public String getPrice() {
        return price;
    }
    public String getBookedBy(){return bookedBy;}
    public void setId(String id){
        this.id = id;
    }
}
