setting up the beagle bone....no details of settign up the bell-tower

resource: http://crunchbang.org/forums/viewtopic.php?pid=182574

fsck:
   set linux fsckfix by default to yes.  edit /lib/init/vars.sh

system configuration

eth0 networking:
	enable networking: open file /etc/network/interfaces
	add:
		auto eth0
		allow-hotplug eth0
		iface eth0 inet dhcp

    if needful, create /etc/resolv.conf with entry 'nameserver 8.8.8.8'

    some network start behavior (need more research) create unwanted default route, so
      figure this out or turn off AVAHI_DAEMON_DETECT_LOCAL=0 in /etc/default/avahi-daemon

usb networking:
	enable networking: open file /etc/network/interfaces
        iface usb0 inet static
            address 192.168.7.2
            netmask 255.255.255.252
            network 192.168.7.0
            gateway 192.168.7.1
            dns-nameservers 8.8.8.8


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
	sudo iptables -A POSTROUTING -t nat -j MASQUERADE -s 192.168.7.0/3

 for wifi,
    mask systemctl: systemctl mask wpa_supplicant.service
        because, usb startup script fails if wpa_supplicant is running,
	and wpa_supplicant is started when dbus service starts.
    on some debians, disable device naming:
	DOES NOT WORK!
	edit: /lib/systemd/network/99-default.link
        change 'persistent' to 'none'
        see also: man systemd.link
    add something like this to /etc/network/interfaces:
	auto wlan0
	iface wlan0 inet dhcp
	wpa-essid essid
	wpa-psk psk password

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

