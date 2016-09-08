#!/bin/bash

# %s: seconds since 1970-01-01 00:00:00 UTC
earliest=$(date -d 2016-01-01 +%s)

now=$(date +%s)

if (( earliest >  now ));
then
    echo break out because time is not set right
    exit 1
fi

if mpc status | grep -q playing
then
   echo playing
   exit 2
elif [ "$#" -gt 0 ]
then
   mpc clear
   mpc add $*
   mpc play
fi
exit 0
