from __future__ import division
from __future__ import print_function
from __future__ import unicode_literals

from LOGISTIC_REGRESSION.log_regr_lib import get_data, logreg_init_local
from utils.algorithm_utils import parse_exareme_args


def main(args):
    cur_state_pkl, local_in = get_data(args)
    # Run algorithm local step
    local_state, local_out = logreg_init_local(local_in=local_in)
    # Save local state
    local_state.save(fname=cur_state_pkl)
    # Transfer local output (should be the last command)
    local_out.transfer()


if __name__ == '__main__':
    main(parse_exareme_args(__file__))
