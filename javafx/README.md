

# User interface for belltower

Controls for
- ringing bells
- accessing bells timetable
- get status of the bell pattern player

To use the same code base for web and embedded display,
javafx is the UI programming technology.

For low memory footprint and dependency injection,
Quarkus is the overall development platform.

## Belltower Scene
The application would open with a read only status scene.
This contains:
- general events describing overall status change over time
- display describing overall instantaneous status
  - status of time service
  - status of remote service that provides timetable

## Timetable Scene

The Timetable Scene has functions to read the bell timetable.
The timetable gives the time and bell pattern to play.

## Belfry Scene

The Belfry scene gives details of the bell status,
whether it is idle or playing a pattern,
and what pattern it is playing.

## Login Scene

Collects credentials and drives a backend call to authentication and authorization.

# Security requirements

Unauthenticated users can:
- read timetable
- read overall system status
- read bell status

Authenticated users can:
- ring bell patterns on command
- restart the application
- reboot the operating system

# Program Design notes

The view-model-viewmodel pattern is maintained.

* The view is the set of UI controls on a javafx scene which is comprised of:
  * an fxml file,
  * and a java class (View)
* The viewmodel are the observers and callbacks between the view and the model.
It decouples the UI control from the model.
* The model is the logic which can obtain and present data to the view
and perform tasks.  This layer must remain isolated from the UI controls
via the viewmodel.

Decoupling is further supported by CDI injection.

The CDI bean manager is registered to javafx to
instantiate the View controllers at fxml load time.



