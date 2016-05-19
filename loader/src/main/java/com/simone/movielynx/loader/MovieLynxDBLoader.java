package com.simone.movielynx.loader;

import java.io.*;
import java.util.*;

/**
 * Class to load actor/movie data into a Neo4J graph database.
 */
public class MovieLynxDBLoader {

    // Constants
    public static String ACTOR_FILE_DIR_ENV_VAR = "ACTOR_FILE_DIR";
    public static String NEO4J_DB_URL_ENV_VAR = "NEO4J_DB_URL";
    public static String NEO4J_DB_USER_ENV_VAR = "NEO4J_DB_USER";
    public static String NEO4J_DB_PASSWORD_ENV_VAR = "NEO4J_DB_PASSWORD";
    public static List<String> ACTOR_FILES = Arrays.asList("actors.list", "actresses.list");

    // Instance variables
    private Environment environment = null;
    private File actorFilesDir = null;
    private String dbURL = null;
    private String dbUser = null;
    private String dbPassword = null;

    public static void main(String args[]) throws Exception {
        MovieLynxDBLoader loader = new MovieLynxDBLoader();
        loader.load();
    }

    /**
     * Creates a new DB Loader with default environment.
     */
    public MovieLynxDBLoader() {
        this(new Environment());
    }

    /**
     * Creates a new DB Loader with a custom Environment.
     */
    public MovieLynxDBLoader(Environment environment) {
        this.environment = environment;
        if (environment == null) {
            throw new IllegalArgumentException("Invalid environment!");
        }

        // Verify actor file settings
        String actorFilesDirString = getVerifyEnvVariable(ACTOR_FILE_DIR_ENV_VAR, environment);
        actorFilesDir = new File(actorFilesDirString);
        if (!actorFilesDir.exists()) {
            throw new IllegalArgumentException("Non-existent actors file directory: " + actorFilesDirString);
        }
        List<String> fileList = new ArrayList<>();
        if (actorFilesDir.list() != null) {
            fileList = Arrays.asList(actorFilesDir.list());
        }
        for (String expectedActorFile : ACTOR_FILES) {
            if (!fileList.contains(expectedActorFile)) {
                throw new IllegalArgumentException("Actor file directory must contain " + expectedActorFile);
            }
        }

        // Verify Neo4J settings
        dbURL = getVerifyEnvVariable(NEO4J_DB_URL_ENV_VAR, environment);
        dbUser = getVerifyEnvVariable(NEO4J_DB_USER_ENV_VAR, environment);
        dbPassword = getVerifyEnvVariable(NEO4J_DB_PASSWORD_ENV_VAR, environment);
    }

    /**
     * Loads our Environment's actor files into our given Neo4J DB.
     *
     * @throws IOException if an error occurs reading actor data or loading into Neo4J
     */
    public void load() throws IOException {

        // Build up a master map of all actors->movies
        Map<String, List<String>> masterMap = new HashMap<>();
        ActorFileParser actorFileParser = new ActorFileParser();
        for (String actorFileName : ACTOR_FILES) {
            File actorFile = new File(actorFilesDir.getAbsoluteFile() + File.separator + actorFileName);
            masterMap.putAll(actorFileParser.parseMovieList(new FileInputStream(actorFile)));
        }

/*
        System.out.println("LOADING");


        int i = 1;
        int total = map.keySet().size();
        Label actorLabel = DynamicLabel.label("Actor");
        Label movieLabel = DynamicLabel.label("Movie");

        Transaction tx = neo4jDB.beginTx();
        long stamp = System.currentTimeMillis();
        for (String actor : map.keySet()) {
            if (i % 1000 == 0) {
                tx.success();
                tx = neo4jDB.beginTx();
                System.out.println(actor + " %" + Math.round((double) i / (double) total * 10000) / (double) 100);
                long x = System.currentTimeMillis();
                System.out.println(x - stamp);
                stamp = x;
            }
            //System.out.println(i++);

            Node actorNode = null;
            try {
                actorNode = neo4jDB.findNode(actorLabel, "id", actor);
            } catch (NoSuchElementException e) {
                //System.out.println("GOT IT FOR " + actor);
            }
            if (actorNode == null) {
                actorNode = neo4jDB.createNode(actorLabel);
            }
            actorNode.setProperty("id", actor);

            for (String movie : map.get(actor)) {
                //System.out.println("   " + movie);
                Node movieNode = null;
                try {
                    movieNode = neo4jDB.findNode(movieLabel, "id", movie);
                } catch (NoSuchElementException e) {
                    //System.out.println("GOT IT FOR " + actor + " " + movie);
                }
                if (movieNode == null) {
                    movieNode = neo4jDB.createNode(movieLabel);
                }
                movieNode.setProperty("id", movie);
                boolean foundRelationship = false;
                for (Relationship r : actorNode.getRelationships(MyRelationshipTypes.ACTED_IN)) {
                    if (movieNode.equals(r.getEndNode())) {
                        foundRelationship = true;
                    }
                }
                if (!foundRelationship) {
                    actorNode.createRelationshipTo(movieNode, MyRelationshipTypes.ACTED_IN);
                }
            }

            i++;
        }
        tx.success();
    */
    }

    private String getVerifyEnvVariable(String variable, Environment environment) {
        String value = environment.getValue(variable);
        if (value == null) {
            throw new IllegalArgumentException(variable + " must be set in the environment");
        }
        return value;
    }
}
