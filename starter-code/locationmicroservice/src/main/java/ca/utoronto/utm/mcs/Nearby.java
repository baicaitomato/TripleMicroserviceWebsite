package ca.utoronto.utm.mcs;

import java.io.IOException;
import org.json.*;
import org.neo4j.driver.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;

public class Nearby extends Endpoint {

    /**
     * GET /location/nearbyDriver/:uid?radius=:radius
     * @param uid, radius
     * @return 200, 400, 404, 500
     * Get drivers that are within a certain radius around a user.
     */

    @Override
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String[] params = r.getRequestURI().toString().split("/");
        if (params.length != 4 || params[3].isEmpty()) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String urldata = params[3];
            String[] url_params = urldata.split("\\?");
            if (url_params.length != 2 || url_params[1].isEmpty()) {
                this.sendStatus(r, 400);
                return;
            }
            String uid = url_params[0];

            //get user location
            Double latitude = null;
            Double longitude = null;
            try {
                Result result = this.dao.getUserLocationByUid(uid);
                if (result.hasNext()) {
                    Record user = result.next();
                    longitude = user.get("n.longitude").asDouble();
                    latitude = user.get("n.latitude").asDouble();
                } else {
                    this.sendStatus(r, 404);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            //search nearby user
            int radius;
            try {
                radius = Integer.parseInt(url_params[1].split("=")[1]);
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 400);
                return;
            }
            Result result = this.dao.getUserInRadius(latitude, longitude, radius);
            if (result.hasNext()) {
                JSONObject res = new JSONObject();
                JSONObject data = new JSONObject();

                while (result.hasNext()) {
                    Record user = result.next();
                    String curr_user_uid = user.get("n.uid").asString();
                    Double curr_user_longitude = user.get("n.longitude").asDouble();
                    Double curr_user_latitude = user.get("n.latitude").asDouble();
                    String curr_user_street = user.get("n.street").asString();

                    JSONObject curr_user = new JSONObject();
                    curr_user.put("longitude", curr_user_longitude);
                    curr_user.put("latitude", curr_user_latitude);
                    curr_user.put("street", curr_user_street);
                    data.put(curr_user_uid, curr_user);
                }
                res.put("status", "OK");
                res.put("data", data);
                this.sendResponse(r, res, 200);
            } else {
                this.sendStatus(r, 404);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
