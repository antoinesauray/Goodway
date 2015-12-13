package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by root on 6/13/15.
 */
public class Event implements Parcelable {

    private int id, size;
    private String name, url, date;
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

    public Event(int id, String name, String url, String date, double latitude, double longitude, int size){
        this.id = id;
        this.name = name;
        this.url = url;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.size = size;
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

    public String getDate(){return date;}

    public int getId(){return id;}

    public int size(){return size;}

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
        dest.writeString(date);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(size);
    }
    private void readFromParcel(Parcel in) {
        id = in.readInt();
        name = in.readString();
        url = in.readString();
        date = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        size = in.readInt();
    }
}
