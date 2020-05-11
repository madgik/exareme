# Forward compatibility
from __future__ import division
from __future__ import print_function

import sys
from os import path

from utils.algorithm_utils import parse_exareme_args
from KAPLAN_MEIER.km_lib import get_data, local_1


def main(args):
    local_in = get_data(args)
    # Run algorithm local step
    local_out = local_1(local_in=local_in)
    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == "__main__":
    args = parse_exareme_args(__file__)
    main(args)
