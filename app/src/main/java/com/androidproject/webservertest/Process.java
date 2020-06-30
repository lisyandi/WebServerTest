package com.androidproject.webservertest;

import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Process {
    private String Action;
    private String BranchCode, Name, AuthenticationCode, LastSetCookie, NextSetCookie, CookiePrefix, ProcessType, NRIC, CSRFToken, IdentifierType;
    private String CheckInCookie, CheckInCSRFToken, CheckOutCookie, CheckOutCSRFToken;
    private String UrlLoginPage, UrlLoginProcess, UrlRedirect, UrlCheckInPage, UrlCheckInProcess, UrlCheckOutPage, UrlCheckOutProcess, UrlGetDeviceID;
    private boolean Result;

    public boolean SendToServer(){
        CookiePrefix = "_ga=GA1.3.1997960644.1592482416; _gid=GA1.3.717040169.1592482416;";
        return true;
    }

    public boolean Action(String pAction, String pID, String pCode, String pName, String pNRIC){
        boolean ret = false;
        Result = false;
        Action  = pAction;
        CookiePrefix = "_ga=GA1.3.1997960644.1592482416; _gid=GA1.3.717040169.1592482416;";
        UrlLoginPage = "https://www.safeentry.gov.sg/scanner_login";
        UrlLoginProcess = "https://www.safeentry.gov.sg/scanner_login";
        UrlCheckInPage = "https://www.safeentry.gov.sg/entries/manual_entry";
        UrlCheckInProcess = "https://www.safeentry.gov.sg/entries/record_entry";
        UrlCheckOutPage = "https://www.safeentry.gov.sg/entries/manual_exit";
        UrlCheckOutProcess = "https://www.safeentry.gov.sg/entries/record_exit";
        BranchCode = pCode;
        Name = pName;
        ProcessType = pAction;
        IdentifierType = "nric_fin";
        NRIC = pNRIC;

        LoginPage();

        ret = Result;
        return ret;
    }

    private void LoginPage(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();
        Request request = new Request.Builder().url(UrlLoginPage).build();
        try {
            Response response = client.newCall(request).execute();
            LastSetCookie = response.header("set-cookie");
            NextSetCookie = CookiePrefix + LastSetCookie.split(";")[0];

            String responseString = response.body().string();
            Document html = Jsoup.parse(responseString);
            Element authenticity_token = html.getElementsByAttributeValue("name", "authenticity_token").first();
            AuthenticationCode = authenticity_token.val();
            LoginProcess();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void LoginProcess(){
        RequestBody formBody = new FormBody.Builder()
                .add("utf8", "✓")
                .add("authenticity_token", AuthenticationCode)
                .add("code", BranchCode)
                .add("name", Name)
                .add("commit", "Start scanning")
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(UrlLoginProcess)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", NextSetCookie)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            if(response.priorResponse() != null) {
                LastSetCookie = response.priorResponse().priorResponse().header("set-cookie");
                UrlRedirect = response.priorResponse().request().url().toString();
                NextSetCookie = CookiePrefix + LastSetCookie.split(";")[0];
                if(Action.toUpperCase().trim() != "LOGIN") {
                    RedirectPage();
                }
                else{
                    Result = true;
                }
            }
            else{
                Result = false;
            }
        } catch (IOException e) {
            //Toast.makeText(this, "Error LoginProcess: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void RedirectPage(){
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(UrlRedirect)
                .addHeader("Cookie", NextSetCookie)
                .build();
        try {
            Response response = client.newCall(request).execute();
            LastSetCookie = response.header("set-cookie");
            NextSetCookie = CookiePrefix + LastSetCookie.split(";")[0];
            if(ProcessType == "CHECK_IN"){
                CheckInPage();
            }
            else
            {
                CheckOutPage();
            }
        } catch (IOException e) {
            //Toast.makeText(this, "Error RedirectPage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void CheckInPage(){
        if(CheckInCookie == null || CheckInCookie.isEmpty()) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(5, TimeUnit.SECONDS);
            builder.readTimeout(5, TimeUnit.SECONDS);
            builder.writeTimeout(5, TimeUnit.SECONDS);
            OkHttpClient client = builder.build();
            Request request = new Request.Builder()
                    .url(UrlCheckInPage)
                    .addHeader("Cookie", NextSetCookie)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                LastSetCookie = response.header("set-cookie");
                CheckInCookie = CookiePrefix + LastSetCookie.split(";")[0];

                String responseString = response.body().string();
                Document html = Jsoup.parse(responseString);
                Element csrf_token = html.getElementsByAttributeValue("name", "csrf-token").first();
                CheckInCSRFToken = csrf_token.attr("content");
                CheckInProcess();
            } catch (IOException e) {
                //Toast.makeText(this, "Error CheckInPage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        else{
            CheckInProcess();
        }
    }

    private void CheckInProcess(){
        RequestBody formBody = new FormBody.Builder()
                .add("identifier", NRIC)
                .add("identifier_type", IdentifierType)
                .add("restriction_reason", "")
                .add("rejected", "false")
                .add("bypass_restriction", "false")
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(UrlCheckInProcess)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", CheckInCookie)
                .addHeader("X-CSRF-Token", CheckInCSRFToken)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            try {
                JSONObject objResponse = new JSONObject(responseString);
                Result = true;
                //Toast.makeText(this, objResponse.getString("message"), Toast.LENGTH_SHORT).show();
            }
            catch (JSONException ex){
                Result = false;
                //Toast.makeText(this, "Error JSONConvert: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        } catch (IOException e) {
            Result = false;
            //Toast.makeText(this, "Error LoginProcess: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void CheckOutPage(){
        if(CheckOutCookie == null || CheckOutCookie.isEmpty()) {
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(5, TimeUnit.SECONDS);
            builder.readTimeout(5, TimeUnit.SECONDS);
            builder.writeTimeout(5, TimeUnit.SECONDS);
            OkHttpClient client = builder.build();
            Request request = new Request.Builder()
                    .url(UrlCheckOutPage)
                    .addHeader("Cookie", NextSetCookie)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                LastSetCookie = response.header("set-cookie");
                NextSetCookie = CookiePrefix + LastSetCookie.split(";")[0];

                String responseString = response.body().string();
                Document html = Jsoup.parse(responseString);
                Element csrf_token = html.getElementsByAttributeValue("name", "csrf-token").first();
                CSRFToken = csrf_token.attr("content");
                CheckOutProcess();
            } catch (IOException e) {
                //Toast.makeText(this, "Error CheckInPage: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
        else{
            CheckOutProcess();
        }
    }

    private void CheckOutProcess(){
        RequestBody formBody = new FormBody.Builder()
                .add("identifier", NRIC)
                .add("identifier_type", IdentifierType)
                .add("restriction_reason", "")
                .add("rejected", "false")
                .add("bypass_restriction", "false")
                .build();

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(UrlCheckOutProcess)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Cookie", NextSetCookie)
                .addHeader("X-CSRF-Token", CSRFToken)
                .post(formBody)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            try {
                JSONObject objResponse = new JSONObject(responseString);
                Result = true;
                //Toast.makeText(this, objResponse.getString("message"), Toast.LENGTH_SHORT).show();
            }
            catch (JSONException ex){
                Result = false;
                //Toast.makeText(this, "Error JSONConvert: " + ex.getMessage(), Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }
        } catch (IOException e) {
            Result = false;
            //Toast.makeText(this, "Error LoginProcess: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public String getDeviceID(){
        UrlGetDeviceID = "http://127.0.0.1:8090/getDeviceKey";
        String deviceID = "";

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(5, TimeUnit.SECONDS);
        builder.readTimeout(5, TimeUnit.SECONDS);
        builder.writeTimeout(5, TimeUnit.SECONDS);

        OkHttpClient client = builder.build();
        Request request = new Request.Builder()
                .url(UrlGetDeviceID)
                .build();
        try {
            Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            try {
                JSONObject objResponse = new JSONObject(responseString);
                deviceID = objResponse.getString("data");
            }
            catch (JSONException ex){
                ex.printStackTrace();
            }
        } catch (IOException e) {
            Result = false;
            e.printStackTrace();
        }

        return deviceID;
    }
}
