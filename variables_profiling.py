#!/usr/bin/env python

import argparse
import datetime
import json
import logging
import os
import requests

LEVELS = {
    'debug': logging.DEBUG,
    'info': logging.INFO,
    'warning': logging.WARNING,
    'error': logging.ERROR
}

if __name__ == "__main__":

    parser = argparse.ArgumentParser(
            prog="variables_profiling",
            description="Variables Profiling",
            version="0.1"
    )
    parser.add_argument('--host', action='store', dest='host')
    parser.add_argument('--port', action='store', dest='port')
    parser.add_argument('--log', action='store', dest='level')
    parser.add_argument('--dir', action='store', dest='dir')

    try:
        args = parser.parse_args()
    except IOError, msg:
        parser.error(msg)

    level = LEVELS.get(args.level, logging.INFO)
    logging.basicConfig(level=level)

    if args.host:
        host = args.host
    else:
        host = "0.0.0.0"
    logging.debug("Host : %s" % host)

    if args.port:
        port = args.port
    else:
        port = "9090"
    logging.debug("PORT : %s" % port)

    if args.dir:
        outputdir = args.dir
    else:
        dt = datetime.datetime.now()
        outputdir = os.path.join("/tmp/vprofil/",
                                 datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S'))
    if not os.path.exists(outputdir):
        os.makedirs(outputdir)
    logging.debug("OUTPUT DIR : %s" % outputdir)

    try:
        url = 'http://%s:%s/mining/algorithms.json' % (host, port)
        logging.info("Listing algorithms %s ..." % url)
        response = requests.get(url)
        if response.status_code != 200:
            raise Exception("Interval error")
        logging.info("#Algorithms : %d" % len(response.json()))

        url = 'http://%s:%s/mining/query/WP_LIST_VARIABLES' % (host, port)
        logging.info("Listing variables %s ..." % url)
        response = requests.post(url, stream=True)
        if response.status_code != 200:
            raise Exception("Interval error")

        variables = [];
        lines = response.iter_lines()
        next(lines)
        for line in lines:
            if line:
                variables.append(json.loads(line.lstrip()))
        logging.info("'#Variables : %d" % len(variables))
        logging.debug(variables)
        try:
            variablesfp = open(os.path.join(outputdir, "variables.json"), 'a+')
            json.dump(variables, variablesfp)
        except Exception, ex:
            logging.error("Unable to store variables", ex)
        finally:
            try:
                if variablesfp:
                    variablesfp.close()
            except:
                logging.error("Unable to close variables.json", ex)

        for variable in variables:

            var = variable[0]
            profile = []
            payload = [{"name": "variable", "value": var}]
            logging.debug(payload)
            url = 'http://%s:%s/mining/query/WP_VARIABLE_SUMMARY' % (host, port)
            logging.info(" Summary Statistics %s variable %s ..." % (var, url))
            response = requests.post(url, json=payload, stream=True)
            if response.status_code != 200:
                raise Exception("Interval error")
            lines = response.iter_lines()
            datasetStatistics = [next(lines)];
            for line in lines:
                if line:
                    datasetStatistics.append(json.loads(line.lstrip()))
            response.close()
            logging.debug(datasetStatistics)
            profile.append(datasetStatistics)

            if variable[1] != "text":

                payload = [{"name": "column1", "value": var}, {"name": "nobuckets", "value": "10"}]
                logging.info(payload)
                url = 'http://%s:%s/mining/query/WP_VARIABLE_HISTOGRAM' % (host, port)
                logging.info(" Dataset Statistics %s variable %s ..." % (var, url))
                response = requests.post(url, json=payload, stream=True)
                if response.status_code != 200:
                    raise Exception("Interval error")
                datasetStatistics = [];
                lines = response.iter_lines()
                datasetStatistics.append(next(lines))  # schema
                for line in lines:
                    if line:
                        datasetStatistics.append(json.loads(line.lstrip()))
                response.close()
                logging.debug(datasetStatistics)
                profile.append(datasetStatistics)

                byvars = ["DX_bl", "AGE", "PTGENDER", "APOE"]
                for byvar in byvars:

                    logging.info(" Dataset Statistics %s - %s variable ..." % (var, byvar))
                    payload = [
                        {"name": "column1", "value": var},
                        {"name": "column2", "value": byvar},
                        {"name": "nobuckets", "value": "10"}
                    ]
                    logging.info(payload)
                    url = 'http://%s:%s/mining/query/WP_VARIABLES_HISTOGRAM' % (host, port)
                    response = requests.post(url, json=payload, stream=True)
                    logging.info(" Dataset Statistics %s - %s variable %s ..." % (var, byvar, url))
                    if response.status_code != 200:
                        raise Exception("Interval error")
                    datasetStatistics = [];
                    lines = response.iter_lines()
                    datasetStatistics.append(next(lines))  # schema
                    for line in lines:
                        if line:
                            datasetStatistics.append(json.loads(line.lstrip()))
                    response.close()
                    logging.debug(datasetStatistics)
                    profile.append(datasetStatistics)

            # logging.debug(profile)
            # # original
            resultpath = os.path.join(outputdir, var)
            os.makedirs(resultpath)
            # oresultfp = open(os.path.join(resultpath, "response.original.json"), 'a+')
            # try:
            #
            #     oresultfp.write(json.dumps(profile, sort_keys=True, indent=4, separators=(',', ': ')))
            # finally:
            #     if oresultfp:
            #         oresultfp.close()

            # specs
            result = []
            i = 0
            summary_statistics = {}
            summary_statistics["dataType"] = "SummaryStatistics"
            summary_statistics["code"] = var
            summary_statistics["count"] = profile[0][1][0]
            summary_statistics["average"] = profile[0][1][1]
            summary_statistics["min"] = profile[0][1][2]
            summary_statistics["max"] = profile[0][1][3]
            summary_statistics["std"] = profile[0][1][4]
            result.append(summary_statistics)
            i += 1
            if variable[1] != "text":  # not categorical

                for byvar in byvars:
                    categories = []
                    headers = []
                    values = []
                    iterres = iter(profile[i])
                    next(iterres)
                    for r in iterres:
                        categories.append(r[1])
                        headers.append((r[3] - r[2])/2 + r[2])
                        values.append(r[4])
                    dataset_statistics = {}
                    dataset_statistics["code"] = var
                    dataset_statistics["dataType"] = "DatasetStatistic"
                    name = "Count " + var + " values"
                    dataset_statistics["dataset"] = {
                        "data": {"categories": categories, "header": headers, "shape": "vector",
                                 "value": values}, "name": name}
                    dataset_statistics["label"] = "Histogram - %s" % byvar
                    result.append(dataset_statistics)
                    i = +1

            resultfp = open(os.path.join(resultpath, "response.json"), 'a+')
            try:
                resultfp.write(json.dumps(result, sort_keys=True, indent=4, separators=(',', ': ')))
                logging.info("Response stored.")
            finally:
                if resultfp:
                    resultfp.close()

    except Exception, e:
        import traceback

        traceback.print_exc()
        # os.removedirs(outputdir)
        logging.error(e)
