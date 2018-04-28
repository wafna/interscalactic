# InterScalactic

An ongoing demo project in React and Scala using Akka, Slick, and whatever else looks useful or interesting.

Prêt-à-hack.

## Client

Demonstrates a concise system for managing React component state. 

## Server

The database layer is quite straight forward; it shovels futures from the database to the http threads on the routes. 

## Run

Run the api server from sbt using class wafna.interscalactic.InterScalacticServer.  The server can also server the 
production web build (from the root path on port 8080).

Run the web server from npm at client/web.  This will server the dev web content on port 3030 and is configured to 
use the API on port 8080.

The above can be done very conveniently in IDEA.
