FROM armhf/alpine:3.4
MAINTAINER jjfraney@gmail.com

ENV MPD_VERSION 0.19.15-r2

RUN apk -q update \
    && apk -q --no-progress add alsa-lib \
    && apk -q --no-progress add alsa-utils \
    && apk -q --no-progress add mpd="$MPD_VERSION" \
    && rm -rf /var/cache/apk/*

# suitable for alpine container on ubuntu
# gid 29: missing from alpine; 
RUN addgroup -g 29 -S hostaudio

# create mpd user with host's audio group
RUN adduser -u 6500 -G hostaudio -S cmpd

# this mpd.conf file should specify:
#
#  user 'cmpd'
#  music_directory "/mpd/music"
#  playlist_directory "/mpd/data/playlists"
#  db_file "/mpd/data/file"
#  state_file "/mpd/data/state"
#  sticker_file "/mpd/data/sticker.sql"
#  log_file not specified (commented out), entrypoint uses stdout
#  pid_file not specified (commented out), no need to retain pid


COPY mpd.conf /etc/mpd.conf

# note:
VOLUME ["/mpd/music", "/mpd/data", "/mpd/log"]

# Entry point for mpc update and stuff
EXPOSE 6600

ENTRYPOINT ["mpd", "--stdout", "--no-daemon", "--verbose", "/etc/mpd.conf"]

