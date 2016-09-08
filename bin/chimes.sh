#!/bin/bash

minute=$(date +%M)
hour=$(date +%I)

# if near top of hour
if (( minute > 55 || minute < 5 ));
then
    if (( minute >> 55 ));
    then
	let hour=$hour+1;
    fi
    chime="q1chime q2chime q3chime q4chime"
    for ((i=0; i < hour; i++));
	do
	    chime="$chime toll"
	done
	 
    echo $chime

# if near first quarter hour
elif (( 10 < minute && minute < 20 )) ;
then
    echo q1chime

# if near half hour
elif (( 25 < minute && minute < 35 )) ;
then
    echo q1chime q2chime

# if near third quarter hour
elif (( 40 < minute && minute < 50 )) ;
then
    echo q1chime q2chime q3chime
fi
     
    
     
	 
    
