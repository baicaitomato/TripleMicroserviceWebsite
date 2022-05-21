package ca.utoronto.utm.mcs;

// import com.mongodb.client.MongoCollection;
import com.mongodb.BasicDBObject;
import com.mongodb.client.*;
import com.mongodb.client.result.UpdateResult;
import io.github.cdimascio.dotenv.Dotenv;
import org.bson.Document;
import org.bson.types.ObjectId;
import static com.mongodb.client.model.Filters.eq;

import javax.xml.transform.Result;

public class MongoDao {
	
	public MongoCollection<Document> collection;

	public MongoDao() {
        // TODO: 
        // Connect to the mongodb database and create the database and collection. 
        // Use Dotenv like in the DAOs of the other microservices.
		String password = "123456";
		String username = "root";
		String dbName = "trip";
		String collectionName = "trips";

		Dotenv dotenv = Dotenv.load();
		String addr = dotenv.get("MONGODB_ADDR");
		String uriDb = String.format("mongodb://%s:%s@%s:27017", username, password, addr);
		try {
			MongoClient mongoClient = MongoClients.create(uriDb);
			MongoDatabase database = mongoClient.getDatabase(dbName);
			this.collection = database.getCollection(collectionName);
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}

	// *** implement database operations here *** //

	public FindIterable<Document> getTripsByOid(String oid){
		return this.collection.find(eq("_id", new ObjectId(oid)));
	}

	public MongoCursor<Document> getTripsByPassengerUid(String uid){
		FindIterable<Document> trips = this.collection.find(new Document("passenger", uid));
		return trips.iterator();
	}

	public MongoCursor<Document> getTripsByDriverUid(String uid){
		FindIterable<Document> trips = this.collection.find(new Document("driver", uid));
		return trips.iterator();
	}

	public ObjectId postTrip(String driver, String passenger, Integer startTime){
		Document doc = new Document();
		doc.put("driver", driver);
		doc.put("passenger", passenger);
		doc.put("startTime", startTime);
		try {
			this.collection.insertOne(doc);
			return doc.getObjectId("_id");
		} catch (Exception e) {
			System.out.println("Error occurred");
		}
		return doc.getObjectId("_id");
	}

	public Boolean patchTrip(String _id, Integer distance , Integer endTime, Integer timeElapsed, String totalCost) {
//		Document doc = new Document("_id", _id);

		// add fields to existing item
		BasicDBObject new_trip = new BasicDBObject();
		new_trip.put("distance", distance);
		new_trip.put("endTime", endTime);
		new_trip.put("timeElapsed", timeElapsed);
		new_trip.put("totalCost", totalCost);


		try {
			UpdateResult aaa = this.collection.updateOne(eq("_id", new ObjectId(_id)), new Document("$set", new_trip));
			return true;
		}
		catch (Exception e){
			return false;
		}

	}
}
