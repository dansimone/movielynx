package com.simone.movielynx.loader;

import org.neo4j.graphdb.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class capable of reading an actor descriptor file from AWS and loading it into a
 * Neo4J DB.
 */
public class MovieLynxDBLoader {
    public static String ACTOR_FILES_DIR_ENV_VAR = "ACTOR_FILES_DIR";
    public static String ACTRESSES_FILE_NAME = "actresses.txt";
    public static String ACTORS_FILE_NAME = "actors.txt";
    public static List<String> REQUIRED_FILES = Arrays.asList(ACTRESSES_FILE_NAME);
    private File actorFilesDir = null;

    public static void main(String args[]) throws Exception {
        MovieLynxDBLoader loader = new MovieLynxDBLoader();

        //String x = "http://app50504514-yHvPJF:Rfi0BsF34DbBXa5UJfvZ@app50504514yhvpjf.sb02.stations.graphenedb" +
        //        ".com:24789";
        String x = "http://localhost:7474/db/data";
        System.out.println("Cnx to " + x);
        //GraphDatabaseService neo4jDB = new RestGraphDatabase(x, "neo4j", "123");
        GraphDatabaseService neo4jDB = null;


        loader.loadInDB(neo4jDB);
    }

    public MovieLynxDBLoader() {
        String actorFilesDirString = getEnvVarOrSystemProperty(ACTOR_FILES_DIR_ENV_VAR);
        if (actorFilesDirString == null) {
            throw new IllegalArgumentException(
                    "Actor files directory must be provided via environment variable or system property " +
                            ACTOR_FILES_DIR_ENV_VAR);
        }
        actorFilesDir = new File(actorFilesDirString);
        if (!actorFilesDir.exists()) {
            throw new IllegalArgumentException("Non-existent actors file directory: " + actorFilesDirString);
        }
        List<String> fileList = new ArrayList<>();
        if (actorFilesDir.list() != null) {
            fileList = Arrays.asList(actorFilesDir.list());
        }
        for (String expectedFile : REQUIRED_FILES) {
            if (!fileList.contains(expectedFile)) {
                throw new IllegalArgumentException(
                        "Actors file directory doesn't contain expected file: " + expectedFile);
            }
        }
    }

    public void loadInDB(GraphDatabaseService neo4jDB) throws Exception {
        File actressesFile = new File(actorFilesDir.getAbsoluteFile() + File.separator + ACTRESSES_FILE_NAME);
        File actorsFile = new File(actorFilesDir.getAbsoluteFile() + File.separator + ACTORS_FILE_NAME);


        Map<String, List<String>> map = parseMovieList(new FileInputStream(actressesFile));
        Map<String, List<String>> map1 = parseMovieList(new FileInputStream(actorsFile));
        map.putAll(map1);


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
            /*
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
            */
            i++;
        }
        tx.success();

    }


    enum MyRelationshipTypes implements RelationshipType {
        ACTED_IN
    }


    Map<String, List<String>> parseMovieList(InputStream inputStream) throws Exception {
        Map<String, List<String>> actorToMovieListMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

            // Search for the beginning
            boolean isStartMovieList = false;
            while (!isStartMovieList && (line = reader.readLine()) != null) {
                isStartMovieList = isMovieStartLine(line);
            }

            // At the movies section
            String currActor = null;
            List<String> currActorMovieList = null;
            while ((line = reader.readLine()) != null) {
                String lineActor = getActorFromLine(line);
                String lineMovie = getMovieFromLine(line);
                // Starting a new actor
                if (lineActor != null) {
                    // Add the previous actor to the Map at this point
                    if (currActor != null) {
                        if (currActorMovieList.size() > 0) {
                            actorToMovieListMap.put(currActor, currActorMovieList);
                        }
                    }
                    currActor = lineActor;
                    currActorMovieList = new ArrayList<>();
                }

                if (lineMovie != null) {
                    if (currActorMovieList != null) {
                        currActorMovieList.add(lineMovie);
                    }
                }
            }

            // Add the final actor to the Map, if the actor has at least 1 movie
            if (currActorMovieList != null) {
                if (currActorMovieList.size() > 0) {
                    actorToMovieListMap.put(currActor, currActorMovieList);
                }
            }

        } finally {
            try {
                inputStream.close();
            } catch (Throwable ignore) {
            }
        }
        return actorToMovieListMap;
    }

    boolean isMovieStartLine(String line) throws Exception {
        String movieStartLine = "----\t\t\t------";
        return line != null && line.equals(movieStartLine);
    }

    String getActorFromLine(String line) throws Exception {
        if (line == null || line.length() == 0) {
            return null;
        }

        // Actor line has to start with a non-whitespace
        if (Character.isWhitespace(line.charAt(0))) {
            return null;
        }

        // Actor name ends at the first occurrence of a tab
        int tabIndex = line.indexOf("\t");
        if (tabIndex < 0) {
            return null;
        }
        String rawActorName = line.substring(0, tabIndex);

        // If the raw actor name has a comma, use that to determine first and last name
        if (rawActorName.indexOf(",") >= 0) {
            int commaIndex = rawActorName.indexOf(",");
            String lastName = rawActorName.substring(0, commaIndex);
            String firstName = rawActorName.substring(commaIndex + 1);
            return firstName.trim() + " " + lastName.trim();
        } else {
            return rawActorName;
        }
    }

    String getMovieFromLine(String line) throws Exception {
        if (line == null || line.length() == 0) {
            return null;
        }

        String moviePart = null;
        if (Character.isWhitespace(line.charAt(0))) {
            moviePart = line.trim();
        } else {
            // First find the non-actor part of the line
            int tabIndex = line.indexOf("\t");
            if (tabIndex < 0) {
                return null;
            }
            moviePart = line.substring(tabIndex).trim();
        }

        // If the move part starts with a quote, then it's a TV show, and we will disregard it
        if (moviePart.startsWith("\"")) {
            return null;
        }
        Pattern pattern = Pattern.compile("(.*) (\\(\\d+\\))(.*)");
        Matcher matcher = pattern.matcher(moviePart);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    private String getEnvVarOrSystemProperty(String name) {
        String envValue = System.getenv(name);
        if (envValue != null) {
            return envValue;
        }
        String propValue = System.getProperty(name);
        if (propValue != null) {
            return propValue;
        }
        return null;
    }
}
