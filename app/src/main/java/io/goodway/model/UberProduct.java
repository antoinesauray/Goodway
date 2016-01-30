package io.goodway.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by antoine on 28/12/15.
 */
public class UberProduct extends Uber implements Parcelable{

    private String surge_confirmation_href, surge_confirmation_id, distance_unit;
    private int pickup_estimate;
    public UberProduct(Uber uber, String surge_confirmation_href, String surge_confirmation_id,
                       String display, String currency_code, String distance_unit, int low_estimate, int high_estimate, int minimum, int duration_estimate,
                       double surge_multiplier, double distance_estimate, int pickup_estimate){
        super(uber.localized_display_name, high_estimate, minimum, duration_estimate, uber.estimate,
                distance_estimate, display, uber.product_id, low_estimate, surge_multiplier, currency_code);
        this.surge_confirmation_href = surge_confirmation_href;
        this.surge_confirmation_id = surge_confirmation_id;
        this.distance_unit = distance_unit;
        this.pickup_estimate = pickup_estimate;
    }

    public UberProduct(Parcel in){
        super(in);
        surge_confirmation_href = in.readString();
        surge_confirmation_id = in.readString();
        distance_unit = in.readString();
        pickup_estimate = in.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(surge_confirmation_href);
        dest.writeString(surge_confirmation_id);
        dest.writeString(distance_unit);
        dest.writeInt(pickup_estimate);
    }

    public static final Creator CREATOR =
            new Creator() {
                public UberProduct createFromParcel(Parcel in) {
                    return new UberProduct(in);
                }

                public UberProduct[] newArray(int size) {
                    return new UberProduct[size];
                }
            };

}
