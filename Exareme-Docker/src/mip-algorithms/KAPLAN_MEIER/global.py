from __future__ import division
from __future__ import print_function

from os import path

from utils.algorithm_utils import (
    parse_exareme_args,
    set_algorithms_output_data,
    TransferAndAggregateData,
)
from KAPLAN_MEIER.km_lib import global_1


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = TransferAndAggregateData.load(local_dbs)
    # Run algorithm global step
    global_out = global_1(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == "__main__":
    args = parse_exareme_args(__file__)
    main(args)
