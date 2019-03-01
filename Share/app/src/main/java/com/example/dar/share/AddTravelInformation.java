package com.example.dar.share;


import com.mapbox.mapboxsdk.geometry.LatLng;

public class AddTravelInformation {

    public LatLng Origin;
    public LatLng Destination;
    public String OriginString;
    public String DestinationString;
    public Integer Available;
    public Integer NoOfUsers;
    public Integer MinimumFare;
    public Integer MaximumFare;
    public Integer EstimatedTravelTime;

    public AddTravelInformation(LatLng origin, LatLng destination, String originString, String destinationString, Integer available, Integer noOfUsers, Integer fareFrom, Integer fareTo, Integer estimatedTravelTime){
        this.Origin = origin;
        this.Destination = destination;
        this.OriginString = originString;
        this.DestinationString = destinationString;
        this.Available = available;
        this.NoOfUsers = noOfUsers;
        this.MinimumFare = fareFrom;
        this.MaximumFare = fareTo;
        this.EstimatedTravelTime = estimatedTravelTime;
    }

}
