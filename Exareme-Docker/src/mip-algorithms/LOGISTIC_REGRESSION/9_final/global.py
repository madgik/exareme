from __future__ import division
from __future__ import print_function

import sys
from os import path
import os
import errno
from argparse import ArgumentParser
import numpy as np
import json
import scipy.stats as st

sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.abspath(__file__)))) + '/LOGISTIC_REGRESSION/')

from algorithm_utils import StateData, set_algorithms_output_data
from log_regr_lib import LogRegrFinal_Loc2Glob_TD



def logregr_global_final(global_state, global_in):
    # Unpack global state
    n_obs = global_state['n_obs']
    n_cols = global_state['n_cols']
    coeff = global_state['coeff']
    y_val_dict = global_state['y_val_dict']
    schema_X = global_state['schema_X']
    schema_Y = global_state['schema_Y']
    # Unpack global input
    ll, grad, hess, ysum = global_in.get_data()

    # Output summary
    # stderr
    stderr = np.sqrt(
            np.diag(
                    np.linalg.inv(hess)
            )
    )
    # z scores
    z_scores = np.divide(coeff, stderr)
    # p-values
    z_to_p = lambda z: st.norm.sf(abs(z)) * 2
    p_values = z_to_p(z_scores)
    # Confidence intervals
    lci = np.array(
            [st.norm.ppf(0.025, loc=coeff[i], scale=stderr[i]) for i in range(len(coeff))]
    )
    rci = np.array(
            [st.norm.ppf(0.975, loc=coeff[i], scale=stderr[i]) for i in range(len(coeff))]
    )
    # Degrees of freedom
    df_mod = n_cols - 1
    df_resid = n_obs - df_mod - 1
    # Null model log-likelihood
    ymean = ysum / n_obs
    ll0 = ysum * np.log(ymean) + (n_obs - ysum) * np.log(1.0 - ymean)
    # AIC
    aic = 2 * n_cols - 2 * ll
    # BIC
    bic = np.log(n_obs) * n_cols - 2 * ll

    # Write output to JSON
    global_out = json.dumps(
        {
            'result': {
                'Covariates': list(schema_X),
                'Coefficients': list(coeff),
                'SE': list(stderr),
                'z scores': list(z_scores),
                'p values': list(p_values),
                'Lower C.I.': list(lci),
                'Upper C.I.': list(rci),
                'Model degrees of freedom': df_mod,
                'Residual degrees of freedom': df_resid,
                'Log-likelihood': ll,
                'Null model log-likelihood': ll0,
                'AIC': aic,
                'BIC': bic
            }
        }
    )

    return global_out


def main():
    # Parse arguments
    parser = ArgumentParser()
    parser.add_argument('-cur_state_pkl', required=True,
                        help='Path to the pickle file holding the current state.')
    parser.add_argument('-prev_state_pkl', required=True,
                        help='Path to the pickle file holding the previous state.')
    parser.add_argument('-local_step_dbs', required=True,
                        help='Path to db holding local step results.')
    args, unknown = parser.parse_known_args()
    fname_prev_state = path.abspath(args.prev_state_pkl)
    local_dbs = path.abspath(args.local_step_dbs)

    # Load global state
    global_state = StateData.load(fname_prev_state).data
    # Load local nodes output
    local_out = LogRegrFinal_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_out = logregr_global_final(global_state=global_state, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
