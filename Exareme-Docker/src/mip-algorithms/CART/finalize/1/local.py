from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import json


sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/CART/')

from cart_lib import Cart_Glob2Loc_TD,Node

def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True, help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True, help='Path to the pickle file holding the previous state.')
    parser.add_argument('-global_step_db', required=True, help='Path to db holding global step results.')
    args, unknown = parser.parse_known_args()

    # Load global node output
    globalTree, activePaths = Cart_Glob2Loc_TD.load(path.abspath(args.global_step_db)).get_data()

    # Save output to txt file
    globalTreeJ = globalTree.tree_to_json()
    file = open("/root/tree.txt","w")
    file.write(json.dumps(globalTreeJ))
    file.close()

if __name__ == '__main__':
    main()
