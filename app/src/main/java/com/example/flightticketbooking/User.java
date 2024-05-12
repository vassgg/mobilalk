package com.example.flightticketbooking;

public class User {
    private String uid;
    private String firstName;
    private String lastName;
    private String docId;

    public User() {
    }
    public User(String uid, String firstName, String lastName){
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
    }
    public String getUid() {
        return uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
    public String _getDocId(){
        return docId;
    }
    public void setDocId(String docId) {
        this.docId = docId;
    }
}
