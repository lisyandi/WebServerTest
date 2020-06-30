package com.androidproject.webservertest;

public class EntryLog {
    private String id;
    private String nric;
    private String process_date;

    public EntryLog(){

    }

    public EntryLog(String id, String nric, String process_date) {
        this.id = id;
        this.nric = nric;
        this.process_date = process_date;
    }

    @Override
    public String toString() {
        return "EntryLog{" +
                "id='" + id + '\'' +
                ", nric='" + nric + '\'' +
                ", process_date='" + process_date + '\'' +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNric() {
        return nric;
    }

    public void setNric(String nric) {
        this.nric = nric;
    }

    public String getProcess_date() {
        return process_date;
    }

    public void setProcess_date(String process_date) {
        this.process_date = process_date;
    }
}
