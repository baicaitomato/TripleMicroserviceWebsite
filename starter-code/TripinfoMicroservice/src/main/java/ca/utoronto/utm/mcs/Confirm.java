package ca.utoronto.utm.mcs;

import com.mongodb.util.JSON;
import com.sun.net.httpserver.HttpExchange;
import org.bson.types.ObjectId;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Confirm extends Endpoint {

    /**
     * POST /trip/confirm
     * @body driver, passenger, startTime
     * @return 200, 400
     * Adds trip info into the database after trip has been requested.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        // TODO
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String driver_id = null;
        String passenger_id = null;
        Integer startTime = null;

        // check what values are present
        if (deserialized.has("driver")) {
            if (deserialized.get("driver").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            driver_id = deserialized.getString("driver");
        }
        if (deserialized.has("passenger")) {
            if (deserialized.get("passenger").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            passenger_id = deserialized.getString("passenger");
        }
        if (deserialized.has("startTime")) {
            if (deserialized.get("startTime").getClass() != Integer.class) {
                this.sendStatus(r, 400);
                return;
            }
            startTime = deserialized.getInt("startTime");
        }

        // if all the variables are still null then there's no variables in request so retrun 400
        if (driver_id == null || passenger_id == null || startTime == null || startTime < 0) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            JSONObject res = new JSONObject();
            JSONObject data = new JSONObject();
            JSONObject oid = new JSONObject();

            ObjectId obj_id = this.dao.postTrip(driver_id, passenger_id, startTime);
            if (!obj_id.toString().equals("")){
                oid.put("$oid", obj_id);
                data.put("_id",oid);
                res.put("status", "OK");
                res.put("data", data);
                this.sendResponse(r, res, 200);
            } else {
                this.sendStatus(r, 500);
            }

        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
