import requests
import unittest
import os,sys
import json
import logging
import math
import re
from decimal import *


endpointUrl='http://88.197.53.34:9090/mining/query/ID3'

def test_ID3_1():
    logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")
    data = [{   "name": "x", "value": "CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate"  },
            {   "name": "y", "value": "CL_contact_lenses" },
            {   "name": "dataset", "value": "contact-lenses" },
            {   "name": "filter", "value": "" },
            {   "name": "pathology","value":"dementia"}]
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)

    print (result['result'])

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

    #
    # result = [{'data': {'profile': 'tabular-data-resource', 'data': [[1.0, 'CL_tear_prod_rate', 'normal', 2.0, ''], [1.0, 'CL_tear_prod_rate', 'reduced', '', 'none'], [2.0, 'CL_astigmatism', 'no', 3.0, ''], [2.0, 'CL_astigmatism', 'yes', 4.0, ''], [3.0, 'CL_age', 'pre-presbyopic', '', 'soft'], [3.0, 'CL_age', 'presbyopic', 5.0, ''], [3.0, 'CL_age', 'young', '', 'soft'], [4.0, 'CL_spectacle_prescrip', 'hypermetrope', 6.0, ''], [4.0, 'CL_spectacle_prescrip', 'myope', '', 'hard'], [5.0, 'CL_spectacle_prescrip', 'hypermetrope', '', 'soft'], [5.0, 'CL_spectacle_prescrip', 'myope', '', 'none'], [6.0, 'CL_age', 'pre-presbyopic', '', 'none'], [6.0, 'CL_age', 'presbyopic', '', 'none'], [6.0, 'CL_age', 'young', '', 'hard']], 'name': 'ID3_TABLE', 'schema': {'fields': [{'type': 'int', 'name': 'id'}, {'type': 'text', 'name': 'colname'}, {'type': 'text', 'name': 'edge'}, {'type': 'int', 'name': 'childnodes'}, {'type': 'text', 'name': 'leafval'}]}}, 'type': 'application/vnd.dataresource+json'}, {'type': 'application/json', 'data': [{'childnodes': [{'edge': 'normal', 'colname': 'CL_astigmatism', 'childnodes': [{'edge': 'no', 'colname': 'CL_age', 'childnodes': [{'leafval': 'soft', 'edge': 'pre-presbyopic'}, {'edge': 'presbyopic', 'colname': 'CL_spectacle_prescrip', 'childnodes': [{'leafval': 'soft', 'edge': 'hypermetrope'}, {'leafval': 'none', 'edge': 'myope'}]}, {'leafval': 'soft', 'edge': 'young'}]}, {'edge': 'yes', 'colname': 'CL_spectacle_prescrip', 'childnodes': [{'edge': 'hypermetrope', 'colname': 'CL_age', 'childnodes': [{'leafval': 'none', 'edge': 'pre-presbyopic'}, {'leafval': 'none', 'edge': 'presbyopic'}, {'leafval': 'hard', 'edge': 'young'}]}, {'leafval': 'hard', 'edge': 'myope'}]}]}, {'leafval': 'none', 'edge': 'reduced'}], 'colname': 'CL_tear_prod_rate'}]}]
    # treeR = ['CL_tear_prod_rate', [['=', 'reduced', 'none'], ['=', 'normal', ['CL_astigmatism', [['=', 'no', ['CL_age', [['=', 'pre-presbyopic', 'soft'], ['=', 'young', 'soft'], ['=', 'presbyopic', ['CL_spectacle_prescrip', [['=', 'hypermetrope', 'soft'], ['=', 'myope', 'none']]]]]]], ['=', 'yes', ['CL_spectacle_prescrip', [['=', 'myope', 'hard'], ['=', 'hypermetrope', ['CL_age', [['=', 'pre-presbyopic', 'none'], ['=', 'presbyopic', 'none'], ['=', 'young', 'hard']]]]]]]]]]]]


    resultsComparison(result['result'][1]['data'], treeR)


def test_ID3_Privacy():
    logging.info("---------- TEST : ID3 - Test using contact-lenses dataset  ")

    data = [{   "name": "x", "value": "CL_age,CL_spectacle_prescrip,CL_astigmatism,CL_tear_prod_rate"  },
            {   "name": "y", "value": "CL_contact_lenses" },
            {   "name": "dataset", "value": "contact-lenses" },
            {   "name": "filter", "value": "{\"condition\": \"AND\", \"rules\": [{\"id\": \"CL_age\", \"field\": \"CL_age\", \"type\": \"string\", \"input\": \"text\", \"operator\": \"equal\", \"value\": \"Young\"}], \"valid\": true}" },
            {   "name": "pathology","value":"dementia"}]

    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl,data=json.dumps(data),headers=headers)
    result = json.loads(r.text)
    print (result)

    check_privacy_result(r.text)



def check_privacy_result(result):
    assert result == "{\"result\" : [{\"data\":\"The Experiment could not run with the input provided because there are insufficient data.\",\"type\":\"text/plain+warning\"}]}"


def resultsComparison(jsonExaremeResult, jsonRResult):
    if jsonExaremeResult[0]['colname']==jsonRResult[0]:
        #print("TEST",  jsonRResult[0])
        for childR in jsonRResult[1]:
            find = 0
            for childEx in jsonExaremeResult[0]['childnodes']:
                #print ("paidi",childEx)
                if childEx['edge'] == childR[1]:
                    find = 1
                    if 'childnodes' in childEx.keys():
                        print ("TOTO",childEx, childR[2])
                        resultsComparison([childEx], childR[2])
                    else:
                        #print ("Leafvals:", childEx['leafval'], childR[2])
                        # if '(Stopped due to privacy)' in childEx['leafval']:
                        #     childEx['leafval'].replace(' (Stopped due to privacy)', '')
                        if '(Stoppedduetoprivacy)' not in childEx['leafval']:
                            assert (childEx['leafval']== childR[2])
            if find == 0:
                print ("error: Different childs")
                assert 0
    else:
        #print("AAA", jsonExaremeResult, jsonRResult[0])
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
