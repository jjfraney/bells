#!/bin/bash

minute=$(date +%M)


# if near top of hour
if (( minute > 55 || minute < 5 ));
then
    hour=$(date +%I)
    if (( minute >> 55 ));
    then
	let hour=$hour+1;
    fi
    qtr="4"

# if near first quarter hour
elif (( 10 < minute && minute < 20 )) ;
then
    qtr="1"

# if near half hour
elif (( 25 < minute && minute < 35 )) ;
then
    qtr="2"

# if near third quarter hour
elif (( 40 < minute && minute < 50 )) ;
then
    qtr="3"
fi
if [[ -n $qtr ]]
then
  root=westminster-chimes-qtr-$qtr
  if [[ -z $hour ]]
  then
    echo time/$root.wav
  else
    echo time/$root-hour-$hour-toll.wav
  fi
fi
exit 0
