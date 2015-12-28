package io.goodway.navitia_android;

import android.os.Parcel;

/**
 * @author Alexis Robin
 * @version 0.6
 * Licensed under the Apache2 license
 */
public class BikeStation extends Address {

    private String stationId;

    public BikeStation(String name, double lat, double lon, String stationId){
        super(name, lat, lon);
        this.stationId = stationId;
    }

    public BikeStation(Parcel in){
        super(in);
        stationId = in.readString();
    }

    public String getStationId() {
        return stationId;
    }

    @Override
    public String toString(){
        return " station " + super.toString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(stationId);
    }

    public static final Creator CREATOR =
            new Creator() {

                @Override
                public Object createFromParcel(Parcel in) {
                    return new BikeStation(in);
                }

                public BikeStation[] newArray(int size) {
                    return new BikeStation[size];
                }
            };
}
