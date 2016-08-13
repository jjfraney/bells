from gino:
	network ip address (dhcp?) (need to connect for correct time)
	install mdc client(s)
	install putty on windows
	
resource: http://crunchbang.org/forums/viewtopic.php?pid=182574

system configuration
install, disable mdp systemctl as a system process with root priv
	apt-get install mpd mpc
	service mpd stop
	update-rc.d mpd disable

add user 'bells'
	# create home directory, user name: 'bells'
	useradd -m bells
	# add user 'bells' to 'audio' group
	usermod -a -G audio bells

hdmi: enable audio
	http://www.raspberry-pi-geek.com/Archive/2013/02/HDMI-and-the-BeagleBone-Black-Multimedia-Environment/(offset)/2
	modify eEnv.txt
		kms_force_mode=video=HDMI-A-1:720x480@60e


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

enable mpd as user level system process (started at boot by systemd)
	current debian on beagleboard as init.d for mpd, upgrade?
	changed /etc/default/mpd to point to /home/bells/.mpd/mdp.conf

	mpd can be started manually (it is not already running):
		login as user bells
		mpd -v .mpd/mpd.conf



networking.
 to route ip from beagleboard: define route on beagle board:
	ip route add default via 192.168.7.1

 to route ip from beagleboard: allow traffic through linux host:
	echo 1 | sudo tee /proc/sys/net/ipv4/ip_forward
	sudo iptables -P FORWARD ACCEPT
	sudo iptables -A POSTROUTING -t nat -j MASQUERADE -s 192.168.7.0/30


