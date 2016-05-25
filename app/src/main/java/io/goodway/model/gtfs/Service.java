package io.goodway.model.gtfs;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Antoine Sauray on 5/25/2016.
 */
public class Service implements Parcelable {
    public Service(String route_id, String trip_headsign, int direction) {
        this.route_id = route_id;
        this.trip_headsign = trip_headsign;
        this.direction = direction;
    }

    protected Service(Parcel in) {
        route_id = in.readString();
        trip_headsign = in.readString();
        direction = in.readInt();
    }

    public static final Creator<Service> CREATOR = new Creator<Service>() {
        @Override
        public Service createFromParcel(Parcel in) {
            return new Service(in);
        }

        @Override
        public Service[] newArray(int size) {
            return new Service[size];
        }
    };

    public String getRoute_id() {
        return route_id;
    }

    public String getTrip_headsign() {
        return trip_headsign;
    }

    public int getDirection() {
        return direction;
    }

    private String route_id, trip_headsign;
    private int direction;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(route_id);
        parcel.writeString(trip_headsign);
        parcel.writeInt(direction);
    }
}
