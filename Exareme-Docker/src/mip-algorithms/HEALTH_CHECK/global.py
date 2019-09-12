import sys
import json
from os import path
from argparse import ArgumentParser

sys.path.append(path.dirname(path.dirname(path.abspath(__file__))) + '/utils/')

from algorithm_utils import set_algorithms_output_data
from health_check_lib import HealthCheckLocalDT

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-local_step_dbs', required=True, help='Path to local db.')
    args, unknown = parser.parse_known_args()
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = HealthCheckLocalDT.load(local_dbs)
    nodes = {}
    nodes["active_nodes"] = local_out.get_data()
    
    # Return the algorithm's output
    set_algorithms_output_data(json.dumps(nodes))


if __name__ == '__main__':
    main()
