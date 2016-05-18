package com.simone.movielynx.loader;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MovieLynxDBLoaderTest {
    private static MovieLynxDBLoader loader = null;
    private static String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "MovieLynxDBLoaderTest";

    @BeforeClass
    public static void staticPrepare() throws Exception {
        File tempDirFile = new File(tempDir + File.separator + "MovieLynxDBLoaderTest");
        FileUtils.deleteDirectory(tempDirFile);
        tempDirFile.mkdirs();
        List<String> lines = new ArrayList<>();
        lines.add("Name Titles\n");
        lines.add("----\t\t\t------\n");
        lines.add("Bar1, Foo1\tMovieA (1955)\n");
        lines.add("\tMovieB (1954)  <5>\n");
        lines.add("\tMovieC (1954)  <5>\n");
        lines.add("Bar2, Foo2\tMovieB (1955)\n");
        lines.add("\tMovieC (1954)  <5>\n");
        lines.add("Bar3, Foo31111dafaf\tMovieA (1955)\n");

        for (String requiredFile : MovieLynxDBLoader.REQUIRED_FILES) {
            File file = new File(tempDirFile.getAbsoluteFile() + File.separator + requiredFile);
            // System.out.println("AAA " + file.getAbsolutePath());
            file.createNewFile();
            FileUtils.writeLines(file, lines);
        }

        System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, tempDirFile.getAbsolutePath());
        loader = new MovieLynxDBLoader();
    }

    GraphDatabaseService neo4jDB = null;

    //@Test
    public void testE2E0() throws Exception {
        //System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, "d:\\tmp");

        MovieLynxDBLoader loader = new MovieLynxDBLoader();
        GraphDatabaseService neo4jDB = null;
        loader.loadInDB(neo4jDB);
    }


    //@Test
    public void testE2E1() throws Exception {
        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(tempDir)).
                setConfig(GraphDatabaseSettings.pagecache_memory, "50M")
                .setConfig(GraphDatabaseSettings.string_block_size, "60")
                .setConfig(GraphDatabaseSettings.array_block_size, "300").newGraphDatabase();
        new MovieLynxDBLoader().loadInDB(graphDb);

        String query = "MATCH (n) RETURN n.actor";
        Result result = graphDb.execute(query);


        List<String> actorNames = new ArrayList<>();
        while (result.hasNext()) {
            actorNames.add(result.next().get("n.name").toString());
        }
        assertEquals(actorNames, Arrays.asList("Carmencita Abad"));
        graphDb.shutdown();


    }

    @Test
    public void testEnvSettings1() throws Exception {
        new MovieLynxDBLoader();
    }

    //@Test
    public void testEnvSettings2MissingDir() throws Exception {
        String initialActorsDir = System.getProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR);
        System.clearProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR);
        boolean gotException = false;
        try {
            new MovieLynxDBLoader();
        } catch (Throwable e) {
            gotException = true;
        } finally {
            System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, initialActorsDir);
        }
        assertTrue(gotException);
    }

    //@Test
    public void testEnvSettings3MissingDir() throws Exception {
        String initialActorsDir = System.getProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR);
        System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, "foobar");
        boolean gotException = false;
        try {
            new MovieLynxDBLoader();
        } catch (Throwable e) {
            gotException = true;
        } finally {
            System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, initialActorsDir);
        }
        assertTrue(gotException);
    }

    //@Test
    public void testEnvSettings4EmptyDir() throws Exception {
        String initialActorsDir = System.getProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR);
        File tmpDir = File.createTempFile("MovieLynxDBLoaderTest-emptyDir", "actorfile");
        tmpDir.mkdir();
        System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, tmpDir.getAbsolutePath());
        boolean gotException = false;
        try {
            new MovieLynxDBLoader();
        } catch (Throwable e) {
            gotException = true;
        } finally {
            System.setProperty(MovieLynxDBLoader.ACTOR_FILES_DIR_ENV_VAR, initialActorsDir);
        }
        assertTrue(gotException);
    }

    @Test
    public void testIsStartMovieLine1() throws Exception {
        String line = "----\t\t\t------";
        assertEquals(true, loader.isMovieStartLine(line));
    }

    @Test
    public void testIsStartMovieLine2() throws Exception {
        String line = "foo";
        assertEquals(false, loader.isMovieStartLine(line));
    }

    @Test
    public void testActorFromLine1() throws Exception {
        String line = "'Noble Julz'Hamilton, Ulia\tStudZmen (2013)  (as Noble Julz)  [Deacon]";
        assertEquals("Ulia 'Noble Julz'Hamilton", loader.getActorFromLine(line));
    }

    @Test
    public void testActorFromLine2() throws Exception {
        String line = "Aalysha\tSong.null.drei (2003) (TV)  [Herself]";
        assertEquals("Aalysha", loader.getActorFromLine(line));
    }

    @Test
    public void testActorFromLine3() throws Exception {
        String line = "Aames, Marlene\tAlong the Navajo Trail (1945/I)  (uncredited)  [Gypsy Girl]";
        assertEquals("Marlene Aames", loader.getActorFromLine(line));
    }

    @Test
    public void testActorFromLine4() throws Exception {
        String line = "Aames, Marlene A";
        assertEquals(null, loader.getActorFromLine(line));
    }

    @Test
    public void testActorFromLine5() throws Exception {
        String line = "Aan, Nan\tLooking for America: A Saipan Story (2007) (TV)  [Bollywood Dancer]";
        assertEquals("Nan Aan", loader.getActorFromLine(line));
    }

    @Test
    public void testActorFromLine6() throws Exception {
        String line = "\tBon appétit (2010)  [Sara]  <9>";
        assertEquals(null, loader.getActorFromLine(line));
    }

    @Test
    public void testActorFromLine7() throws Exception {
        assertEquals(null, loader.getActorFromLine(""));
    }

    @Test
    public void testActorFromLine8() throws Exception {
        assertEquals(null, loader.getActorFromLine(null));
    }

    @Test
    public void testMovieFromLine1() throws Exception {
        String line = "\t\"Vida loca\" (2011) {Vida loca (#1.1)}  [Laura Hita Ferran]  <6>";
        assertEquals(null, loader.getMovieFromLine(line));
    }

    @Test
    public void testMovieFromLine2() throws Exception {
        String line = "\t\"Vida loca\" (2011) {Vida loca (#1.1)}  [Laura Hita Ferran]  <6>";
        assertEquals(null, loader.getMovieFromLine(line));
    }

    @Test
    public void testMovieFromLine3() throws Exception {
        String line = "Abajian, Agaby\tArmenia, My Love... (2016)  [Armenian Prisoner]";
        assertEquals("Armenia, My Love...", loader.getMovieFromLine(line));
    }

    @Test
    public void testMovieFromLine4() throws Exception {
        String line = "Abajian, Agaby\tArmenia, My Love...";
        assertEquals(null, loader.getMovieFromLine(line));
    }

    @Test
    public void testMovieFromLine5() throws Exception {
        String line = "Abajian, Agaby\t";
        assertEquals(null, loader.getMovieFromLine(line));
    }

    @Test
    public void testMovieFromLine6() throws Exception {
        assertEquals(null, loader.getMovieFromLine(""));
    }

    @Test
    public void testMovieFromLine7() throws Exception {
        assertEquals(null, loader.getMovieFromLine(null));
    }

    @Test
    public void parseMovieList1() throws Exception {
        StringBuffer sb = new StringBuffer();
        sb.append("THE ACTRESSES LIST\n");
        sb.append("        ==================\n");
        sb.append("\n");
        sb.append("Name Titles\n");
        sb.append("----\t\t\t------\n");
        sb.append("Abad, Carmencita\t1 2 3 (1955)\n");
        sb.append("\tAbarinding (1954)  <5>\n");
        sb.append("\tBayanihan (1960)\n");
        sb.append("\tBiyaya ng lupa (1959)  [Gloria]  <4>\n");
        sb.append("\tCuatro cantos (1960)  <5>\n");
        sb.append("\tDalawang ina (1957)\n");
        sb.append("\n");
        sb.append("Abad, Chantal\t\"Gracias por venir, gracias por estar\" (2012) {Homenaje a Armando " +
                "Manzanero (#2.34)}  [Herself - Chef]\n");
        sb.append("\t\"Hacete de Oliva\" (2013) {(2013-08-19)}  [Herself - Guest]\n");
        sb.append("\t\"Morfi, todos a la mesa\" (2015)  [Herself - Chef]  <4>\n");
        sb.append("\t\"Morfi, todos a la mesa\" (2015) {(2015-06-29)}  [Herself - Chef]  <4>\n");
        sb.append("\n");
        sb.append("Abad, Macarena\tConquista en Juego (2015)  [Carla]\n");
        sb.append("\tGoodbye (2016/II)  [Angie]  <1>\n");
        sb.append("\tThe Real Double (2014)  [Andrea]\n");
        sb.append("\tThe Remake (2015)  [Anika]\n");
        sb.append("\t\"Yo soy Bea\" (2006) {Máquinas de escribir contra la crisis (#3.125)} " +
                "[Nanny]\n");
        sb.append("\n");
        sb.append("Abad, Patricia (I)\tImpulso (2008)  <2>\n");
        sb.append("\n");
        sb.append("Abad, Ysabelle\t\"Foudre\" (2007) {Deux îles désertes (#2.2)}  [Gwen]\n");
        sb.append("\t\"Foudre\" (2007) {L'Australie (#2.7)}  [Gwen]\n");
        sb.append("\t\"Foudre\" (2007) {Seuls au monde (#2.1)}  [Gwen]\n");
        sb.append("\t\"Foudre\" (2007) {Une journée de chien (#2.5)}  [Gwen]\n");
        sb.append("\t\"Foudre\" (2007) {US Army (#2.3)}  [Gwen]\n");
        sb.append("\t\"Foudre\" (2007) {À poil (#2.6)}  [Gwen]\n");
        sb.append("\n");
        sb.append("Abad, Angeles\tTroyanas (2015)\n");
        sb.append("\n");
        sb.append("Abad-Santos, Ana\tApocalypse Child (2015)\n");
        sb.append("\tBen & Sam (2010)  (as Ana Abad Santos)  [Arlene Martinez]  <7>\n");
        sb.append("\tDry Rain (2009)  <1>\n");
        sb.append("\tFidel (2009)  [Malacanang Representative]\n");
        sb.append("\tMetro Manila (2013)  (as Ana Abad Santos)  [Dora Ong]  <8>\n");

        InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        Map<String, List<String>> actorMoveListMap = loader.parseMovieList(inputStream);

        Map<String, List<String>> expectedMap = new HashMap<>();
        List<String> list1 =
                Arrays.asList("1 2 3", "Abarinding", "Bayanihan", "Biyaya ng lupa", "Cuatro cantos", "Dalawang ina");
        expectedMap.put("Carmencita Abad", list1);
        List<String> list2 = Arrays.asList("Conquista en Juego", "The Real Double", "The Remake");
        expectedMap.put("Macarena Abad", list2);
        List<String> list3 = Arrays.asList("Impulso");
        expectedMap.put("Patricia (I) Abad", list3);
        List<String> list4 = Arrays.asList("Troyanas");
        expectedMap.put("Angeles Abad", list4);
        List<String> list5 = Arrays.asList("Apocalypse Child", "Ben & Sam", "Dry Rain", "Fidel", "Metro Manila");
        expectedMap.put("Ana Abad-Santos", list5);
        assertEquals(expectedMap, actorMoveListMap);
    }

    @Test
    public void parseMovieList2() throws Exception {
        StringBuffer sb = new StringBuffer();
        // No movie start line
        sb.append("THE ACTRESSES LIST\n");
        sb.append("        ==================\n");
        sb.append("\n");
        sb.append("Name Titles\n");
        sb.append("Abad, Carmencita        1 2 3 (1955)\n");
        sb.append("\tAbarinding (1954)  <5>\n");
        sb.append("\tBayanihan (1960)\n");
        sb.append("\tBiyaya ng lupa (1959)  [Gloria]  <4>\n");

        InputStream inputStream = new ByteArrayInputStream(sb.toString().getBytes(StandardCharsets.UTF_8));
        Map<String, List<String>> actorMoveListMap = loader.parseMovieList(inputStream);
        assertEquals(new HashMap<String, List<String>>(), actorMoveListMap);
    }

    //@Test
    public void foo() throws Exception {
        Map<String, List<String>> map = loader.parseMovieList(new FileInputStream("d:\\actresses.txt"));

        /*
        for (String actor : map.keySet()) {
            System.out.println(actor);
            for (String movie : map.get(actor)) {
                System.out.println("   " + movie);
            }
        }
        */
        System.out.println("AAA " + map.size());
        int totalMovies = 0;
        for (String actor : map.keySet()) {
            totalMovies += map.get(actor).size();
        }
        System.out.println("BBB " + totalMovies);
    }

    public void foo1() {


        GraphDatabaseService graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(new File(tempDir))
                .setConfig(GraphDatabaseSettings.pagecache_memory, "512M")
                .setConfig(GraphDatabaseSettings.string_block_size, "60")
                .setConfig(GraphDatabaseSettings.array_block_size, "300").newGraphDatabase();

        Node firstNode = graphDb.createNode();
        firstNode.setProperty("message", "Hello, ");
        Node secondNode = graphDb.createNode();
        secondNode.setProperty("message", "World!");

        Relationship relationship = firstNode.createRelationshipTo(secondNode, RelTypes.KNOWS);
        relationship.setProperty("message", "brave Neo4j ");

    }

    private static enum RelTypes implements RelationshipType {
        KNOWS
    }
}
