package io.goodway.model.gtfs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public class Route implements Parcelable {
    public Route(String route_id, String route_short_name) {
        this.route_id = route_id;
        this.route_short_name = route_short_name;
    }

    protected Route(Parcel in) {
        route_id = in.readString();
        route_short_name = in.readString();
    }

    public static final Creator<Route> CREATOR = new Creator<Route>() {
        @Override
        public Route createFromParcel(Parcel in) {
            return new Route(in);
        }

        @Override
        public Route[] newArray(int size) {
            return new Route[size];
        }
    };

    public String getRoute_id() {
        return route_id;
    }

    public String getRoute_short_name() {
        return route_short_name;
    }

    /*
        "route_id":"1-0","agency_id":1,"route_short_name":"1","route_long_name":"François Mitterrand / Jamet - Beaujoire / Ranzay","route_desc":null,"route_type":0,"route_url":null,"route_color":"007a45","route_text_color":"ffffff"
         */
    private String route_id, route_short_name;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(route_id);
        parcel.writeString(route_short_name);
    }
}
