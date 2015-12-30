package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by antoine on 28/12/15.
 */
public class Uber implements Parcelable{

    protected double distance, surge_multiplier;
    protected int low_estimate, high_estimate, minimum, duration;
    protected String localized_display_name, estimate, display, product_id, currency_code;

    public Uber(String localized_display_name, int high_estimate, int minimum, int duration, String estimate, double distance, String display, String product_id, int low_estimate, double surge_multiplier, String currency_code){
        this.localized_display_name = localized_display_name;
        this.high_estimate = high_estimate;
        this.minimum = minimum;
        this.duration = duration;
        this.estimate = estimate;
        this.distance = distance;
        this.display = display;
        this.product_id = product_id;
        this.low_estimate = low_estimate;
        this.surge_multiplier = surge_multiplier;
        this.currency_code = currency_code;
    }

    public Uber(Parcel in){
        localized_display_name = in.readString();
        high_estimate = in.readInt();
        minimum = in.readInt();
        duration = in.readInt();
        estimate = in.readString();
        distance = in.readDouble();
        display = in.readString();
        product_id = in.readString();
        low_estimate = in.readInt();
        surge_multiplier = in.readDouble();
        currency_code = in.readString();
    }

    public String getDisplayName(){return localized_display_name;}

    public String getDisplay(){return display;}

    public int getDuration(){return duration;}

    public String getProduct_id(){return product_id;}

    public int getLowEstimate(){return low_estimate;}

    public int getHighEstimate(){return high_estimate;}

    public String getEstimate(){return estimate;}

    public String getCurrency_code(){return currency_code;}

    public String toString(){return localized_display_name;}

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(localized_display_name);
        dest.writeInt(high_estimate);
        dest.writeInt(minimum);
        dest.writeInt(duration);
        dest.writeString(estimate);
        dest.writeDouble(distance);
        dest.writeString(display);
        dest.writeString(product_id);
        dest.writeInt(low_estimate);
        dest.writeDouble(surge_multiplier);
        dest.writeString(currency_code);
    }

    public static final Creator CREATOR =
            new Creator() {
                public Uber createFromParcel(Parcel in) {
                    return new Uber(in);
                }

                public Uber[] newArray(int size) {
                    return new Uber[size];
                }
            };

}
