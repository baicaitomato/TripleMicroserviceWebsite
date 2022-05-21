package ca.utoronto.utm.mcs;

/**
 * Everything you need in order to send and recieve httprequests to
 * other microservices is given here. Do not use anything else to send
 * and/or recieve http requests from other microservices. Any other
 * imports are fine.
 */
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Drivetime extends Endpoint {

    /**
     * GET /trip/driverTime/:_id
     * @param _id
     * @return 200, 400, 404, 500
     * Get time taken to get from driver to passenger on the trip with
     * the given _id. Time should be obtained from navigation endpoint
     * in location microservice.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String oid = params[3];
            FindIterable<Document> trips;
            try {
                trips = this.dao.getTripsByOid(oid);
            } catch (Exception e) {
                this.sendStatus(r, 400);
                return;
            }
            MongoCursor<Document> cursor = trips.iterator();

            Document trip = null;
            if (cursor.hasNext()) {
                trip = cursor.next();
            } else {
                this.sendStatus(r, 404);
                return;
            }

            String driver_id = trip.getString("driver");
            String passenger_id = trip.getString("passenger");

            String uri_string = "http://locationmicroservice:8000/location/navigation/%s?passengerUid=%s";
            uri_string = String.format(uri_string, driver_id, passenger_id);
            URI uri;
            try {
                uri = new URI(uri_string);
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }
            HttpURLConnection connection = (HttpURLConnection)uri.toURL().openConnection();
            connection.setRequestMethod("GET");
            int status = connection.getResponseCode();
            if (status != 200) {
                this.sendStatus(r, status);
                return;
            }
            JSONObject result = new JSONObject(Utils.convert(connection.getInputStream()));
            int total_time = result.getJSONObject("data").getInt("total_time");

            JSONObject resp = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("arrival_time", total_time);
            resp.put("data", data);
            this.sendResponse(r, resp, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
