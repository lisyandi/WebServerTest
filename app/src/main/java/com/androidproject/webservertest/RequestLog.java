package com.androidproject.webservertest;

public class RequestLog {
    private String id;
    private String request;

    public RequestLog(){

    }

    public RequestLog(String id, String request) {
        this.id = id;
        this.request = request;
    }

    @Override
    public String toString() {
        return "RequestLog{" +
                "id='" + id + '\'' +
                "request='" + request + '\'' +
                '}';
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
