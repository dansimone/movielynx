package com.simone.movielynx.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to parse actor files: files of the form here: ftp://ftp.fu-berlin.de/pub/misc/movies/database/actors.list.gz
 * <p>
 * Some notes on the format of the actor file:
 * 1) The indicator of where the moviee list starts is "----\t\t\t------"
 * 2) An 'actor' line is of the form: "<name>\t<movie_name> (<date>)
 * 3) A 'movie only' line is of the form: "\t<movie_name> (<date>)
 * 4) Any lines that don't conform to the above format are ignored.
 */
public class ActorFileParser {
    private final static String MOVIE_START_LINE = "----\t\t\t------";

    /**
     * Given an InputStream to an actor file, parses it and returns a map of actor names to the
     * list of movies they've acted in.
     *
     * @param inputStream the InputStream to the actor file
     * @return a map of actor names to the list of movies they've acted in
     * @throws IOException if an error occurs reading from the InputStream
     */
    public Map<String, List<String>> parseMovieList(InputStream inputStream) throws IOException {
        Map<String, List<String>> actorToMovieListMap = new HashMap<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line = "";

            // First locate the "movie start" start
            boolean isStartMovieList = false;
            while (!isStartMovieList && (line = reader.readLine()) != null) {
                isStartMovieList = isMovieStartLine(line);
            }

            // Iterate through rest of the file, building up a movie list for each actor
            String currActor = null;
            List<String> currActorMovieList = null;
            while ((line = reader.readLine()) != null) {
                String lineActor = getActorFromLine(line);
                String lineMovie = getMovieFromLine(line);
                // Starting a new actor
                if (lineActor != null) {
                    // When starting a new actor, add the previous actor to the map
                    if (currActor != null && currActorMovieList.size() > 0) {
                        actorToMovieListMap.put(currActor, currActorMovieList);
                    }
                    currActor = lineActor;
                    currActorMovieList = new ArrayList<>();
                }

                if (lineMovie != null && currActorMovieList != null) {
                    currActorMovieList.add(lineMovie);
                }
            }

            // Add the final actor to the map
            if (currActorMovieList != null && currActorMovieList.size() > 0) {
                actorToMovieListMap.put(currActor, currActorMovieList);
            }
        } finally {
            inputStream.close();
        }

        return actorToMovieListMap;
    }


    /**
     * Parses the given line and returns the actor name contained in it, or
     * null if no actor is contained.
     *
     * @param line the line to parse
     * @return the actor name
     */
    public String getActorFromLine(String line) {
        if (line == null || line.length() == 0) {
            return null;
        }

        // Actor line must start with a non-whitespace character
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

    /**
     * Parses the given line and returns the movie name contained in it, or
     * null if no movie is contained.
     *
     * @param line the line to parse
     * @return the movie name
     */
    public String getMovieFromLine(String line) {
        if (line == null || line.length() == 0) {
            return null;
        }

        // First locate the long movie part of the line
        String longMoviePart = null;

        // Whitespace at the beginning means this is not an actor line
        if (Character.isWhitespace(line.charAt(0))) {
            longMoviePart = line.trim();
        }
        // Otherwise, remove the actor portion
        else {
            int tabIndex = line.indexOf("\t");
            if (tabIndex < 0) {
                return null;
            }
            longMoviePart = line.substring(tabIndex).trim();
        }

        // If the movie part starts with a quote, then it's a TV show, and we will disregard it
        if (longMoviePart.startsWith("\"")) {
            return null;
        }
        Pattern pattern = Pattern.compile("(.*) (\\(\\d+\\))(.*)");
        Matcher matcher = pattern.matcher(longMoviePart);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * Returns whether the given line represents the start of a new movie.
     *
     * @param line the line to parse
     * @return whether the line represents the start of a new movie
     */
    public boolean isMovieStartLine(String line) {
        return line != null && line.equals(MOVIE_START_LINE);
    }
}
