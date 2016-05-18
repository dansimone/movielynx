package com.simone.movielynx.backend;

import org.glassfish.grizzly.http.server.HttpServer;

/**
 * Placeholder for our backend server.
 */
public class Main {

    public static void main(String[] args) {

        int port = 8080;
        if( System.getenv("PORT") != null){
            port = Integer.valueOf(System.getenv("PORT"));
        }

        HttpServer server = HttpServer.createSimpleServer(null, port);
        try {
            server.start();
            System.out.println("Press any key to stop the server...");
            Thread.sleep(Integer.MAX_VALUE);
        } catch (Exception e) {
            System.err.println(e);
        }
    }
}
