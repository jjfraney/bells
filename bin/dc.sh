#!/bin/bash

# %s: seconds since 1970-01-01 00:00:00 UTC
earliest=$(date -d 2016-01-01 +%s)

now=$(date +%s)

if (( earliest >  now ));
then
    echo break out because time is not set right
    exit 3
fi

minute=$(date +%M)
hour=$(date +%I)

if (( minute > 55 || minute < 5 ));
then
    if (( minute >> 55 ));
    then
	let hour=$hour+1;
    fi
    chime = q1chime q2chime q3chime q4chime
    for ((i=0; i < hour; i++));
	do
	    chime = $(echo $chime toll)
	done
	 
    echo $chime
elif (( 10 < minute < 20 )) ;
then
    echo q1chime
elif (( 25 < minute < 35 )) ;
then
    echo q1chime q2chime
elif (( 40 < minute < 50 )) ;
then
    echo q1chime q2chime q3chime
fi
     
    
     
	 
    
