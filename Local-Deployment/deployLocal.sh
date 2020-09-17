#!/usr/bin/env bash

docker-compose down

#Run convert-csv-to-db
chmod 775 ../Exareme-Docker/files/root/exareme/convert-csv-dataset-to-db.py
#Removing all previous .db files from the LOCAL_DATA_FOLDER
echo "Starting the process of creating databases.."
echo -e "\nDeleting previous databases."
rm -rf ${LOCAL_DATA_FOLDER}/**/*.db

echo -e "\nParsing csv files in " ${LOCAL_DATA_FOLDER} " to database files. "
#python ../Exareme-Docker/files/root/exareme/convert-csv-dataset-to-db.py -f ${LOCAL_DATA_FOLDER} -t "master"
#Get the status code from previous command
#py_script=$?
#If status code != 0 an error has occurred
#if [[ ${py_script} -ne 0 ]]; then
#     echo -e "\nCreation of databases failed. Exareme will not run until fixes be made." >&2
#     exit 1
#fi

chmod 755 *.sh

docker-compose up -d
