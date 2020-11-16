package model;

import org.bson.types.ObjectId;

public class Comment {
    private ObjectId id;  // Comment ID
    private ObjectId postId;
    private ObjectId userId;
    private boolean anonymous;
    private String content;
    // The parent comment's id. Null if it is the root comment.
    private ObjectId parentId;

    public Comment(ObjectId postId, ObjectId userId, boolean anonymous, String content, ObjectId parentId) {
        this.postId = postId;
        this.userId = userId;
        this.anonymous = anonymous;
        this.content = content;
        this.parentId = parentId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getPostId() {
        return postId;
    }

    public void setPostId(ObjectId postId) {
        this.postId = postId;
    }
    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ObjectId getParentId() {
        return parentId;
    }

    public void setParentId(ObjectId parentId) {
        this.parentId = parentId;
    }
}
