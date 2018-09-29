package com.diff.user.app.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Tranxit Technologies Pvt Ltd, Chennai
 */

public class PlacePredictions implements Serializable {

    public String strSourceLatitude = "";
    public String strSourceLongitude = "";
    public String strSourceLatLng = "";
    public String strSourceAddress = "";

    public String strDestLatitude = "";
    public String strDestLongitude = "";
    public String strDestLatLng = "";
    public String strDestAddress = "";
    private ArrayList<PlaceAutoComplete> predictions;

    public ArrayList<PlaceAutoComplete> getPlaces() {
        return predictions;
    }

    public void setPlaces(ArrayList<PlaceAutoComplete> places) {
        this.predictions = places;
    }

}
