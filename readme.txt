
music tracks:
  tools lmms and audacity

  lmms:
	1) need a bell sample: here, one with no royalty, was downloaded from web.
	(any sample is ok....this one with non-commercial permission,
	a re-use for non-commercial applicaition, from www.orangefreesounds.com,
	the name of the bell file from this source is 'church-bell-single-hit',
	'http://www.orangefreesounds.com/church-bell/'

	2) the sample was read into lmms linux tool using the 'AudioFileProcessor'.
	This plugin has the ability to change the pitch of the bell sound, and
	allows us to lay down a 'track' using a piano-roll technique.

	3) the westminster chimes.  wikipedia for the details.
	https://en.wikipedia.org/wiki/Westminster_Quarters
	there are 5 patterns of 4 notes.  An lmms 'song' was
	laid for each pattern, and an audio file created.

	4) the bell has a long sustain (training resonance).  This
	bell is beautiful and rich.  However, the sustain is not needed
	in the middle of the sequence....only at the end.  So, two
	songs were created for each westminster pattern.  sustain-short
	and sustain long. 




from gino:
	network ip address (dhcp?) (need to connect for correct time)
	install mdc client(s)
	install putty on windows
	
resource: http://crunchbang.org/forums/viewtopic.php?pid=182574

fsck:
   set linux fsckfix by default to yes.  edit /lib/init/vars.sh

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

enable mpd as user level system process (started at boot by systemd)
	current debian on beagleboard as init.d for mpd, upgrade?
	changed /etc/default/mpd to point to /home/bells/.mpd/mdp.conf

	mpd can be started manually (it is not already running):
		login as user bells
		mpd -v .mpd/mpd.conf


add user 'bells'
	# create home directory, user name: 'bells'
	# the userid of 'bells' on host, must be same as userid in docker containers.
	useradd -m bells
	# add user 'bells' to 'audio' group
	usermod -a -G audio bells

eth0 networking:
	enable networking: open file /etc/network/interfaces
	add:
		auto eth0
		allow-hotplug eth0
		iface eth0 inet dhcp

# currently using usb audio.
# hdmi controller quit after 10 minutes (every time) for unknown reason
# don't bother to enable hdmi audio
hdmi: enable audio
	http://www.raspberry-pi-geek.com/Archive/2013/02/HDMI-and-the-BeagleBone-Black-Multimedia-Environment/(offset)/2
	modify eEnv.txt
		kms_force_mode=video=HDMI-A-1:720x480@60e


# NOTE: systemd provides timedate service.  Don't use ntp in this case.
set system time
	resource: http://derekmolloy.ie/automatically-setting-the-beaglebone-black-time-using-ntp/

	set system timezone
		ln -s /usr/share/zoneinfo/America/New_York /etc/localtime
	install/enable ntp
		apt-get install ntp
	configure ntp servers (in /etc/ntp.conf):
		server 0.us.pool.ntp.org
		server 1.us.pool.ntp.org
		server 2.us.pool.ntp.org
		server 3.us.pool.ntp.org
	TODO: see if a device inside St V provides reliable time service.

Cron should not run jobs if we KNOW the time is wrong....or not right.
	maybe we should get a UPS.
	how do we know the time is not set from ntp?
		if ntpq -np output shows a high 'offset'.... or
		if ntpq -np output is "No associations ID's returned"

	On startup:
		ntpq -nc gives 'No associations ID's returned" 
			for some reason, ntp is not sending ntp udp requests, until some delay (?unknown)
		if there is a network,
			ntpq -np gives 'No association ID's returned" for ~2 > X < ~20 minutes.
			ntpq -np gives a list of servers....and an extreme 'offset' for X minutes.
			ntpq -np gives a list of servers....with low offset, and one server marked with '*' (current sync server)
		if network drops
	Behavior of ntp at startup when there is a network.
		ntpq -np : offset does not change, 'when' increases, 'reach' shifts to 0
		'*' and '+' are dropped after about 10 minutes.

	When network restores
		
	bb: apr 23 16:20:48, host: 8/10 20:44

	on netwok fail



networking.
 to route ip from beagleboard: define route on beagle board:
	ip route add default via 192.168.7.1
 add nameserver to bbb's /etc/resolv.conf (unless its using tool: resolvconf)

 to route ip from beagleboard: allow traffic through linux host:
	echo 1 | sudo tee /proc/sys/net/ipv4/ip_forward
	sudo iptables -P FORWARD ACCEPT
	sudo iptables -A POSTROUTING -t nat -j MASQUERADE -s 192.168.7.0/30


setting a new beaglebone black for docker
-- using 'Debian 8.6 2016-11-06 4GB SD LXQT'
-- should have some elements of the above
-- time synchronization.  previous install used ntp, but this
    latest debian activates the systemd time synchronization service by default.    time is synced when network is on.  Cool.  This alleviates/mitigates risk.

-- audio drivers, already installed debian default
-- alsa utilities, already installed debian default
-- hdmi support.  Not sure if audio on hdmi works without edit to uEnv.txt
    (as above).  If so....there is a different syntax, and debian default
    has ready line commented out.
-- ethernet networking and dhcp active by debian default
-- timezone....set to US/Eastern with dpkg-reconfigure tzdata

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

--- a normal user, named 'bells', is the owner of the processes
  -- the 'bells' uid must match the uid of the mpd user in the mpd container
  -- passwd: 0fStVer0nica
  -- in user bells home directory:
    -- Bells - is the music directory in use by mpd
    -- data - is mpd's data directory
    -- bell-tower contains configuration, and google credentials
      # it is NECESSARY to get google credentials on a machine with a browser, and copy it into this volume


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

