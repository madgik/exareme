from __future__ import division
from __future__ import print_function

from os import path

from PCA.pca import global_1
from utils.algorithm_utils import TransferAndAggregateData, parse_exareme_args


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = TransferAndAggregateData.load(local_dbs)
    # Run algorithm global step
    global_out = global_1(args, global_in=local_out)
    # Return the algorithm's output
    global_out.transfer()


if __name__ == "__main__":
    main(parse_exareme_args(__file__))
