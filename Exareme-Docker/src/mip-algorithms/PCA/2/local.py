from __future__ import division
from __future__ import print_function

import sys
from os import path, getcwd
import numpy as np

_new_path = path.dirname(path.dirname(path.abspath(__file__)))
sys.path.append(_new_path)
while True:
    try:
        import utils.algorithm_utils
    except:
        sys.path.pop()
        _new_path = path.dirname(_new_path)
        sys.path.append(_new_path)
    else:
        break
del _new_path

from utils.algorithm_utils import StateData, Global2Local_TD, TransferData, parse_exareme_args


def pca_local(local_state, local_in):
    # Unpack local state
    X, schema_X = local_state['X'], local_state['schema_X']
    # Unpack local input
    data = local_in.get_data()
    mean = data['mean']

    # Substract the mean of each variable
    n_obs = len(X)
    X = X - mean
    gramian = np.dot(np.transpose(X), X)

    # Pack results
    local_out = TransferData(gramian=(gramian, 'add'), n_obs=(n_obs, 'add'), schema_X=(schema_X, 'do_nothing'))
    return local_out


def main(args):
    fname_prev_state = path.abspath(args.prev_state_pkl)
    global_db = path.abspath(args.global_step_db)

    # Load local state
    local_state = StateData.load(fname_prev_state).get_data()
    # Load global node output
    global_out = TransferData.load(global_db)
    # Run algorithm local step
    local_out = pca_local(local_state=local_state, local_in=global_out)
    # Return the output data
    local_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args()
    main(args)
