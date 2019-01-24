#!/bin/bash

uuid() {
    cat /proc/sys/kernel/random/uuid | tr -d '\n'
}

echo "uuid,Date,Person UUID,Location,Encounter UUID,Concept Reference Term,Concept Name,Value,Void/Retire" >obs.csv
for i in {1..10000}; do
    echo "$(uuid),2019-01-01T00:00:00Z,5946f880-b197-400b-9caa-a3c661d23041,Xanadu,,,FOOD ASSISTANCE,FALSE," >>obs.csv
done

