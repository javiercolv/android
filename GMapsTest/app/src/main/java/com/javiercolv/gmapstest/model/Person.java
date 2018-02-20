package com.javiercolv.gmapstest.model;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Javier Cruz on 18/02/2018.
 */

public class Person {
    private String name;
    private String lastName;
    private LatLng ubication;
    private LatLng destiny;

    public Person(){}

    public Person(String name, String lastName, LatLng ubication, LatLng destiny) {
        this.name = name;
        this.lastName = lastName;
        this.ubication = ubication;
        this.destiny = destiny;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LatLng getUbication() {
        return ubication;
    }

    public void setUbication(LatLng ubication) {
        this.ubication = ubication;
    }

    public LatLng getDestiny() {
        return destiny;
    }

    public void setDestiny(LatLng destiny) {
        this.destiny = destiny;
    }
}
