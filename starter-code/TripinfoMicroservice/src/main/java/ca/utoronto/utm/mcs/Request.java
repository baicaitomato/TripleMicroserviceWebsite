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

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Request extends Endpoint {

    /**
     * POST /trip/request
     * @body uid, radius
     * @return 200, 400, 404, 500
     * Returns a list of drivers within the specified radius 
     * using location microservice. List should be obtained
     * from navigation endpoint in location microservice
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException,JSONException{
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String uid = null;
        Integer radius = null;

        // check what values are present
        if (deserialized.has("uid")) {
            if (deserialized.get("uid").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            uid = deserialized.getString("uid");
        }
        if (deserialized.has("radius")) {
            if (deserialized.get("radius").getClass() != Integer.class) {
                this.sendStatus(r, 400);
                return;
            }
            radius = deserialized.getInt("radius");
        }

        // if all the variables are still null then there's no variables in request so retrun 400
        if (uid == null || radius == null || radius < 0) {
            this.sendStatus(r, 400);
            return;
        }

        try {
            String uri_string = "http://locationmicroservice:8000/location/nearbyDriver/%s?radius=%d";
            uri_string = String.format(uri_string, uid, radius);
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
            JSONObject result_data = result.getJSONObject("data");
            Iterator<String> keys = result_data.keys();
            List<String> drivers = new ArrayList<>();
            while(keys.hasNext()) {
                drivers.add(keys.next());
            }

            JSONObject resp = new JSONObject();
            resp.put("data", drivers);
            this.sendResponse(r, resp, 200);
        } catch (Exception e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
        }
    }
}
