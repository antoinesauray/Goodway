package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by root on 6/13/15.
 */
public class Event implements Parcelable {

    private int id;
    private String name, description, date;
    private double latitude, longitude;

    public final static String BASEURL="http://gorilla.goodway.io/event_";

    public static final Creator CREATOR =
            new Creator() {
                public Event createFromParcel(Parcel in) {
                    return new Event(in);
                }

                public Event[] newArray(int size) {
                    return new Event[size];
                }
            };

    public Event(int id, String name, String description, String date, double latitude, double longitude){
        this.id = id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Event(Parcel in){
        readFromParcel(in);
    }

    public String getName(){
        return name;
    }

    public String getDescription(){
        return description;
    }

    public String getDate(){return date;}

    public int getId(){return id;}

    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(date);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        description = in.readString();
        date = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }
}
