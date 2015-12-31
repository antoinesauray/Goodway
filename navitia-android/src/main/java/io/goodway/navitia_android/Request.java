package io.goodway.navitia_android;

import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.ArrayList;

/**
 * @author Alexis Robin
 * @version 0.6.1
 * Licensed under the Apache2 license
 */
public class Request {

    public static void getWays(Action a, ArrayList<Pair> pairs, Address from, Address to, ErrorAction error){
        try{
            new GetWays(a, pairs, from, to, error).execute();
            Log.d("getWays", "getWays");
        }
        catch(IllegalStateException e){
            Log.e(e.getMessage(), "exception");
        }

    }

    private static class GetWays extends AsyncTask<Pair, Way, Integer>{

    /*
        wiki : http://wiki.openstreetmap.org/wiki/Nominatim
        street=<housenumber> <streetname>
        city=<city>
        county=<county>
        state=<state>
        country=<country>
        postalcode=<postalcode>
        use q= if you don't know whether the user type an address, a city a county or whatever
    */

        private final String QUERY = "http://navitia.goodway.io/get_ways.php?";
        private Action action;
        private ErrorAction error;
        private ArrayList<Pair> pairs;
        private Address from, to;

        /**
         *
         * @param action The method to apply on each Place which is returned by nominatim
         * @param pairs A set of keys and values to provide to the request. Each map will be triggered in a different request
         * @see Action
         */
        public GetWays(Action action, ArrayList<Pair> pairs, Address from, Address to, ErrorAction error){
            this.action = action;
            this.error = error;
            this.pairs = pairs;
            this.from = from;
            this.to = to;
        }

        @Override
        protected Integer doInBackground(Pair... params) {
            StringBuilder jsonResult = new StringBuilder();
            StringBuilder sb = new StringBuilder(QUERY);
            int nbJourneys=0;
            int nbWays=0;
                for(Pair p : pairs){
                    sb.append(p.first+"=" + p.second+"&");
                    Log.d("p.first="+p.first+" & o.second"+p.second, "pairs");
                }

            Log.d("url", sb.toString());

                try {
                    URL url = new URL(sb.toString());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    InputStreamReader in = new InputStreamReader(conn.getInputStream());

                    BufferedReader jsonReader = new BufferedReader(in);
                    String lineIn;
                    while ((lineIn = jsonReader.readLine()) != null) {
                        jsonResult.append(lineIn);
                    }

                    try {
                        JSONObject jsonObj = new JSONObject(jsonResult.toString());
                        JSONArray ways = jsonObj.getJSONArray("ways");
                        nbWays = ways.length();

                        for (int i = 0; i < nbWays; i++) {

                            JSONObject way = ways.getJSONObject(i);

                            String label = way.optString("label");
                            double co2Emission = way.optDouble("co2Emission");
                            String arrivalDateTime = way.optString("arrivalDateTime");
                            String departureDateTime = way.optString("departureDateTime");
                            int duration = way.optInt("duration");

                            ArrayList<WayPart> parts = new ArrayList<WayPart>();
                            JSONArray wayParts = way.getJSONArray("wayParts");
                            int nbParts = wayParts.length();

                            WayPart tmpWayPart = null;
                            for (int j = 0; j < nbParts; j++) {

                                JSONObject wayPart = wayParts.getJSONObject(j);
                                tmpWayPart = getWayPart(wayPart);

                                if(tmpWayPart != null)
                                    parts.add(tmpWayPart);
                            }

                            publishProgress(new Way(label, from, to, co2Emission, departureDateTime, arrivalDateTime, duration, parts));
                        }

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            return nbWays;
        }


        @Override
        protected void onProgressUpdate(Way...progress){
            action.action(progress[0]);
            Log.d(progress[0].toString(), "entity");
        }

        protected void onPostExecute(Integer result){
            if(result==0){error.action(result);}
        }

        private Address getAddress(JSONObject address){

            Address ret = null;
            try {
                String type = address.getString("type");

                double lat  = address.getDouble("lat");
                double lon  = address.getDouble("lon");
                String name  = address.getString("name");

                if(type.equals("address")){
                    ret = new Address(name, lat, lon);
                } else if(type.equals("stop")){
                    String stopId = address.getString("stopId");
                    ret = new Stop(name, lat, lon, stopId);
                } else if(type.equals("timed_stop")){
                    String stopId = address.getString("stopId");
                    String departureDateTime = address.getString("departureDateTime");
                    String arrivalDateTime = address.getString("arrivalDateTime");
                    ret = new TimedStop(name, lat, lon, stopId, departureDateTime, arrivalDateTime);
                } else if(type.equals("bike_station")){
                    String stationId = address.getString("stationId");
                    ret = new BikeStation(name, lat, lon, stationId);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
        }

        private GeoJSON getGeoJSON(JSONObject obj){

            GeoJSON ret = null;

            try {

                if(obj.getString("type") == "LineString"){

                    int length = obj.getInt("length");
                    JSONArray coord = obj.getJSONArray("coordinates");

                    Coordinate[] coordinates = new Coordinate[coord.length()];
                    for(int k = 0; k < coord.length(); k++) {
                        coordinates[k] = new Coordinate(coord.getJSONArray(k).getDouble(0), coord.getJSONArray(k).getDouble(1));
                    }

                    ret = new GeoJSON("LineString", length, coordinates);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;
        }

        private WayPart getWayPart(JSONObject wayPart){

            WayPart ret = null;

            try {

                String type = wayPart.getString("type");
                Address from = getAddress(wayPart.getJSONObject("from"));
                Address to = getAddress(wayPart.getJSONObject("to"));
                double co2Emission = Double.parseDouble(wayPart.getString("co2Emission"));
                String departureDateTime = wayPart.getString("departureDateTime");
                String arrivalDateTime = wayPart.getString("arrivalDateTime");
                int duration = wayPart.getInt("duration");
                if(type.equals("walking")){

                    GeoJSON geoJson = getGeoJSON(wayPart.getJSONObject("geoJson"));
                    ret = new Walking(from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJson);

                } else if(type.equals("public_transport")){

                    String routeId = wayPart.getJSONObject("route").getString("id");
                    String routeName = wayPart.getJSONObject("route").getString("name");
                    String lineId = wayPart.getJSONObject("route").getJSONObject("line").getString("id");
                    String lineName = wayPart.getJSONObject("route").getJSONObject("line").getString("name");
                    String lineColor = wayPart.getJSONObject("route").getJSONObject("line").getString("color");
                    String networkId = wayPart.getJSONObject("route").getJSONObject("line").getString("networkId");
                    Route route = new Route(routeId, routeName, new Line(lineId, lineName, lineColor, networkId));

                    String vehicleId = wayPart.getString("vehicleId");
                    String vehicleType = wayPart.getString("vehicleType");

                    JSONArray s = wayPart.getJSONArray("stops");
                    ArrayList<TimedStop> timedStops = new ArrayList<>();
                    for(int i = 0; i < s.length(); i++) {
                        timedStops.add((TimedStop) getAddress(s.getJSONObject(i)));
                    }

                    GeoJSON geoJson = getGeoJSON(wayPart.getJSONObject("geoJson"));

                    ret = new BusTrip(from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJson, route, vehicleId, vehicleType, timedStops);

                } else if(type.equals("waiting")){

                    ret = new Waiting(co2Emission, departureDateTime, arrivalDateTime, duration);

                } else if(type.equals("transfer")){

                    GeoJSON geoJson = getGeoJSON(wayPart.getJSONObject("geoJson"));
                    ret = new Transfer(from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJson);

                } else if(type.equals("bike")){

                    GeoJSON geoJson = getGeoJSON(wayPart.getJSONObject("geoJson"));
                    ret = new Biking(from, to, co2Emission, departureDateTime, arrivalDateTime, duration, geoJson);

                } else if(type.equals("bss_rent")){

                    ret = new BssRent(from, to, co2Emission, departureDateTime, arrivalDateTime, duration, null);

                } else if(type.equals("bss_put_back")){

                    ret = new BssPutBack(from, to, co2Emission, departureDateTime, arrivalDateTime, duration, null);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;

        }

    }
}
