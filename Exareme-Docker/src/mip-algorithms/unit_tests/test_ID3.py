import requests
import unittest
import os,sys
import json
import logging
import math
import re
from decimal import *


endpointUrl='http://localhost:9090/mining/query/ID3'

def test_ID3_1():
    logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")
    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate"  },
            {   "name": "y", "value": "CL_contact_lenses" },
            {   "name": "dataset", "value": "contact-lenses" },
            {   "name": "filter", "value": "" },
            {   "name": "pathology","value":"dementia"}

        ]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (result['result'][1]['data'])

    resultR  = '''\
CL_tear_prod_rate = reduced: none
CL_tear_prod_rate = normal
|  CL_astigmatism = no
|  |  CL_age = pre-presbyopic: soft
|  |  CL_age = young: soft
|  |  CL_age = presbyopic
|  |  |  CL_spectacle_prescrip = hypermetrope: soft
|  |  |  CL_spectacle_prescrip = myope: none
|  CL_astigmatism = yes
|  |  CL_spectacle_prescrip = myope: hard
|  |  CL_spectacle_prescrip = hypermetrope
|  |  |  CL_age = pre-presbyopic: none
|  |  |  CL_age = presbyopic: none
|  |  |  CL_age = young: hard'''

    tree_lines = re.split('\n',resultR)
    tree = parse_tree(tree_lines)
    treeR = json.dumps(tree)
    treeR = json.loads(treeR)
    print (treeR)
    resultsComparison(result['result'][1]['data'], treeR)



def test_ID3_Privacy():
    logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")

    data = [
            {   "name": "iterations_max_number", "value": "20" },
            {   "name": "x", "value": "CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate"  },
            {   "name": "y", "value": "CL_contact_lenses" },
            {   "name": "dataset", "value": "contact-lenses" },
            {   "name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"CL_age\", \"field\": \"CL_age\", \"type\": \"string\", \"input\": \"text\", \"operator\": \"equal\", \"value\": \"Young\"}], \"valid\": true}" },
            {   "name": "pathology","value":"dementia"}
        ]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (result)

    check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"error\" : \"The Experiment could not run with the input provided because there are insufficient data.\"}"






def resultsComparison(jsonExaremeResult, jsonRResult):
    if jsonExaremeResult[0]['colname']==jsonRResult[0]:
        for childR in jsonRResult[1]:
            find = 0
            for childEx in jsonExaremeResult[0]['childnodes']:
                if childEx['edge'] == childR[1]:
                    find = 1
                    if 'childnodes' in childEx.keys():
                        resultsComparison(childEx['childnodes'], childR[2])
                    else:
                        print ("Leafvals:", childEx['leafval'], childR[2])
                        assert (childEx['leafval']== childR[2])
            if find == 0:
                print ("error: Different childs")
                assert 0
    else:
        print ("error: Different colname", jsonExaremeResult[0]['colname'], jsonRResult[0])
        assert(0)




re_splitter = re.compile("[ :]")
re_range = re.compile(
    r"^'\("
    r"(-inf|-?[0-9]+(\.[0-9]+)?)"
    r"-"
    r"(-?[0-9]+(\.[0-9]+)?\]|inf\))"
    r"'$")

def parse_value(token):
    """Returns an float if the token represents a number, a range if the token
    represents a range of numbers, otherwise return the token as is."""
    try:
        return float(token)
    except ValueError:
        # Look for ranges of the form '(start-end]', ' included
        if re_range.match(token):
            range_str = token[2:-2]
            # Careful not to use a minus sign as a dash.
            separator_dash = range_str.find("-", 1)
            return (parse_value(range_str[:separator_dash]),
                    parse_value(range_str[separator_dash+1:]))
        else:
            # Not a number or range - so it must be nominal, leave it as it.
            return token


def parse_line(line):
    """Split the line into a tuple
    (depth, feature, comparator, value, classification/None)"""
    # Avoid empty strings from double whitespaces and the likes.
    split = [ l for l in re_splitter.split(line) if len(l) > 0 ]
    depth = split.count("|")

    return (depth, split[depth], split[depth + 1],
            parse_value(split[depth + 2]),
            split[depth + 3] if len(split) > depth + 3 else None)


def parse_tree(lines):
    """Parses input lines into a decision tree."""
    current_index = [0] # need mutable container because of closure limitations
    def parse(current_depth):
        """Helper recursive function."""
        node_feature = None
        children = []
        while current_index[0] < len(lines):
            line = lines[current_index[0]]
            depth, feature, comparator, value, classif = parse_line(line)
            if depth < current_depth:
                # Finished parsing this node.
                break
            elif depth == current_depth:
                if node_feature is None:
                    node_feature = feature
                elif node_feature != feature:
                    raise Exception("Error : Feature mismatch - expected %s" "but got : \n%s"% (node_feature, line))

                # Another branch
                current_index[0] += 1
                if classif is None:
                    children.append((comparator, value, parse(current_depth + 1)))
                else:
                    children.append((comparator, value, classif))
            else:
                raise Exception("Error : Input jumps two levels at once\n%s." % line)

        return (node_feature, children)

    return parse(0)
