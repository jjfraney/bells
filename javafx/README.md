

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

# Workflows

## Scheduling

* view the time and pattern of the next scheduled peal.
  * UI: belltower (main) status panel, timetable panel
  * API: get schedule
* view the list of upcoming scheduled peals.
  * UI: timetable panel
  * API: get schedule
* get list of bell patterns (call to mass, wedding, funeral)
  * UI: belfry, timetable (at edit time)
  * API: pattern list
* add/delete/change permanent schedule
  * UI: probably a different timetable panel (too much controls to fit)
  * API: maybe direct to google calendar?

## Immediate care
* add a one-time pattern to the schedule
  * UI: timetable panel (edit)
  * API: set schedule
* stop the current play immediately
  * graceful, as if real bells
  * hard, like no real bell can stop
  * UI: belfry panel, belltower panel
  * API: stop (hard and graceful)
* one-time play a bell pattern:
  * immediately
  * in-case schedule is incorrect and there is a miss in the schedule...play call-to-mass at next
  * UI: belfry panel
  * API: play pattern
half-hour (minus 60 s).  Emergency call to mass.
  * after some arbitrary delay
  * at some scheduled time
  * UI: belfry, timetable panels
  * API: playAt


## Administration

* health readout (most if not all on belltower/main panel)
  * to confirm tower will play next scheduled pattern at the right time
    * UI: belltower panel
  * to show tower has upto date schedule
    * UI: belltower panel
  * to show tower has the correct time
    * UI: belltower panel
  * tower can report the last missed scheduled bell play
    * comparing schedule to actual
  * show tower can connect to network
  * show tower is connected with media player (mpd)
  * time of last restart
  * time of last network outage and duration
  * show logs
    * result of getting schedule
    * restart/reboot
    * requests to ad hoc ring
* soft restart (reset short of a reboot)
* hard restart (reboot)
* get authorization to access google calendar
* default schedule (built-in to run while network is unavailable)
* remembers the last schedule download in case there is:
  * network outage
  * power outage and restoration
* login/authorization
  * times out.
  * authorization levels
    * monitor - read only
    * ringer - immediate rings, schedule edits
    * system - user management, reboots


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



