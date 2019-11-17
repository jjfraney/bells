
Abandoned docker in November 2019.  These notes remain for possible
future.  However, I'm not expecting a java image for bbb.

system configuration

# currently using mpd in a docker container.  Don't bother installing
install, disable mdp systemctl as a system process with root priv
	apt-get install mpd mpc
	service mpd stop
	update-rc.d mpd disable
	edit /etc/default/mpd and set MPDCONF=/home/bells/.mpd/mpd.conf
	update-rc.d mpd enable
	service mpd start

	# the mpd service attempts to bind to the external ip address, but the network is not running yet: fail.
	set: bind_to_address         "0.0.0.0"
        	removing/commenting the other.

setting a new beaglebone black for docker
-- using 'Debian 8.6 2016-11-06 4GB SD LXQT'
-- should have some elements of the above
-- time synchronization.  previous install used ntp, but this
    latest debian activates the systemd time synchronization service by default.    time is synced when network is on.  Cool.  This alleviates/mitigates risk.

-- installed docker.
--- space for images
---- using debian distribution without x11, otherwise
---- there is not enough room on bbb for all these binaries
---- investigate: use smaller bootable images
---- for now, using a microsd card for extra storage
----- manual mount
     mount /dev/mmcblk0p1 /mnt
----- auto mount on boot, add this to /etc/fstab
     /dev/mmcblk0p1	/mnt ext2 rw,relatime,block_validity,barrier,user_xattr,acl

--- use docker's namespace
    # per docker, a special user is needed
    useradd -u 5000 --no-create-home --shell /usr/sbin/nologin -c "docker user remap" dckremap

    # add lines to /etc/sub file (can use something other than 900000 if it is taken):
    to /etc/subuid: dckremap:900000:65536
    to /etc/subgid: dckremap:900000:65536


--- starting docker
---- in /lib/systemd/system/docker.service, change this line
     ExecStart=/usr/bin/dockerd -g /mnt/docker --userns-remap=dckremap -H fd:// -H tcp://192.168.1.133:2375


--- docker image for java
---- building remotly using docker api:

----- in directory: scheduler/core/src/main/docker/jdk, copy in the jdk gz.

      # create the jdk image
      docker -H tcp://192.168.1.133:2375 build -t jdk-arm:1.8.0_111 .

      # create the jdk volume container
      docker -H tcp://192.168.1.133:2375 create --name jdk8 jdk-arm:1.8.0_111

--- create a docker network for both the scheduler and mpd to hang on
   this lets the scheduler connect to mpd by the container name
      docker -H tcp://192.168.1.133:2375  network create bells

--- docker image for scheduler as it stands today
----- use maven to build the docker image for scheduler
----- mvn package docker:build -pl core
      # build the image for the scheduler core
      mvn clean package docker:build -pl core -Dmaven.test.skip=true

      # start container, logs to syslog, volume from jdk


     docker -H tcp://192.168.1.133:2375 run -d --net bells --userns host -e TZ=US/Eastern --restart=unless-stopped --log-driver=syslog \
		-v /home/bells/bell-tower:/bell-tower --volumes-from jdk8 --name core core:1.0-SNAPSHOT


-- mpd in docker on beaglebone black
  -- test audio on the black with speaker-test (alsa tools package is required), discover the devices and add them to the mpd.conf
	  --- discover devices with aplay -L
		# for the SB Go usb dongle:
		speaker-test -Dplughw:Go -t wav -c 2
  -- run docker build for mpd with direct access to internet.
  -- the /dev/snd device must be mapped
  -- disable user names with --userns host
    -- the user id of the mpd process should match the host userid of the account
    -- the user in the host must be in the audio group
    -- in this configuration, the container starts as root, but mpd switches to the configured mpd user id, which is normal user on host.
  -- map volume to the bells home directory
     docker -H tcp://192.168.1.133:2375 run -d --net bells --restart=unless-stopped --userns host --device /dev/snd:/dev/snd \
		-v /home/bells/Bells:/mpd/music -v /home/bells/data:/mpd/data --name bells-mpd mpd:latest
  -- to verify operation, use netcat command
    -- use docker inspect to get the ip address of the bells-mpd container.
       netcat 172.18.0.2 6600


