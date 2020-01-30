from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from os import path

from pearsonc_lib import pearson_global, PearsonCorrelationLocalDT
from utils.algorithm_utils import parse_exareme_args, set_algorithms_output_data


def main(args):
    local_dbs = path.abspath(args.local_step_dbs)

    local_out = PearsonCorrelationLocalDT.load(local_dbs)
    # Run algorithm global step
    global_out = pearson_global(global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
