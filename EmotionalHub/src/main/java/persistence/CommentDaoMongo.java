package persistence;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import model.Comment;
import model.Post;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.set;

public class CommentDaoMongo {
    // Active client connection
    private final MongoClient mongoClient;
    // Document collection representation of data
    private final MongoCollection<Document> collection;

    public CommentDaoMongo(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        this.collection = mongoClient.getDatabase("db1").getCollection("Comment");
    }

    public ObjectId add(Comment comment) {
        ObjectId id;
        Document doc = new Document("_id", id =
                (comment.getId() != null ? comment.getId() : new ObjectId()))
                .append("post_id", comment.getPostId())
                .append("user_id", comment.getUserId())
                .append("anonymity", comment.isAnonymous())
                .append("content", comment.getContent())
                .append("parent_id", comment.getParentId());
        collection.insertOne(doc);
        comment.setId(id);
        return id;
    }

    public List<Comment> getCommentsByPost(ObjectId postId) {
        List<Document> elems = collection.find(eq("post_id", postId)).into(new ArrayList<>());
        List<Comment> comments = new ArrayList<>();
        elems.forEach(elem -> {
            Comment comment = new Comment(
                    (ObjectId) elem.get("post_id"),
                    (ObjectId) elem.get("user_id"),
                    (boolean) elem.get("anonymous"),
                    (String) elem.get("content"),
                    (ObjectId) elem.get("parent_id"));
            comment.setId((ObjectId) elem.get("_id"));
            comments.add(comment);
        });
        return comments;
    }

    public boolean update(Comment comment) {
        Bson filter = eq("_id", comment.getId());
        collection.findOneAndUpdate(filter, combine(
                set("content", comment.getContent()),
                set("anonymous", comment.isAnonymous())
        ));
        return true;
    }

    public boolean delete(Comment comment) {
        Bson filter = eq("_id", comment.getId());
        collection.findOneAndDelete(filter);
        return true;
    }
}
