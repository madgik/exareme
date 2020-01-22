import json
from os import path
from argparse import ArgumentParser

from utils.algorithm_utils import set_algorithms_output_data
from LIST_DATASET.list_dataset_lib import ListDatasetLocalDT

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = ListDatasetLocalDT.load(local_dbs)
    pathologies = local_out.get_data()
    
    # Return the algorithm's output
    set_algorithms_output_data(json.dumps(pathologies))


if __name__ == '__main__':
    main()
