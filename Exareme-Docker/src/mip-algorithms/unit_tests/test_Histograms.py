import requests
import json
import logging

# Required datasets: adni

endpointUrl='http://88.197.53.100:9090'


def test_HISTOGRAM_1():
    logging.info("---------- TEST : Histogram ADNI 1")

    data = [
                {
                    "name": "column1",
                    "value": "subjectageyears"
                },
                {
                    "name": "column2",
                    "value": ""
                },
                {
                    "name": "nobuckets",
                    "value": "4"
                },
                {
                    "name": "dataset",
                    "value": "adni"
                },
                {
                    "name": "filter",
                    "value": ""
                }
            ]
    
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl+'/mining/query/VARIABLES_HISTOGRAM',data=json.dumps(data),headers=headers)
    
    result = json.loads(r.text)
    
	
    """
    Results from exareme:
    
    {
      "chart": {
        "type": "column"
      },
      "title": {
        "text": "Histogram"
      },
      "subtitle": {
        "text": " subjectageyears "
      },
      "xAxis": {
        "categories": [
          "55.0 - 63.7525",
          "63.7525 - 72.505",
          "72.505 - 81.2575",
          "81.2575 - 90.01"
        ],
        "crosshair": true
      },
      "yAxis": {
        "min": 0,
        "title": {
          "text": "Number of Participants"
        }
      },
      "tooltip": {
        "headerFormat": "<span style='font-size:10px'>{point.key}</span><table>",
        "pointFormat": "<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>",
        "footerFormat": "</table>",
        "shared": true,
        "useHTML": true
      },
      "plotOptions": {
        "column": {
          "pointPadding": 0.2,
          "borderWidth": 0
        }
      },
      "series": [
        {
          "name": " subjectageyears ",
          "data": [
            120,
            363,
            426,
            157
          ]
        }
      ]
    }
    """
	   
    assert result['xAxis']['categories'][0] == "55.0 - 63.7525"
    assert result['xAxis']['categories'][1] == "63.7525 - 72.505"
    assert result['xAxis']['categories'][2] == "72.505 - 81.2575"
    assert result['xAxis']['categories'][3] == "81.2575 - 90.01"
    
    assert result['series'][0]['data'][0] == 120    
    assert result['series'][0]['data'][1] == 363    
    assert result['series'][0]['data'][2] == 426    
    assert result['series'][0]['data'][3] == 157



def test_HISTOGRAM_2():
    logging.info("---------- TEST : Histogram ADNI 2")

    data = [
                {
                    "name": "column1",
                    "value": "subjectageyears"
                },
                {
                    "name": "column2",
                    "value": "gender"
                },
                {
                    "name": "nobuckets",
                    "value": "4"
                },
                {
                    "name": "dataset",
                    "value": "adni"
                },
                {
                    "name": "filter",
                    "value": ""
                }
            ]
    
    headers = {'Content-type': 'application/json', "Accept": "text/plain"}
    r = requests.post(endpointUrl+'/mining/query/VARIABLES_HISTOGRAM',data=json.dumps(data),headers=headers)
    
    result = json.loads(r.text)
    
    """
    Results from exareme:
    
    {
      "chart": {
        "type": "column"
      },
      "title": {
        "text": "Histogram"
      },
      "subtitle": {
        "text": " subjectageyears - gender "
      },
      "xAxis": {
        "categories": [
          "55.0 - 63.7525",
          "63.7525 - 72.505",
          "72.505 - 81.2575",
          "81.2575 - 90.01"
        ],
        "crosshair": true
      },
      "yAxis": {
        "min": 0,
        "title": {
          "text": "Number of Participants"
        }
      },
      "tooltip": {
        "headerFormat": "<span style='font-size:10px'>{point.key}</span><table>",
        "pointFormat": "<tr><td style='color:{series.color};padding:0'>{series.name}: </td><td style='padding:0'><b>{point.y:.0f} </b></td></tr>",
        "footerFormat": "</table>",
        "shared": true,
        "useHTML": true
      },
      "plotOptions": {
        "column": {
          "pointPadding": 0.2,
          "borderWidth": 0
        }
      },
      "series": [
        {
          "name": " F ",
          "data": [
            65,
            186,
            182,
            52
          ]
        },
        {
          "name": " M ",
          "data": [
            55,
            177,
            244,
            105
          ]
        }
      ]
    }
    """
    
    assert result['xAxis']['categories'][0] == "55.0 - 63.7525"
    assert result['xAxis']['categories'][1] == "63.7525 - 72.505"
    assert result['xAxis']['categories'][2] == "72.505 - 81.2575"
    assert result['xAxis']['categories'][3] == "81.2575 - 90.01"
    
    assert result['series'][0]['data'][0] == 65    
    assert result['series'][0]['data'][1] == 186    
    assert result['series'][0]['data'][2] == 182    
    assert result['series'][0]['data'][3] == 52
    
    assert result['series'][1]['data'][0] == 55    
    assert result['series'][1]['data'][1] == 177    
    assert result['series'][1]['data'][2] == 244    
    assert result['series'][1]['data'][3] == 105
    