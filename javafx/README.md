

# User interface for belltower

Controls for
- ringing bells
- accessing bells schedule
- get status of bell tower

# Scenes and Scene Model

A javafx Scene describes controls of a 'view'.
A SceneModel would contain the data of the 'view' as javafx Properties
A SceneModel would also contain methods to call the backend belltower service.

## Status Scene
The application would open with a read only status scene.
This contains:
- a read out of the upcoming bell schedule
- a command control to refresh the schedule *
- realtime status of the belltower 
- a list of bell patterns available with a control
- to select and ring any pattern *
- a link to another scene to control the Calendar.


The Status Scene includes a link to
the calendar and control scenes.

* If user is not authenticated, then this scene will pop-up the login scene
for authentication.

## Calendar Scene

The Calendar Scene has functions to read and modify the bell calendar.

The controls are focused on scheduling bells for a given date-time.
Repeatable and one-time-only ringing are supported.

If use is not authenticated, then this scene will pop-up the login scene
for authentication.

## Belltower Scene

For more details of control of the bell tower.
Also controls to ring a pattern and read the different patterns available

If use is not authenticated, then this scene will pop-up the login scene
for authentication.

## Login Scene

Collects credentials and drives a backend call to authentication and authorization.



# Getting started with Quarkus JavaFx

This is a minimal javafx application that access Quarkus API's.

Under the hood, this demo uses:

- `@QuarkusMain` to enable a custom main class

## Requirements

To compile and run this demo you will need:

- JDK 11+
- Maven 3.6.3+

### Configuring JDK 11

Make sure that `JAVA_HOME` environment variables have
been set, and that a JDK 11+ `java` command is on the path.

## Building the application

Launch the Maven build on the checked out sources of this demo:

> mvn package


Then run it:

> java -jar target/quarkus-javafx-example-1.0-runner.jar

