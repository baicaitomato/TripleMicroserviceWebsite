package ca.utoronto.utm.mcs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

public class Navigation extends Endpoint {

    /**
     * GET /location/navigation/:driverUid?passengerUid=:passengerUid
     * @param driverUid, passengerUid
     * @return 200, 400, 404, 500
     * Get the shortest path from a driver to passenger weighted by the
     * travel_time attribute on the ROUTE_TO relationship.
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
            String driverUid = url_params[0];
            String passengerUid;
            try {
                passengerUid = url_params[1].split("=")[1];
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 400);
                return;
            }

            //get user location
            String driverRoad;
            String passengerRoad;
            try {
                Result driver_result = this.dao.getDriver(driverUid);
                Result passenger_result = this.dao.getUserLocationByUid(passengerUid);
                if (driver_result.hasNext()) {
                    Record driver = driver_result.next();
                    driverRoad = driver.get("n.street").asString();
                } else {
                    this.sendStatus(r, 404);
                    return;
                }
                if (passenger_result.hasNext()) {
                    Record passenger = passenger_result.next();
                    passengerRoad = passenger.get("n.street").asString();
                } else {
                    this.sendStatus(r, 404);
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }


            Result result = this.dao.getNavigationRoute(driverRoad, passengerRoad);
            if (result.hasNext()) {
                JSONObject res = new JSONObject();
                JSONObject data = new JSONObject();

                Record route = result.next();
                Double total_cost = route.get("tc").asDouble();
                List<Object> streetIds = route.get("nodeIds").asList();
                List<Object> costs = route.get("costs").asList();

                JSONArray routes = new JSONArray();

                for (int i = 0; i < streetIds.size(); i ++) {
                    Result road = this.dao.getRoadId((Long)streetIds.get(i));
                    Record curr_road = road.next();
                    JSONObject curr_route = new JSONObject();
                    curr_route.put("street", curr_road.get("n.name").asString());
                    curr_route.put("has_traffic", curr_road.get("n.has_traffic").asBoolean());
                    curr_route.put("time", ((Double)costs.get(i)).intValue());
                    routes.put(curr_route);
                }

                data.put("total_time", total_cost);
                data.put("route", routes);
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
