#!/usr/bin/bash

CURR_DIR=/home/bitor/projects/redwars/src/main/java/edu/ufp/inf/sd/rmi/runscripts
source ${CURR_DIR}/setenv.sh 

function trap_sigInt() {
    #make sure services are shut
    echo -e "Terminating Python server"
    kill $(lsof -t -i:8000)
    echo -e "Terminating rmiregistry"
    kill $(lsof -t -i:1099)
    echo -e "Terminating Java server"
    exit 1
}

trap 'trap_sigInt' 2

# compile first
cd ${JAVAPROJ}
mvn package

# make sure that maps are available
if [ -z "$(ls -A target/classes/maps)" ]; then
    echo "WARING: Mapsand/or saves folder is empty! Copying..."
    cp -r maps/ target/classes/
    cp -r saves/ taget/classes/
fi

cd ${CURR_DIR}
source ${CURR_DIR}/_1_runpython.sh &
source ${CURR_DIR}/_2_runregistry.sh &

while [[ 1 ]]; do
    source ${CURR_DIR}/_3_runserver.sh 
    sleep infinity
done


# echo "Press 'Y' to run Client!"
#     read  input
#     case ${input} in
#         [Yy]) source ${JAVAPROJ}/src/edu/ufp/inf/sd/rmi/_02_calculator/runscripts/_4_runclient.sh;
#     esac
