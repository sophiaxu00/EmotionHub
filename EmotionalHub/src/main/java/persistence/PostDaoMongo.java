package persistence;

import com.mongodb.BasicDBObject;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import model.*;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.*;

/** Data access object for manipulating
 *  instances of Posts in a MongoDB
 *  database.
 */
public class PostDaoMongo {

    // Active client connection
    private final MongoClient mongoClient;
    // Document collection representation of data
    private final MongoCollection<Document> collection;

    /** Constructs a new Post DAO using the given
     *  client connection.
     *
     *  @param mongoClient Client connection used for
     *                     data access operations.
     */
    public PostDaoMongo(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.collection = mongoClient.getDatabase("db1").getCollection("Post");
    }

    /**
     * Constructs a new User DAO.
     * @param mongoClient Client connection
     * @param test Whether this is a test instance.
     */
    public PostDaoMongo(MongoClient mongoClient, boolean test) {
        this.mongoClient = mongoClient;
        String dbName = test ? "test" : "db1";
        this.collection = mongoClient.getDatabase(dbName).getCollection("Post");
    }

    /** Adds the given Post to the database.
     *
     *  @param post The Post to be added.
     *  @return The Post's ID, which is newly
     *          generated before insertion if
     *          not previously set.
     */
    public ObjectId add(Post post) {
        ObjectId pid;

        Map<String, Object> intentions_map = new HashMap<>(post.getIntentions());
        Document intentions = new Document(intentions_map);

        Document doc = new Document("_id", pid =
                (post.getPostID() != null ? post.getPostID() : new ObjectId()))
                .append("user_id", post.getUserID())
                .append("content", post.getContent())
                .append("intentions", intentions)
                .append("topics", post.getTopics())
                .append("media_id", post.getMediaId())
                .append("analyzed", post.isAnalyzed());
        collection.insertOne(doc);
        post.setPostID(pid);
        return pid;
    }

    /** Returns a list of all Posts currently in
     *  the database.
     *
     *  @return List of all Posts found in the
     *          database.
     */
    public List<Post> listAll() {
        List<Document> elems = collection.find().into(new ArrayList<>());
        List<Post> posts = new ArrayList<>();
        elems.forEach(elem -> {
            Post post = new Post((ObjectId) elem.get("user_id"),
                    (String) elem.get("content"),
                    (Map<String, Double>) elem.get("intentions"), // TODO: test it!
                    (List<String>) elem.get("topics"));
            post.setPostID((ObjectId) elem.get("_id"));
            posts.add(post);
        });
        return posts;
    }

    /**
     * Return a list of posts that corresponds to the given intent. A post
     * is selected if its score for the given intent is greater than or equal to 0.8
     *
     * @param intent One of "excite, chill, inspire, laugh, opinions, skip"
     * */
    public List<Post> list(String intent) {
        if ("skip".equals(intent)) {
            return listAll();
        }
        List<Document> elems = collection.find().into(new ArrayList<>());
        List<Post> posts = new ArrayList<>();
        elems.forEach(elem -> {
            Map<String, Double> intentScores = (Map<String, Double>) elem.get("intentions");
            // Skip posts that don't have a score (yet).
            if (intentScores != null && intentScores.containsKey(intent) &&
                    intentScores.get(intent) >= 0.8) {
                Post post = new Post((ObjectId) elem.get("user_id"),
                        (String) elem.get("content"),
                        intentScores, // TODO: test it!
                        (List<String>) elem.get("topics"));
                post.setPostID((ObjectId) elem.get("_id"));
                posts.add(post);
            }
        });
        return posts;
    }

    /** Updates the given Post in the database,
     *  using the Post ID to index and updating
     *  all other fields.
     *
     *  @param post The Post to be updated.
     *  @return Whether the post was successfully
     *          updated.
     */
    public boolean update(Post post) {
        Bson filter = eq("_id", post.getPostID());
        collection.findOneAndUpdate(filter, combine(
                set("content", post.getContent()),
                        set("intentions", post.getIntentions()),
                        set("topics", post.getTopics()),
                        set("analyzed", post.isAnalyzed()),
                        set("media_id", post.getMediaId())
        ));
        return true;
    }

    /** Deletes the given Post in the database,
     *  using the Post ID to index.
     *
     *  @param post The Post to be deleted.
     *  @return Whether the post was successfully
     *          deleted.
     */
    public boolean delete(Post post) {
        Bson filter = eq("_id", post.getPostID());
        collection.findOneAndDelete(filter);
        return true;
    }

    /**
     * Deletes the post specified by the id.
     */
    public boolean delete(ObjectId id) {
        Bson filter = eq("_id", id);
        collection.findOneAndDelete(filter);
        return true;
    }

    /** Finds and returns the Post in the database
     *  matching the given ID.
     *
     *  @param pid The ID of the Post to find.
     *  @return The Post matching the given ID.
     */
    public Post get(ObjectId pid) {
        Bson filter = eq("_id", pid);
        Document doc = collection.find(filter).into(new ArrayList<>()).get(0);
        Post post = new Post((ObjectId) doc.get("user_id"),
                (String) doc.get("content"),
                (Map<String, Double>) doc.get("intentions"),
                (List<String>) doc.get("topics") ); // TODO
        post.setPostID((ObjectId) doc.get("_id"));
        return post;
    }

    /** Finds and returns the Post in the database
     *  using the given filter.
     *
     *  @param filter The Bson filter.
     *  @return The Post matching the given ID.
     */
    public List<Post> query(Bson filter) {
        List<Document> elems = collection.find(filter).into(new ArrayList<>());
        List<Post> posts = new ArrayList<>();
        elems.forEach(elem -> {
            Post post = new Post((ObjectId) elem.get("user_id"),
                    (String) elem.get("content"),
                    (Map<String, Double>) elem.get("intentions"),
                    (List<String>) elem.get("topics") );
            post.setPostID((ObjectId) elem.get("_id"));
            posts.add(post);
        });
        return posts;
    }

    public boolean deleteAllDocuments() {
        this.collection.deleteMany(new BasicDBObject());
        return true;
    }

    public void addMedia(Post post, InputStream stream) throws IOException {
        GridFSBucket uploader = GridFSBuckets.create(
                mongoClient.getDatabase("db1"),
                "Files");
        GridFSUploadOptions options = new GridFSUploadOptions()
                .chunkSizeBytes(358400)
                .metadata(new Document("type", "presentation"));
        ObjectId id = uploader.uploadFromStream(post.getPostID().toString(), stream, options);
        post.setMediaId(id);
    }

    public void retrieveMedia(Post post) throws IOException {
        GridFSBucket downloader = GridFSBuckets.create(
                mongoClient.getDatabase("db1"),
                "Files");
        FileOutputStream output = new FileOutputStream("/tmp/" + post.getMediaId());
        downloader.downloadToStream(post.getMediaId(), output);
    }

}
