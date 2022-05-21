package ca.utoronto.utm.mcs;

import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpExchange;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Trip extends Endpoint {

    /**
     * PATCH /trip/:_id
     * @param r
     * @body distance, endTime, timeElapsed, totalCost
     * @return 200, 400, 404
     * Adds extra information to the trip with the given id when the 
     * trip is done. 
     */

    @Override
    public void handlePatch(HttpExchange r) throws IOException, JSONException {

        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 3 || params[2].isEmpty()) {
            System.out.println(1);
            this.sendStatus(r, 400);
        }

        // initialize userid and get all trips from database
        String _id = params[2];

        MongoCursor<Document> cursor = this.dao.getTripsByPassengerUid(_id);


        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        Integer distance = null;
        Integer endTime = null;
        Integer timeElapsed = null;
        String totalCost = null;

        // check what values are present
        if (deserialized.has("distance")) {
            if (deserialized.get("distance").getClass() != Integer.class) {
                this.sendStatus(r, 400);
                return;
            }
            distance = deserialized.getInt("distance");
        }
        if (deserialized.has("endTime")) {
            if (deserialized.get("endTime").getClass() != Integer.class) {
                this.sendStatus(r, 400);
                return;
            }
            endTime = deserialized.getInt("endTime");
        }
        if (deserialized.has("timeElapsed")) {
            if (deserialized.get("timeElapsed").getClass() != Integer.class) {
                this.sendStatus(r, 400);
                return;
            }
            timeElapsed = deserialized.getInt("timeElapsed");
        }
        if (deserialized.has("totalCost")) {
            if (deserialized.get("totalCost").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            totalCost = deserialized.getString("totalCost");
        }


        // if all the variables are still null then there's no variables in request so retrun 400
        if (distance == null || endTime == null || timeElapsed == null || totalCost == null ||
                distance < 0 || endTime < 0 || timeElapsed < 0) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            System.out.println("going into dao");
            Boolean exists = this.dao.patchTrip(_id, distance, endTime, timeElapsed, totalCost);
            System.out.println("finish dao");
            if (exists){
                this.sendStatus(r, 200);
            }
            this.sendStatus(r, 404);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
