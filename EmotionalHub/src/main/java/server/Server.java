package server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import model.Post;
import model.User;
import org.bson.types.ObjectId;
import persistence.PostDaoMongo;
import persistence.UserDaoMongo;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;

import javax.servlet.MultipartConfigElement;

import static com.mongodb.client.model.Filters.*;

import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static spark.Spark.*;

/** Runs a local server which provides
 *  RESTful endpoints to [NAME] pages and
 *  supported database operations.
 */
public class Server {

    // Active client connection
    private static MongoClient mongoClient;
    // User DAO created from client connection
    private static UserDaoMongo userDaoMongo;
    // Post DAO created from client connectionpytho
    private static PostDaoMongo postDaoMongo;

    // Local port number
    public static final int PORT_NUM = 7000;

    private static int getHerokuAssignedPort() {
        String herokuPort = System.getenv("PORT");
        if (herokuPort != null) {
            return Integer.parseInt(herokuPort);
        }
        return PORT_NUM;
    }

    private static List<Integer> generateEmptyBlockPositions() {
        List<Integer> lstPos = new ArrayList<>();
        Random rand = new Random();
        int numRows = 5;
        // Generate one empty position for each row, for 'numRow' rows.
        // Each row has 5 positions.
        for (int i = 0; i < numRows; i++) {
            lstPos.add(rand.nextInt(5) + i * 5);
        }
        return lstPos;
    }

    /** Runs server locally by binding to specified
     *  port.
     *
     *  @param args Unused.
     */
    public static void main(String[] args)  {
        // Connection initialization and DAO setup
        Logger.getLogger("org.mongodb.driver").setLevel(Level.WARNING);
        String connectionString = "mongodb+srv://mongo-db-user:lemonade@cluster0.nu1bq.mongodb.net/"
                + "<dbname>?retryWrites=true&w=majority";
        //connectionString = System.getenv("MONGODB_URI");

        mongoClient = new MongoClient(new MongoClientURI(connectionString));
        userDaoMongo = new UserDaoMongo(mongoClient);
        postDaoMongo = new PostDaoMongo(mongoClient);

        port(getHerokuAssignedPort());

        staticFiles.location("/public");

        // Landing page for color selection
        get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String email = req.cookie("userEmail");
            if (email != null) {
                //System.out.println(userDaoMongo.getUser(email).getUserName());
                //model.put("user", userDaoMongo.getUser(email));
                // TODO: figure out how to put user name here.
                model.put("userEmail", email);
                return new ModelAndView(model, "public/templates/color-selection.vm");
            } else {
                res.redirect("/login");
                return null;
            }
        }, new VelocityTemplateEngine());

        // Login page for registered users
        // Other pages requiring login redirect here
        // TODO: Clear cookies on logout
        get("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            return new ModelAndView(model, "public/templates/login.vm");
        }, new VelocityTemplateEngine());

        // Processes and accepts/rejects login request
        post("/login", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String email = req.queryParams("email");
            String pwd = req.queryParams("pwd");
            // Check the user exists and password is correct
            try {
                User user = userDaoMongo.getUser(email);
                if (!user.getPassword().equals(pwd)) {
                    throw new NullPointerException();
                }
                // Remember the user using cookie.
                // Note that the cookie cannot contain white space.
                res.cookie("userEmail", email);
                // user name may contain whitespace and thus is not a valid cookie!
                //res.cookie("userName", user.getUserName());
                res.redirect("/");
                return null;
            } catch(NullPointerException e) {
                model.put("failure", "true");
                ModelAndView mdl = new ModelAndView(model, "public/templates/login.vm");
                return new VelocityTemplateEngine().render(mdl);
            }
        });

        // Page for new users to register
        get("/adduser", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            return new ModelAndView(model, "public/templates/user-register.vm");
        }, new VelocityTemplateEngine());

        // This function helps to processes registration request
        // TODO: Validate email
        // TODO: Create password rules, confirm password, hash password
        post("/adduser", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            String email = req.queryParams("email");
            String pwd = req.queryParams("pwd");
            String userName = req.queryParams("userName");
            User user = new User(email, pwd, userName);

            boolean success = userDaoMongo.addUser(user);
            if (success) {
                model.put("success", "true");
            } else {
                model.put("failure", "true");
            }

            ModelAndView mdl = new ModelAndView(model, "public/templates/user-register.vm");
            return new VelocityTemplateEngine().render(mdl);
        });

        // This function returns feed of all posts from all users
        // In the future, we need to filter posts based on emotion tags, let users expand posts to full view,
        //let user edit/de;ete posts, and give user list of their owb post. 
        // TODO: Let users expand posts to full view
        // TODO: Let users edit/delete posts
        // TODO: Give users list of their own posts
        get("/feed", (req, res) -> {
            if (req.cookie("userEmail") == null) {
                res.redirect("/login");
                return null;
            }
            Map<String, Object> model = new HashMap<>();
            model.put("userEmail", req.cookie("userEmail"));
            // model.put("userName", req.cookie("userName"));

            String intent = req.queryParams("intent");
            if (intent == null) {
                String cookie_intent = req.cookie("intent");
                intent = (cookie_intent != null) ? cookie_intent : "skip";
            } else {
                res.cookie("intent", intent);
            }
            model.put("posts", postDaoMongo.list(intent));
            model.put("userDao", userDaoMongo);
            model.put("emptyPositions", generateEmptyBlockPositions());
            res.status(200);
            return new ModelAndView(model, "public/templates/feed.vm");
        }, new VelocityTemplateEngine());

        /*
        // This function processes and adds new post from a user
        // TODO: Add different types of content
        post("/make-post", (req, res) -> {
            // TODO: WHY???
            List<Post> toAnalyze = postDaoMongo.query(eq("analyzed", 0));
            toAnalyze.forEach(post -> {
                post.updateIntentions();
                postDaoMongo.update(post);
            });
            res.status(200);
            return null;
        });*/

        post("/feed", (req, res) -> {
            User user = userDaoMongo.getUser(req.cookie("userEmail"));
            req.attribute("org.eclipse.jetty.multipartConfig",
                    new MultipartConfigElement("/tmp"));
            req.raw().setAttribute("org.eclipse.jetty.multipartConfig",
                    new MultipartConfigElement("/tmp"));
            Post post = new Post(user.getUserID(), req.queryParams("content"));
            post.setPostID(new ObjectId());
            InputStream stream = req.raw().getPart("media").getInputStream();
            if (stream.available() != 0) {
                postDaoMongo.addMedia(post, stream);
            }
            postDaoMongo.add(post);
            res.status(201);
            res.redirect("/feed");
            return null;
        });

        get("/activity", (req, res) -> {
            if (req.cookie("userEmail") == null) {
                res.redirect("/login");
                return null;
            }
            Map<String, Object> model = new HashMap<>();
            User user = userDaoMongo.getUser(req.cookie("userEmail"));
            List<Post> posts = postDaoMongo.query(eq("user_id", user.getUserID()));
            model.put("userId", user.getUserID());
            model.put("posts", posts);
            res.status(200);
            return new ModelAndView(model, "public/templates/activity.vm");
        }, new VelocityTemplateEngine());

        post("/activity", (req, res) -> {
            if (req.cookie("userEmail") == null) {
                res.redirect("/login");
                return null;
            }
            if (req.queryParams("delete") != null) {
                ObjectId postID = new ObjectId(req.queryParams("delete"));
                postDaoMongo.delete(postID);
            }
            else if (req.queryParams("update") != null) {
                ObjectId postID = new ObjectId(req.queryParams("update"));
                User user = userDaoMongo.getUser(req.cookie("userEmail"));
                Post post = postDaoMongo.get(postID);
                post.setContent(req.queryParams("content"));
                postDaoMongo.update(post);
            }
            res.redirect("/activity");
            return null;
        });

    }
}
