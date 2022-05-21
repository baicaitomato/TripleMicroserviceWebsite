package ca.utoronto.utm.mcs;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Login extends Endpoint {

    /**
     * POST /user/login
     * @body email, password
     * @return 200, 400, 401, 404, 500
     * Login a user into the system if the given information matches the 
     * information of the user in the database.
     */
    
    @Override
    public void handlePost(HttpExchange r) throws IOException, JSONException {


        String body = Utils.convert(r.getRequestBody());
        JSONObject obj_body = new JSONObject(body);

        if (obj_body.has("email") && obj_body.has("password")) {
            ResultSet rs1;
            boolean resultHasNext;

            // make query to check if user with given email exists, return 500 if error
            try {
                rs1 = this.dao.getUsersFromEmail(obj_body.getString("email"));
                resultHasNext = rs1.next();
            } catch (SQLException e) {
                e.printStackTrace();
                this.sendStatus(r, 500);
                return;
            }

            // check if user with given email exists, return 404 if not
            if (!resultHasNext) {
                this.sendStatus(r, 404);
                return;
            }

            // check what values are present
            String email = null;
            String password = null;
            if (obj_body.has("email")) {
                if (obj_body.get("email").getClass() != String.class) {
                    this.sendStatus(r, 400);
                    return;
                }
                email = obj_body.getString("email");
            }
            if (obj_body.has("password")) {
                if (obj_body.get("password").getClass() != String.class) {
                    this.sendStatus(r, 400);
                    return;
                }
                password = obj_body.getString("password");
            }
            // if either one of email or password is still null, the login fail
            if (email == null || password == null) {
                this.sendStatus(r, 400);
                return;
            }

            // check if the given email and password matches the email and password of a user already registered in the
            // database
            try {
                if ((obj_body.getString("email").equals(rs1.getString("email"))) && (obj_body.getString("password").equals(rs1.getString("password")))) {
                    JSONObject resp = new JSONObject();
                    resp.put("uid", rs1.getInt("uid"));
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
        this.sendStatus(r, 400);
        return;
    }
}
