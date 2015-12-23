package io.goodway.navitia_android;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author Alexis Robin
 * @version 0.6
 * Licensed under the Apache2 license
 */
public class Waiting extends WayPart implements Parcelable{

    protected Waiting(double co2Emission, String departureDateTime, String arrivalDateTime, int duration) {
        super("Waiting", co2Emission, departureDateTime, arrivalDateTime, duration, WayPartType.Waiting);
    }

    protected Waiting(Parcel in){
        super(in);
        this.wayPartType = WayPartType.Waiting;
    }

    @Override
    public String toString(){
        return "Attendre " + DataConverter.convertDurationToTime(this.getDuration());
    }

    @Override
    public String getLabel(Context context) {
        return context.getString(R.string.navitia_waiting);
    }

    @Override
    public String getAction(Context context) {
        return context.getString(R.string.navitia_wait);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator CREATOR =
            new Creator() {
                @Override
                public Object createFromParcel(Parcel in) {
                    return new Waiting(in) {
                    };
                }

                public Waiting[] newArray(int size) {
                    return new Waiting[size];
                }
            };
}
