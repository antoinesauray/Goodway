package io.goodway.model.gtfs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public class Stop implements Parcelable{

    private String stop_name, stop_id;

    protected Stop(Parcel in) {
        stop_name = in.readString();
        stop_id = in.readString();
    }

    public static final Creator<Stop> CREATOR = new Creator<Stop>() {
        @Override
        public Stop createFromParcel(Parcel in) {
            return new Stop(in);
        }

        @Override
        public Stop[] newArray(int size) {
            return new Stop[size];
        }
    };

    public String getStop_name() {
        return stop_name;
    }

    public String getStop_id() {
        return stop_id;
    }

    public Stop(String stop_name, String stop_id) {

        this.stop_name = stop_name;
        this.stop_id = stop_id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(stop_name);
        parcel.writeString(stop_id);
    }
}
