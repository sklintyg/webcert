#!/bin/bash
for i in {1..100}
do
   echo "ran $i times"
   DATABASE_PASSWORD=b4pelsin grunt acc:ssh-tunnel:@skicka-till-fk
done
