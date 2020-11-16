package persistence;

import com.google.gson.*;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import model.User;
import org.bson.Document;
import org.bson.types.ObjectId;

/** Data access object for manipulating
 *  instances of Users in a MongoDB
 *  database.
 */
public class UserDaoMongo {

    // Active client connection
    private final MongoClient mongoClient;
    // Document collection representation of data
    private final MongoCollection<Document> collection;

    /** Constructs a new User DAO using the given
     *  client connection.
     *
     *  @param mongoClient Client connection used for
     *                     data access operations.
     */
    public UserDaoMongo(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.collection = mongoClient.getDatabase("db1").getCollection("User");
    }

    /**
     * Constructs a new User DAO.
     * @param mongoClient Client connection
     * @param test Whether this is a test instance.
     */
    public UserDaoMongo(MongoClient mongoClient, boolean test) {
        this.mongoClient = mongoClient;
        String dbName = test ? "test" : "db1";
        this.collection = mongoClient.getDatabase(dbName).getCollection("User");
    }

    /**
     * @param user The user to be added.
     * @return Whether the user is added successfully.
     * TODO: add more security
     */
    public boolean addUser(User user) {
        Document doc = new Document();
        doc.append("email", user.getEmail())
                .append("password", user.getPassword())
                .append("userName", user.getUserName());
        this.collection.insertOne(doc);
        return true;
    }

    /**
     * @param email The email of the user to be retrieved.
     * @return The user retrieved.
     * @throws NullPointerException if there is no user with the given email.
     */
    public User getUser(String email) throws NullPointerException {
        Document userDoc = this.collection.find(new Document("email", email)).first();
        User user = new User((String) userDoc.get("email"),
                (String) userDoc.get("password"),
                (String) userDoc.get("userName"));
        user.setUserID((ObjectId) userDoc.get("_id"));
        return user;
    }

    public User getUserById(ObjectId userId) {
        try {
            Document userDoc = this.collection.find(new Document("_id", userId)).first();
            User user = new User((String) userDoc.get("email"),
                    (String) userDoc.get("password"),
                    (String) userDoc.get("userName"));
            user.setUserID((ObjectId) userDoc.get("_id"));
            return user;
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO: (Heidi) implement this
    public boolean updateUser(User user) {
        return true;
    }

    // TODO: (Heidi) implement this
    public boolean deleteUser(String email) {
        return true;
    }

    public boolean deleteAllDocuments() {
        this.collection.deleteMany(new BasicDBObject());
        return true;
    }
}
