package io.goodway.model;

/**
 * Created by antoine on 28/12/15.
 */
public class Uber {

    private double distance;
    private int low_estimate, high_estimate, minimum, duration, surge_multiplier;
    private String localized_display_name, estimate, display_name, product_id, currency_code;

    public Uber(String localized_display_name, int high_estimate, int minimum, int duration, String estimate, double distance, String display_name, String product_id, int low_estimate, int surge_multiplier, String currency_code){
        this.localized_display_name = localized_display_name;
        this.high_estimate = high_estimate;
        this.minimum = minimum;
        this.duration = duration;
        this.estimate = estimate;
        this.distance = distance;
        this.display_name = display_name;
        this.product_id = product_id;
        this.low_estimate = low_estimate;
        this.surge_multiplier = surge_multiplier;
        this.currency_code = currency_code;
    }

    public String getDisplayName(){return localized_display_name;}

    public int getDuration(){return duration;}

    public String getProduct_id(){return product_id;}

    public String toString(){return display_name;}
}
