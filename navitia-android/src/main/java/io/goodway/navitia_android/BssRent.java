package io.goodway.navitia_android;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Alexis Robin
 * @version 0.6
 * Licensed under the Apache2 license
 */
public class BssRent extends WayPart implements Parcelable{

    protected BssRent(Address from, Address to, double co2Emission, String departureDateTime, String arrivalDateTime, int duration, GeoJSON geoJSON) {
        super("Bike Rent", from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJSON, WayPartType.BssRent);
    }

    protected BssRent(Parcel in){
        super(in);
        this.wayPartType = WayPartType.BssRent;
    }

    @Override
    public String toString(){
        return "Prendre un vélo à " + this.getTo().toString();
    }

    @Override
    public String getLabel(Context context) {
        return context.getString(R.string.navitia_bss_renting)+" "+this.getTo().toString();
    }

    @Override
    public String getAction(Context context) {
        return context.getString(R.string.navitia_bss_rent);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator CREATOR =
            new Creator() {
                @Override
                public Object createFromParcel(Parcel in) {
                    return new BssRent(in) {
                    };
                }

                public BssRent[] newArray(int size) {
                    return new BssRent[size];
                }
            };
}
