
A good unix mqtt client: https://snapcraft.io/mqttx

android mqtt client are tough to find.  They are generally too simple.
Try: mqtt dashboard.  because it can handle bells' status response with some usefulness.

A client would publish to bell-tower/ops
  command is simple text:
    status - verifies running bells, and returns schedule and status of music player
    play call-to-mass.ogg - plays the one minute of tolling for start of mass
    toll - plays a tolling funeral bell forever until stop
    toll #### - plays a tolling funeral bell for some number of seconds
    peal - plays a peal of bells as in a wedding
    peal ### - plays a peal of bells as in a wedding for some number of seconds
    stop - stop playing
    lock - prevent player to play music
    unlock - allow player to play
    schedule - read schedule from google calendar

