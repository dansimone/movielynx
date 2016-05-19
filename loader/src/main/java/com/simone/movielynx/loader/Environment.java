package com.simone.movielynx.loader;

/**
 * Class to manage access to environment variables.
 */
public class Environment {
    public String getValue(String name) {
        return System.getenv(name);
    }
}
