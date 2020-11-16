package model;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/** Represents a Post and its contents made by a User. */
public class Post {

    // Post ID
    private ObjectId _id;
    // User ID of posting user
    private ObjectId user_id;
    // Textual content
    private String content;
    private ObjectId media_id;

    private Double valence;
    private Double arousal;
    private Double dominance;

    private int analyzed;

    // Post intentions returned by backend analysis
    // Invisible to the user
    // TODO: maybe using integers as identifiers instead
    private Map<String, Double> intentions;
    // Post topics assigned by users. Default empty
    // Visible to the users
    private List<String> topics;

    /** Reconstructs a Post from Database entry.
     *
     * @param user_id ObjectId associated to the posting User.
     * @param content Text content of post.
     * @param topics A list of topics associated with the post.
     * @param intentions A map from intentions to strength assigned by doing sentiment/
     *                   intention analysis.
     */
    public Post(ObjectId user_id, String content, Map<String, Double> intentions, List<String> topics) {
        this.user_id = user_id;
        this.content = content;
        this.intentions = intentions;
        this.topics = topics;
        this.media_id = null;
        this.analyzed = 0;
    }

    /** Constructs a new Post from a user with the given User ID and the given content.
     * Compute the associated intentions and initialize a set of topics (TBD).
     *
     * @param user_id ObjectId associated to the posting User.
     * @param content Text content of post.
     */
    public Post(ObjectId user_id, String content) {
        this.user_id = user_id;
        this.content = content;
        this.media_id = null;
        this.intentions = new HashMap<>();
        this.analyzed = 0;
        updateIntentions();
    }

    public void updateIntentions() {
        Map<String, Double> predMap = new VadPrediction().predict(content);
        if (predMap == null) {
            this.valence = 0.0;
            this.arousal = 0.0;
            this.dominance = 0.0;
        } else {
            // Get the predicted valence and arousal values (in range [1, 5]) and transform to range [-2, 2]
            this.valence = predMap.get("valence") - 3;
            this.arousal = predMap.get("arousal") - 3;
            this.dominance = predMap.get("dominance") - 3;
        }

        this.intentions = this.calculateIntentions();

        // TODO: right now set the default to be empty since the
        //      users may not assign topics to every post.
        //      Change later if needed.
        this.topics = new ArrayList<>();
        this.analyzed = 1;
    }

    /**
     * Calculates scores for each intention.
     * @return a map from the intention names to their scores.
     */
    // TODO: make a better algorithm to calculate the intentions.
    private HashMap<String, Double> calculateIntentions() {
        Double excite_s = this.valence + this.arousal;
        Double chill_s = this.valence - this.arousal;
        Double inspire_s = this.valence;
        Double laugh_s = this.valence;
        return new HashMap<String, Double>(){{
            put("excite", excite_s);
            put("chill", chill_s);
            put("inspire", inspire_s);
            put("laugh", laugh_s);
            put("opinions", 0.0);
        }};
    }

    /** Returns the ID of the Post.
     *
     *  @return ObjectId of Post.
     */
    public ObjectId getPostID() { return _id; }

    /** Sets the ID of the Post.
     *
     *  @param _id New ObjectId of Post.
     */
    public void setPostID(ObjectId _id) { this._id = _id; }

    /** Returns ID of posting User.
     *
     *  @return ObjectId of posting User.
     */
    public ObjectId getUserID() { return user_id; }

    /** Associates a new User to the given Post
     *
     *  @param user_id ObjectId of new associated
     *                 User.
     */
    public void setUserID(ObjectId user_id) { this.user_id = user_id; }

    /** Returns the content of the Post.
     *
     *  @return Text content of Post.
     */
    public String getContent() { return content; }

    /** Replaces content of the Post.
     *
     *  @param content New content of Post.
     */
    public void setContent(String content) { this.content = content; this.analyzed = 0; }

    /** Return intentions and their strength assigned to the post
     *
     *  @return a map from intentions to strength assigned to them.
     */
    public Map<String, Double> getIntentions() {
        return intentions;
    }

    /** Update the intention map with new strength assigned to them
     *
     *  @param intentions a map from intentions to updated strength
     */
    public void setIntentions(Map<String, Double> intentions) {
        this.intentions = intentions;
    }

    /** Return topics assigned to the post
     *
     *  @return a list of topic contents
     */
    public List<String> getTopics() {
        return topics;
    }

    /** Update the topic list
     *
     *  @param topics an updated list of topics
     */
    public void setTopics(List<String> topics) {
        this.topics = topics;
    }

    /** Find the media file related to the post
     *
     *  @return the ID of the media file in storage
     */
    public ObjectId getMediaId() { return this.media_id; }

    /** Set the media file related to the post
     *
     *  @param media_id ID of media file related to post
     */
    public void setMediaId(ObjectId media_id) { this.media_id = media_id; }

    /** Returns whether the post has been tagged with
     *  emotions and intentions
     * @return 1 if analyzed, 0 otherwise
     */
    public int isAnalyzed() { return this.analyzed; }

    /** Update the topics list
     *
     *  @param new_topics topics to be added to the list
     */
    public void addTopics(List<String> new_topics) {
        this.topics.addAll(new_topics);
    }

    /** Update the topics list
     *
     *  @param removed_topics topics to be removed from the list
     */
    public void deleteTopics(List<String> removed_topics) throws NullPointerException{
        this.topics.removeAll(removed_topics);
    }

    /**
     * Returns a String representation of the Post
     * in list format.
     *
     * @return String representation of Post.
     */
    public String toString() {
        return "Post{" +
                "_id=" + _id +
                ", user_id=" + user_id +
                ", content='" + content + '\'' +
                ", intentions=" + intentions +
                ", tags=" + topics +
                '}';
    }
}
