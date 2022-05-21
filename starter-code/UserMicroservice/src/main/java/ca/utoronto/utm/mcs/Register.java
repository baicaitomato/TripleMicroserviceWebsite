package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.sql.*;

public class Register extends Endpoint {

    /**
     * POST /user/register
     * @body name, email, password
     * @return 200, 400, 500
     * Register a user into the system using the given information.
     */

    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);

        String email = null;
        String name = null;
        String password = null;

        // check what values are present
        if (deserialized.has("email")) {
            if (deserialized.get("email").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            email = deserialized.getString("email");
        }
        if (deserialized.has("name")) {
            if (deserialized.get("name").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            name = deserialized.getString("name");
        }
        if (deserialized.has("password")) {
            if (deserialized.get("password").getClass() != String.class) {
                this.sendStatus(r, 400);
                return;
            }
            password = deserialized.getString("password");
        }

        // if all the variables are still null then there's no variables in request so retrun 400
        if (email == null || name == null || password == null) {
            this.sendStatus(r, 400);
            return;
        }

        // update db, return 500 if error
        try {
            ResultSet rs;
            boolean resultHasNext;
            rs = this.dao.getUsersFromEmail(email);
            resultHasNext = rs.next();
            // check if user was found, return 404 if not found
            if (resultHasNext) {
                this.sendStatus(r, 409);
                return;
            } else {
                ResultSet rs_uid;
                boolean resultHasNext_uid;
                rs_uid = this.dao.createUser(email, password, name);
                resultHasNext_uid = rs_uid.next();
                if (!resultHasNext_uid) {
                    this.sendStatus(r, 500);
                    return;
                }
                int uid = rs_uid.getInt("uid");
                JSONObject resp = new JSONObject();
                resp.put("uid", uid);
                this.sendResponse(r, resp, 200);
                return;
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            this.sendStatus(r, 500);
            return;
        }
    }
}
