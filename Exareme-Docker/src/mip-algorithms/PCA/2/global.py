from __future__ import division
from __future__ import print_function

from os import path

from PCA.pca_lib import global_2
from utils.algorithm_utils import set_algorithms_output_data, TransferAndAggregateData, parse_exareme_args


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    # Load local nodes output
    local_out = TransferAndAggregateData.load(local_dbs)
    # Run algorithm global step
    global_out = global_2(args, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
