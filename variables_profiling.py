#!/usr/bin/env python

import json, logging, time, os, datetime
import argparse, requests

LEVELS = {
    'debug':logging.DEBUG,
    'info':logging.INFO,
    'warning':logging.WARNING,
    'error':logging.ERROR
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
        exit(1)

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
        outputdir = os.path.join("/tmp/vprofil/",datetime.datetime.now().strftime('%Y-%m-%d_%H-%M-%S'))
    if not os.path.exists(outputdir):
        os.makedirs(outputdir)
    logging.debug("OUTPUT DIR : %s" % outputdir)

    try:
        url = 'http://%s:%s/mining/algorithms.json' % (host, port)
        logging.info("Listing algorithms %s..." % url)
        response = requests.get(url)

        if response.status_code != 200:
            raise Exception("Interval error")
        logging.info("#Algorithms : %d" % len(response.json()))

        logging.info("Listing variables ...")
        url = 'http://%s:%s/mining/query/WP_LIST_VARIABLES' % (host, port)
        response = requests.post(url, stream=True)
        if response.status_code != 200:
            raise Exception("Interval error")

        variables = [];
        lines = response.iter_lines()
        next(lines)
        for line in lines:
            if line:
                variables.append(json.loads(line.lstrip())[0])
        logging.info("'#Variables : %d" % len(variables))

        try:
            variablesfp = open(os.path.join(outputdir, "variables.json"),  'a+')
            json.dump(variables, variablesfp)
        except Exception, ex:
            logging.error("Unable to store variables", ex)
        finally:
            try:
                if variablesfp:
                    variablesfp.close()
            except:
                logging.error("Unable to close variables.json", ex)

        for var in variables:

            logging.info(" Summary Statistics %s variable ..." % var)
            payload = [{ 'variable' : var }]
            url = 'http://%s:%s/mining/query/WP_VARIABLE_PROFILE' % (host, port)
            response = requests.post(url, json=payload, stream=True)
            if response.status_code != 200:
                raise Exception("Interval error")
            lines = response.iter_lines()
            next(lines) #schema
            datasetStatistics = [];
            for line in lines:
                if line:
                    datasetStatistics.append(json.loads(line.lstrip()))
            logging.info(datasetStatistics)

            logging.info(" Dataset Statistics %s variable ..." % var)
            datasetStatisticsTotal = []
            payload = [{"column1": var, "nobuckets": 10}]
            url = 'http://%s:%s/mining/query/WP_VARIABLE_HISTOGRAM' % (host, port)
            response = requests.post(url, json=payload, stream=True)
            if response.status_code != 200:
                raise Exception("Interval error")
            datasetStatistics = [];
            lines = response.iter_lines()
            next(lines) #schema
            for line in lines:
                if line:
                    datasetStatistics.append(json.loads(line.lstrip()))
            logging.info(datasetStatistics)
            datasetStatisticsTotal.append(datasetStatistics)

            byvars = ["APOE4", "AGE", "PTGENDER", "DX_bl"]
            for byvar in byvars:
                logging.info(" Dataset Statistics %s variable ..." % var)
                payload = [{"column1": var, "column2": byvar, "nobuckets" : 10}]
                url = 'http://%s:%s/mining/query/WP_VARIABLES_HISTOGRAM' % (host, port)
                response = requests.post(url, json=payload, stream=True)
                if response.status_code != 200:
                    raise Exception("Interval error")
                datasetStatistics = [];
                lines = response.iter_lines()
                next(lines) #schema
                for line in lines:
                    if line:
                        datasetStatistics.append(json.loads(line.lstrip()))
                datasetStatisticsTotal.append(datasetStatistics)

            # original
            resultpath = os.path.join(outputdir, var)
            os.makedirs(resultpath)
            oresultfp = open(os.path.join(resultpath, "response.original.json"), 'a+')
            try:

                oresultfp.write(str(datasetStatisticsTotal))
            finally:
                if oresultfp:
                    oresultfp.close()

            # requested
            result = {} # TODO format the result
            resultfp = open(os.path.join(resultpath, "response.json"), 'a+')
            try:
                resultfp.write(str(result))
                logging.info("Response stored.")
            finally:
                if resultfp:
                    resultfp.close()

    except Exception, e:
        # import traceback
        # traceback.print_exc()
        # os.removedirs(outputdir)
        logging.error(e)
