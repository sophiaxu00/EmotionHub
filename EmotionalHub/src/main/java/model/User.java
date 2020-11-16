package model;

import org.bson.types.ObjectId;

/** Represents a User and their identifying
 *  information.
 */
public class User {

    // User ID
    private ObjectId _id;
    // User email address (unique)
    private String email;
    // User password
    // TODO: Hash password in User, in DB, or both
    private String password;
    // User screen name
    private String userName;

    /** Constructs a new User from the given email,
     *  password, and username.
     *
     *  @param email Email address of user.
     *  @param password Password used by user for login.
     *  @param userName Name visible to other Users.
     */
    public User(String email, String password, String userName) {
        this.email = email;  // Email will be the identifying info for a user.
        this.password = password;
        this.userName = userName;
    }

    /** Returns ID of User.
     *
     *  @return ObjectId of User.
     */
    public ObjectId getUserID() { return _id; }

    /** Sets the ID of the User.
     *
     *  @param _id New ObjectId of User.
     */
    public void setUserID(ObjectId _id) { this._id = _id; }

    /** Returns the email address of User.
     *
     *  @return String email address.
     */
    public String getEmail() {
        return email;
    }

    /** Sets email address for User.
     *
     *  @param email New email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Returns password of User.
     *
     *  @return String password.
     */
    public String getPassword() {
        return password;
    }

    /** Sets the password for the User.
     *
     *  @param password New password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /** Returns the userName of the User.
     *
     *  @return String userName.
     */
    public String getUserName() {
        return userName;
    }

    /** Sets the userName of the User.
     *
     *  @param userName New userName.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** Returns a String representation of the User
     *  in list format.
     *
     *  @return String representation of User.
     */
    public String toString() {
        return "User: { " +
                "_id: " + _id.toString() + ", " +
                "email = " + email + ", " +
                "password = " + password + ", " +
                "userName = " + userName + " }";
    }
}
