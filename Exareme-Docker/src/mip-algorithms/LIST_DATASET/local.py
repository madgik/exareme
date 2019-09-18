import sys
import os
from os import listdir
from os import path

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

import sqlite3
from argparse import ArgumentParser
from list_dataset_lib import ListDatasetLocalDT


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-input_local_DB', required=True, help='Path to the folder with the dbs.')
    args, unknown = parser.parse_known_args()
    dbs_path = path.abspath(args.input_local_DB)
    
    datasets = {}
    # Get a list of the pathologies in the data folder
    pathologies = listdir(dbs_path)
    
    # Get the datasets for each pathology
    for pathology in pathologies:
        datasetDBPath = path.join(dbs_path,pathology,'datasets.db')
        conn = sqlite3.connect(datasetDBPath)
        cur = conn.cursor()
        cur.execute('SELECT DISTINCT dataset FROM data')
        cur_datasets = []
        for row in cur:
            cur_datasets.append(row[0])
        datasets[pathology] = cur_datasets
    
    node_name = os.environ['NODE_NAME']
    
    pathologies = {}
    pathologies[node_name] = datasets
    
    local_out = ListDatasetLocalDT(pathologies)

    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main()
