package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by root on 6/13/15.
 */
public class Event implements Parcelable {

    private int id;
    private String name, s_time, e_time, url;
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

    public Event(int id, String name, String url, String s_time, String e_time, double latitude, double longitude){
        this.id = id;
        this.name = name;
        this.url = url;
        this.s_time = s_time;
        this.e_time = e_time;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Event(Parcel in){
        readFromParcel(in);
    }

    public String getName(){
        return name;
    }

    public String getUrl(){
        return url;
    }

    public String getS_time(){return s_time;}

    public String getE_time(){return e_time;}

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
        dest.writeString(url);
        dest.writeString(s_time);
        dest.writeString(e_time);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
        s_time = in.readString();
        e_time = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
    }
}
