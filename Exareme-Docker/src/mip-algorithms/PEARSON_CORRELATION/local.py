# Forward compatibility
from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

import sys
from os import path

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

from utils.algorithm_utils import parse_exareme_args
from pearsonc_lib import get_data, pearson_local


def main(args):
    local_in = get_data(args)
    # Run algorithm local step
    local_out = pearson_local(local_in=local_in)
    # Return the output data (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    args = parse_exareme_args(__file__)
    main(args)
