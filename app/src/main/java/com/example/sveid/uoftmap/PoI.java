package com.example.sveid.uoftmap;

/**
 * Created by sveid on 22-Sep-17.
 */

public class PoI {
    private char type;
    private String code;
    private String postal;
    private String address;
    private String description;
    private String url;
    private String name;

    public PoI(String code, String name, String postal, String address) {
        type = 'B';
        this.address = address;
        this.postal = postal;
        this.code = code;
        this.name = name;

    }

    public PoI(String code, String name, String address, String url, String description) {
        type = 'F';
        this.address = address;
        this.url = url;
        this.description = description;
        this.code = code;
        this.name = name;


    }


    public String getDescription() {
        return description;
    }

    public String getAddress() {
        return address;
    }

    public String getPostal() {
        return postal;
    }

    public String getCode() {
        return code;
    }

    public String getUrl() {
        return url;
    }

    public char getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
