#!/bin/bash

if [ -z "$1" ]
then
  echo "No argument supplied"
else
  echo "Argument supplied: $1"
fi



scylla_node="scylla-node$1"
 
echo $scylla_node

docker-compose restart $scylla_node