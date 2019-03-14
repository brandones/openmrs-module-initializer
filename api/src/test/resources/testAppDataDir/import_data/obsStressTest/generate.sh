#!/bin/bash

if [ $# -eq 0 ]; then
    echo "Usage: generate.sh <number of obs to generate>"
    echo "  e.g. ./generate.sh 10000"""
fi

uuid() {
    cat /proc/sys/kernel/random/uuid | tr -d '\n'
}

echo "Generating $1 test observations"

echo "uuid,Date,Person UUID,Location,Encounter UUID,Concept,Value,Void/Retire,Set Members,Set Member Values" >obs.csv
for i in $(seq 1 $1); do
    echo "$(uuid),,,,6519d653-393b-4118-9c83-a3715b82d4ac,FOOD ASSISTANCE,FALSE,,," >>obs.csv
done

