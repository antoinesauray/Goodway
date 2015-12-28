package io.goodway.navitia_android;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Alexis Robin
 * @version 0.6
 * Licensed under the Apache2 license
 */
public class BssPutBack extends WayPart implements Parcelable{

    protected BssPutBack(Address from, Address to, double co2Emission, String departureDateTime, String arrivalDateTime, int duration, GeoJSON geoJSON) {
        super("Bike Rent", from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJSON, WayPartType.BssPutBack);
    }

    protected BssPutBack(Parcel in){
        super(in);
        this.wayPartType = WayPartType.BssPutBack;
    }

    @Override
    public String toString(){
        return "Rendre le vélo à " + this.getFrom().toString();
    }

    @Override
    public String getLabel(Context context) {
        return context.getString(R.string.navitia_bss_puting_back)+" "+this.getTo().toString();
    }

    @Override
    public String getAction(Context context) {
        return context.getString(R.string.navitia_bss_put_back);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator CREATOR =
            new Creator() {
                @Override
                public Object createFromParcel(Parcel in) {
                    return new BssPutBack(in) {
                    };
                }

                public BssPutBack[] newArray(int size) {
                    return new BssPutBack[size];
                }
            };
}
