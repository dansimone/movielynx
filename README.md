# MovieLynx
***
A visualization of movie connections between actors.

## Overview
***
The project is split up into 3 modules:
* __DB Loader__ - loads actor/movie data into a Neo4J graph database.
* __Backend App__ - stateless Grizzly backend serving up a REST API that queries the underlying Neo4J DB
* __Frontend App__ - web app that talks to the Backend App and provides a visual representation of the connections between actors

## DB Loader
***
This module is invoked manually as a one-time step to load actor/movie data into your Neo4J DB of choice.  To use this module:
1) Download and extract the following actor/actress files to some actress.list/actress.list in some local directory:
    * ftp://ftp.fu-berlin.de/pub/misc/movies/database/actors.list.gz
    * ftp://ftp.fu-berlin.de/pub/misc/movies/database/actresses.list.gz
2) Fire up a local or remote [Neo4J](http://neo4j.com/) DB.
3) Set  environment variables:

        export ACTOR_FILES_DIR=<local_dir>
        export NEO4J_DB_URL=<neo4j_db_url>
        export NEO4J_DB_USER=<neo4j_db_user>
        export NEO4J_DB_PASSWORD=<neo4j_db_password>
4) Run DB Loader

        mvn clean install exec:java
## Backend App
***
TBD

## Frontend App
***
TBD

## Deployment to Heroku
***
TBD
### Live Deployment
***
TBD