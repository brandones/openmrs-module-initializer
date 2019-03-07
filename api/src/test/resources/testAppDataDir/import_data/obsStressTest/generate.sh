#!/bin/bash

uuid() {
    cat /proc/sys/kernel/random/uuid | tr -d '\n'
}

echo "uuid,Date,Person UUID,Location,Encounter UUID,Concept,Value,Void/Retire,Set Members,Set Member Values" >obs.csv
for i in {1..10000}; do
    echo "$(uuid),,,,6519d653-393b-4118-9c83-a3715b82d4ac,FOOD ASSISTANCE,FALSE,,," >>obs.csv
done

