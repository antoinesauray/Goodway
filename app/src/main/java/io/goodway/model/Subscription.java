package io.goodway.model;

/**
 * Created by Antoine Sauray on 5/24/2016.
 */
public class Subscription {

    public String getId_stop() {
        return id_stop;
    }

    public String getId_route() {
        return id_route;
    }

    public String getSchema() {
        return schema;
    }

    private String id_stop, id_route, schema;
    public Subscription(String id_stop, String id_route, String schema){
        this.id_stop = id_stop;
        this.id_route = id_route;
        this.schema = schema;
    }

    public String toString(){
        return schema+"/"+id_route+"-"+id_stop;
    }

}
