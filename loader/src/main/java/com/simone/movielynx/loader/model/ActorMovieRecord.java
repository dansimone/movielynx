package com.simone.movielynx.loader.model;

import org.apache.commons.lang3.StringUtils;
import java.util.Objects;

/**
 * Class representing an Actor-Movie record.
 */
public class ActorMovieRecord {
    private String actorName = null;
    private String movieName = null;

    public ActorMovieRecord(String actorName, String movieName) {
        this.actorName = actorName;
        this.movieName = movieName;
    }

    public String getActorName() {
        return actorName;
    }

    public String getMovieName() {
        return movieName;
    }

    @Override
    public String toString() {
        return "ActorMovieRecord[actorName=" + actorName + ", movieName=" + movieName + "]";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof ActorMovieRecord) {
            ActorMovieRecord actorMovieRecord = (ActorMovieRecord) object;
            return StringUtils.equals(this.actorName, actorMovieRecord.getActorName()) &&
                    StringUtils.equals(this.movieName, actorMovieRecord.getMovieName());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.actorName, this.movieName);
    }
}
