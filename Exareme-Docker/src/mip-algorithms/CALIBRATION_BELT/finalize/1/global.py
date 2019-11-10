from __future__ import division
from __future__ import print_function

import sys
from os import path
from argparse import ArgumentParser
import numpy as np
import json
from scipy.stats import chi2, norm
from scipy.special import logit, expit

sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) + '/utils/')
sys.path.append(path.dirname(path.dirname(path.dirname(path.dirname(path.abspath(__file__))))) +
                '/CALIBRATION_BELT/')

from algorithm_utils import StateData, set_algorithms_output_data
from cb_lib import CBFinal_Loc2Glob_TD, find_relative_to_bisector, givitiStatCdf


def cb_global_final(global_state, global_in):
    # Unpack global state
    n_obs = global_state['n_obs']
    ll_old_dict = global_state['ll_dict']
    coeff_dict = global_state['coeff_dict']
    iter = global_state['iter']
    e_name = global_state['e_name']
    o_name = global_state['o_name']
    max_deg = global_state['max_deg']
    # Unpack global input
    ll_dict, grad_dict, hess_dict, logLikBisector = global_in.get_data()

    # likelihood-ratio test
    ddev = 0
    model_deg = 1
    qlr = 0.95
    thres = chi2.ppf(q=qlr, df=1)
    for deg in range(2, max_deg + 1):
        ddev = 2 * (ll_dict[deg] - ll_dict[deg - 1])
        if ddev > thres:
            model_deg = deg
        else:
            break

    # Compute selected model coefficients, log-likelihood, grad and Hessian
    hess = hess_dict[model_deg]
    ll = ll_dict[model_deg]
    grad = grad_dict[model_deg]
    coeff = np.dot(
            np.linalg.inv(hess),
            grad
    )
    covar = np.linalg.inv(hess)

    # Compute p value
    calibrationStat = 2 * (ll - logLikBisector)
    p_value = 1 - givitiStatCdf(calibrationStat, m=model_deg, devel='external', thres=0.95)

    # Compute calibration curve
    e_min, e_max = 0.01, 0.99  # TODO replace e_min and e_max values with actual ones
    e = np.linspace(e_min, e_max, num=100)
    ge = logit(e)
    G = [np.ones(len(e))]
    for d in range(1, len(coeff)):
        G = np.append(G, [np.power(ge, d)], axis=0)
    G = G.transpose()
    p = expit(np.dot(G, coeff))
    calib_curve = np.array([e, p]).transpose()

    # Compute confidence intervals
    qci1, qci2 = 0.8, 0.95  # TODO do not hard-code that
    sig2gp = [np.dot(G[i], np.dot(covar, G[i])) for i in range(len(G))]
    ci1 = np.sqrt(np.multiply(chi2.ppf(q=qci1, df=2), sig2gp))
    ci2 = np.sqrt(np.multiply(chi2.ppf(q=qci2, df=2), sig2gp))
    g_min1, g_max1 = np.dot(G, coeff) - ci1, np.dot(G, coeff) + ci1
    g_min2, g_max2 = np.dot(G, coeff) - ci2, np.dot(G, coeff) + ci2
    p_min1, p_max1 = expit(g_min1), expit(g_max1)
    p_min2, p_max2 = expit(g_min2), expit(g_max2)
    calib_belt1 = np.array([e, p_min1, p_max1]).transpose()
    calib_belt2 = np.array([e, p_min2, p_max2]).transpose()

    # Find regions relative to bisector
    over_bisect1 = find_relative_to_bisector(np.around(e, 4), p_min1, 'over')
    under_bisect1 = find_relative_to_bisector(np.around(e, 4), p_max1, 'under')
    over_bisect2 = find_relative_to_bisector(np.around(e, 4), p_min2, 'over')
    under_bisect2 = find_relative_to_bisector(np.around(e, 4), p_max2, 'under')

    # Format output data
    # JSON raw
    raw_data = {
        'Model Parameters'                              :
            {
                'Model degree'     : model_deg,
                'coeff'            : coeff.tolist(),
                'log-likelihood'   : ll,
                'Hessian'          : hess.tolist(),
                'Covariance matrix': covar.tolist()
            },
        'n_obs'                                         : n_obs,
        'Likelihood ration test'                        : ddev,
        'Calibration curve'                             : np.around(calib_curve, 4).tolist(),
        'Calibration belt ' + str(int(qci1 * 100)) + '%': np.around(calib_belt1, 4).tolist(),
        'Calibration belt ' + str(int(qci2 * 100)) + '%': np.around(calib_belt2, 4).tolist(),
        'p values'                                      : np.around(p_value, 3),
        'Over bisector ' + str(int(qci1 * 100)) + '%'   : over_bisect1,
        'Under bisector ' + str(int(qci1 * 100)) + '%'  : under_bisect1,
        'Over bisector ' + str(int(qci2 * 100)) + '%'   : over_bisect2,
        'Under bisector ' + str(int(qci2 * 100)) + '%'  : under_bisect2,
    }

    # Write output to JSON
    result = {
        'result': [
            # Raw results
            {
                "type": "application/json",
                "data": [
                    raw_data
                ]
            }
        ]
    }
    try:
        global_out = json.dumps(result, allow_nan=False)
    except ValueError:
        raise ValueError('Result contains NaNs.')
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
    local_out = CBFinal_Loc2Glob_TD.load(local_dbs)
    # Run algorithm global step
    global_out = cb_global_final(global_state=global_state, global_in=local_out)
    # Return the algorithm's output
    set_algorithms_output_data(global_out)


if __name__ == '__main__':
    main()
