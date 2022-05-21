package ca.utoronto.utm.mcs;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.bson.Document;

import java.io.IOException;

public class Passenger extends Endpoint {

    /**
     * GET /trip/passenger/:uid
     * @param r
     * @return 200, 400, 404
     * Get all trips the passenger with the given uid has.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException,JSONException{
        // TODO
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
        }

        // initialize userid and get all trips from database
        String uid = params[3];

        MongoCursor<Document> cursor = this.dao.getTripsByPassengerUid(uid);


        // send request using

        if (cursor.hasNext()) {
            try {
                JSONObject res = new JSONObject();
                JSONArray trips = new JSONArray();

                while (cursor.hasNext()) {

                    final Document obj = cursor.next();
                    JSONObject curr_trip = new JSONObject();
                    curr_trip.put("_id",  obj.get("_id"));
                    curr_trip.put("driver", (String) obj.get("driver"));
                    curr_trip.put("startTime", (Integer) obj.get("startTime"));
                    try{
                        curr_trip.put("distance", (Integer) obj.get("distance"));
                    }
                    catch (Exception ignored) {}
                    try{
                        curr_trip.put("endTime", (Integer) obj.get("endTime"));
                    }catch (Exception ignored) {}
                    try{
                        curr_trip.put("timeElapsed", (Integer) obj.get("timeElapsed"));
                    } catch (Exception ignored) {}
                    try{
                        curr_trip.put("totalCost", (String) obj.get("totalCost"));
                    } catch (Exception ignored) {}


                    trips.put(curr_trip);
                }
                JSONObject t = new JSONObject();
                t.put("trips", trips);
                res.put("status", "OK");
                res.put("data", t);
                this.sendResponse(r, res, 200);

            } catch (Exception e) {
                this.sendStatus(r, 500);
            }
        }
        else {
            this.sendStatus(r, 404);
        }
    }
}