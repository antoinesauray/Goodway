package io.goodway.navitia_android;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Alexis Robin
 * @version 0.6
 * Licensed under the Apache2 license
 */
public class Biking extends WayPart implements Parcelable{

    protected Biking(Address from, Address to, double co2Emission, String departureDateTime, String arrivalDateTime, int duration, GeoJSON geoJSON) {
        super("Bike", from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJSON, WayPartType.Biking);
    }

    protected Biking(Parcel in){
        super(in);
        this.wayPartType = WayPartType.Biking;
    }

    @Override
    public String toString(){
        return "Rouler " + DataConverter.convertDurationToTime(this.getDuration()) + " jusqu'à " + this.getTo().toString();
    }

    @Override
    public String getLabel(Context context) {
        return context.getString(R.string.navitia_biking)+" "+this.getTo().toString();
    }

    @Override
    public String getAction(Context context) {
        return context.getString(R.string.navitia_bike);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator CREATOR =
            new Creator() {
                @Override
                public Object createFromParcel(Parcel in) {
                    return new Biking(in) {
                    };
                }

                public Biking[] newArray(int size) {
                    return new Biking[size];
                }
            };
}
