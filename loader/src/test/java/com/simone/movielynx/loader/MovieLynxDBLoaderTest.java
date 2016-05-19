package com.simone.movielynx.loader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MovieLynxDBLoaderTest {
    private static MovieLynxDBLoader defaultLoader = null;
    private static File workDir = null;

    @BeforeClass
    public static void staticPrepare() throws Exception {

        // Create a 'default' Loader that most tests below will use, including a valid directory
        // on disk with our actor files
        workDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "dbloadertest");
        FileUtils.deleteDirectory(workDir);
        workDir.mkdirs();

        for (String actorFileName : MovieLynxDBLoader.ACTOR_FILES) {
            File destActorFile = new File(workDir + File.separator + actorFileName);
            InputStream bundledFileInputStream = MovieLynxDBLoaderTest.class.getResourceAsStream("/" + actorFileName);
            if (bundledFileInputStream == null) {
                throw new Exception("Unable to find bundled file " + actorFileName);
            }

            FileOutputStream outputStream = new FileOutputStream(destActorFile);
            IOUtils.copy(bundledFileInputStream, outputStream);
            outputStream.close();
        }
        MockEnvironment env = new MockEnvironment();
        env.setValue(MovieLynxDBLoader.ACTOR_FILE_DIR_ENV_VAR, workDir.getAbsolutePath());
        env.setValue(MovieLynxDBLoader.NEO4J_DB_URL_ENV_VAR, "tbd");
        env.setValue(MovieLynxDBLoader.NEO4J_DB_USER_ENV_VAR, "tbd");
        env.setValue(MovieLynxDBLoader.NEO4J_DB_PASSWORD_ENV_VAR, "tbd");
        defaultLoader = new MovieLynxDBLoader(env);
    }

    @Test
    public void testEnvSettings1MissingAll() throws Exception {
        boolean gotException = false;
        try {
            new MovieLynxDBLoader(new MockEnvironment());
        } catch (Throwable e) {
            gotException = true;
        }
        assertTrue(gotException);
    }

    @Test
    public void testEnvSettings2MissingDBInfo() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setValue(MovieLynxDBLoader.ACTOR_FILE_DIR_ENV_VAR, workDir.getAbsolutePath());
        boolean gotException = false;
        try {
            new MovieLynxDBLoader(env);
        } catch (Throwable e) {
            gotException = true;
        }
        assertTrue(gotException);
    }

    @Test
    public void testEnvSettings3EmptyDir() throws Exception {
        File tmpDir = File.createTempFile("MovieLynxDBLoaderTest-emptyDir", "actorfile");
        tmpDir.mkdir();

        MockEnvironment env = new MockEnvironment();
        env.setValue(MovieLynxDBLoader.ACTOR_FILE_DIR_ENV_VAR, tmpDir.getAbsolutePath());
        env.setValue(MovieLynxDBLoader.NEO4J_DB_URL_ENV_VAR, "tbd");
        env.setValue(MovieLynxDBLoader.NEO4J_DB_USER_ENV_VAR, "tbd");
        env.setValue(MovieLynxDBLoader.NEO4J_DB_PASSWORD_ENV_VAR, "tbd");
        boolean gotException = false;
        try {
            new MovieLynxDBLoader();
        } catch (Throwable e) {
            gotException = true;
        }
        assertTrue(gotException);
    }

    /*
    public void foo1() {


        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(workDir)
                .setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
                .setConfig(GraphDatabaseSettings.string_block_size, "60")
                .setConfig(GraphDatabaseSettings.array_block_size, "300").newGraphDatabase();

        Node firstNode = graphDb.createNode();
        firstNode.setProperty("message", "Hello, ");
        Node secondNode = graphDb.createNode();
        secondNode.setProperty("message", "World!");

        //Relationship relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
        //relationship.setProperty("message", "brave Neo4j ");

    }

    @Test
    public void testE2E0() throws Exception {
        //System.setProperty(MovieLynxDBLoader.ACTOR_FILE_DIR_ENV_VAR, "d:\\tmp");

        MovieLynxDBLoader loader = new MovieLynxDBLoader();
        GraphDatabaseService neo4jDB = null;
        loader.load();
    }


    //@Test
    public void testE2E1() throws Exception {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(workDir).
                setConfig(GraphDatabaseSettings.pagecache_memory, "50M")
                .setConfig(GraphDatabaseSettings.string_block_size, "60")
                .setConfig(GraphDatabaseSettings.array_block_size, "300").se.newGraphDatabase();


        new MovieLynxDBLoader().load();

        String query = "MATCH (n) RETURN n.actor";
        Result result = graphDb.execute(query);


        List<String> actorNames = new ArrayList<>();
        while (result.hasNext()) {
            actorNames.add(result.next().get("n.name").toString());
        }
        assertEquals(actorNames, Arrays.asList("Carmencita Abad"));
        graphDb.shutdown();


    }
    */

    private static class MockEnvironment extends Environment {
        Map<String, String> envMap = null;

        public MockEnvironment() {
            envMap = new HashMap<>();
        }

        public void setValue(String name, String value) {
            envMap.put(name, value);
        }

        @Override
        public String getValue(String name) {
            return envMap.get(name);
        }
    }
}
