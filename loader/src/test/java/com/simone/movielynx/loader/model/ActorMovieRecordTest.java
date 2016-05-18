package com.simone.movielynx.loader.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Unit-level tests for ActorMovieRecord
 */
public class ActorMovieRecordTest {

    @Test
    public void testEquals1() throws Exception {
        assertEquals(new ActorMovieRecord("foo", "bar"), new ActorMovieRecord("foo", "bar"));
    }

    @Test
    public void testEquals2() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar"), new ActorMovieRecord("foo", "bar1"));
    }

    @Test
    public void testEquals3() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar"), new ActorMovieRecord("foo2", "bar"));
    }

    @Test
    public void testEquals4() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar"), new ActorMovieRecord(null, "bar"));
    }

    @Test
    public void testEquals5() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar"), new ActorMovieRecord(null, null));
    }

    @Test
    public void testEquals6() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar"), null);
    }

    @Test
    public void testToString() throws Exception {
        String expectedString = "ActorMovieRecord[actorName=foo, movieName=bar]";
        assertEquals(expectedString, new ActorMovieRecord("foo", "bar").toString());
    }

    @Test
    public void testHashCode1() throws Exception {
        assertEquals(new ActorMovieRecord("foo", "bar").hashCode(), new ActorMovieRecord("foo", "bar").hashCode());
    }

    @Test
    public void testHashCode2() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar").hashCode(), new ActorMovieRecord("foo", "bar1").hashCode());
    }

    @Test
    public void testHashCode3() throws Exception {
        assertNotEquals(new ActorMovieRecord("foo", "bar").hashCode(), new ActorMovieRecord(null, null).hashCode());
    }
}
