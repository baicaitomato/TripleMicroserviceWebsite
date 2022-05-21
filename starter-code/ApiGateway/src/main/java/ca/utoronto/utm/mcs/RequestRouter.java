package ca.utoronto.utm.mcs;

import java.io.IOException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.json.JSONObject;

/** 
 * Everything you need in order to send and recieve httprequests to 
 * the microservices is given here. Do not use anything else to send 
 * and/or recieve http requests from other microservices. Any other 
 * imports are fine.
 */
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.io.OutputStream;    // Also given to you to send back your response

public class RequestRouter implements HttpHandler {
	
    /**
     * You may add and/or initialize attributes here if you 
     * need.
     */
	public RequestRouter() {

	}

	@Override
	public void handle(HttpExchange r) throws IOException {
		System.out.println("===receive r===");
		final String frontend = "frontend";
		final String apigateway = "apigateway";
		final String locationmicroservice = "locationmicroservice";
		final String usermicroservice = "usermicroservice";
		final String tripinfomicroservice = "tripinfomicroservice";
		final int PORT = 8000;
		final String http = "http://";

		String[] uri = r.getRequestURI().getPath().split("/");
		if (uri.length < 2) {
			r.sendResponseHeaders(404, 0);
			return;
		}

		// System.out.printf("===get uri %s===\n", uri[1]);

		if (uri[1].equals("user")) {
			HttpResponse response = createRequest(r, usermicroservice, PORT);

			// System.out.printf("===success response %s===\n", usermicroservice);

			sendResponse(r, usermicroservice, response);
		} else if (uri[1].equals("location")) {
			HttpResponse response = createRequest(r, locationmicroservice, PORT);

			// System.out.printf("===success response %s===\n", usermicroservice);

			sendResponse(r, locationmicroservice, response);
		} else if (uri[1].equals("trip")) {
			HttpResponse response = createRequest(r, tripinfomicroservice, PORT);

			// System.out.printf("===success response %s===\n", usermicroservice);

			sendResponse(r, tripinfomicroservice, response);
		} else {
			r.sendResponseHeaders(404, 0);
		}
	}

	// cite: https://gist.github.com/JensWalter/0f19780d131d903879a2

	private HttpResponse createRequest(HttpExchange r, String microservice, int PORT){
		HttpClient client = HttpClient.newHttpClient();
		HttpResponse response;

		try{
			// System.out.println(r.getRequestURI());
			HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create("http://" + microservice + ":" + PORT + r.getRequestURI()))
				.method(r.getRequestMethod(),
						HttpRequest.BodyPublishers.ofString(Utils.convert(r.getRequestBody())))
				.build();
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
			return response;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private void sendResponse(HttpExchange r, String microservice, HttpResponse response) throws IOException{

		if (response != null) {
			try{
				// System.out.printf("response.body(): %s, response.body().getclass(): %s\n", response.body(), response.body().getClass());

				JSONObject responseBody = new JSONObject(response.body().toString());

				// System.out.println("===try to write===");

				writeResponse(r, responseBody, response.statusCode());
			} catch (Exception e) {
				e.printStackTrace();
				r.sendResponseHeaders(500, 0);
			}
		} else {
			System.out.printf("cannot go %s", microservice);
			r.sendResponseHeaders(500, 0);
		}
	}

	// cite: a1

	public void writeResponse(HttpExchange r, JSONObject json_response, int status) throws IOException{
		byte[] response = String.valueOf(json_response).getBytes();
		r.sendResponseHeaders(status, response.length);
		OutputStream os = r.getResponseBody();
		os.write(response);
		os.close();
	}
}
