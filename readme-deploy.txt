
Deploy the bell-tower application.

Setup the bbb (see readme-bbb.txt)

add user 'bells'
	# create home directory, user name: 'bells'
	# the userid of 'bells' on host, must be same as userid in docker containers.
	useradd -m bells
	# add user 'bells' to 'audio' group
	usermod -a -G audio bells


--- a normal user, named 'bells', is the owner of the processes
  -- passwd: 0fStVer0nica
  -- in user bells home directory:
    -- bell-tower contains configuration, and google credentials

-- to obtain google api tokesn, run bell tower in developer's environment, and then copy StoredCredential to new bbb


-- install mpd with apt-get
    -- currently running as system service (not under a user)
      -- /etc/mpd.conf
      -- systemd starts mpd, not docker
    -- copy ogg files to /var/lib/mpd/music as root
      -- todo: enable mpd for the bells user (not as root)

-- installed java with 'apt-get install default-jre-headless'
  -- deliver scheduler's jar file, and properties, to user 'bells' homedirectory
    -- link the jar file to '$HOME/core.jar'
  -- created bell-tower service for systemd: bell-tower.service
    -- add bell-tower.service file to /etc/systemd/service
     -- TODO: change bell-tower.service to wait for time sync
	    https://raspberrypi.stackexchange.com/questions/94635/how-can-i-delay-the-startup-of-systemd-services-until-the-datetime-is-set-no-rt
