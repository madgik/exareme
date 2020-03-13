from os import path
import json
import sqlite3

from argparse import ArgumentParser
from utils.algorithm_utils import set_algorithms_output_data

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-input_local_DB', required=True, help='Path to the folder with the dbs.')
    args, unknown = parser.parse_known_args()
    db_path = path.abspath(args.input_local_DB)
    
    variables = {}
    # Get the datasets for each pathology
    conn = sqlite3.connect(db_path)
    cur = conn.cursor()
    cur.execute('SELECT code,sql_type FROM metadata')
    for row in cur:
        variables[row[0]] = row[1]

    # Return the algorithm's output
    set_algorithms_output_data(json.dumps(variables))


if __name__ == '__main__':
    main()
