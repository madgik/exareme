'use strict';

// Declare app level module which depends on views, and components
angular.module('myApp', [
  'ngRoute',
  'myApp.view1',
  'myApp.view2',
  'myApp.version'
]).
config(['$locationProvider', '$routeProvider', function($locationProvider, $routeProvider) {
  $locationProvider.hashPrefix('!');

  $routeProvider.otherwise({redirectTo: '/view1'});
}]).
filter('validType', function() {
  return function(variables, validVarType){
    if(validVarType == undefined){return variables;}
    var result = [];
    //var count = 0;
    //alert ("validType filter: " + validVarType + " " + variables);
    for(var i = 0; i < variables.length; i++) {
      /*if (count < 5){
        alert (variables[i][0] + " " + variables[i][1] + ", pos=" + validVarType.indexOf(variables[i][1]));
        count = count + 1;
      }*/
      if (validVarType.indexOf(variables[i][1]) > -1) {
        //if (count < 5){ alert (variables[i][0]);}
        result.push(variables[i]);
      }
    }
    //alert( result);
    return result;
  }
}).
controller('ExaController', function($scope, $http){
    var exa = this;
    exa.algorithms = [];
    exa.selectedAlgo = {};
    exa.result = {};
    exa.name = '';
    exa.datasets = '';
    exa.variables = '';
    exa.showSubmit = false;

    exa.toggleSubmitButton = function() {
      exa.showSubmit = true;
    }

    // Function to convert list of datasets from array to string
    // (Used to transform the "dataset" input variable of algorithms from an array to Exareme's expected input, i.e., a string)
    exa.array2string = function(array) {
      if (Array.isArray ( array )){
        return array.toString();
      }
      else {
        return array;
      }
    };

    // Function to convert a string to an array
    // (WP_LIST_DATASET output will give a list of datasets if more than one, but a string otherwise...)
    exa.string2array = function(string) {
      if (Array.isArray ( array ) || typeof array === 'number'){
        return string;
      }
      else {
        return string.split(',');
      }
    };

    exa.getDatasets = function(){
      var algorithm = {"name":"WP_LIST_DATASET","desc":"","type":"local_global","parameters":[]};
      exa.submit(algorithm);
    }

    exa.getVariables = function(){
      var algorithm = {"name": "WP_LIST_VARIABLES","desc": "","type": "local","parameters": []};
      exa.submit(algorithm);
    }

    $http({
        method: 'GET',
        url: '/mining/algorithms.json'
    }).then(function successCallback(response) {
        if (response.status == 200) {
            /*exa.algorithms = [];

            for (var key in response.data) {
              if (response.data[key].name != "WP_LIST_DATASET" && response.data[key].name != "WP_LIST_VARIABLES"){
                var alg = response.data[key];
                for (var param in alg.parameters) {
                  alg.parameters[param].value="";
                }
                exa.algorithms.push(alg);
              }
            }
            */
            exa.algorithms = exa.allAlgos;
/*
            exa.algorithms = [];

            for (var key in exa.allAlgos) {
              if (response.data[key].name != "WP_LIST_DATASET" && response.data[key].name != "WP_LIST_VARIABLES"){
                var alg = response.data[key];
                for (var param in alg.parameters) {
                  alg.parameters[param].value="";
                }
                exa.algorithms.push(alg);
              }
            }
            */

            exa.selectedAlgo = {
              "name": "DEMO",
              "desc": "",
              "type": "multiple_local_global",
              "parameters": [
                {
                  "name": "variable",
                  "desc": "",
                  "type": "variable",
                  "number": "",
                  "vartype": ["text"],
                  "value": ""
                },
                {
                  "name": "dataset",
                  "desc": "",
                  "type": "dataset",
                  "number": "1-n",
                  "value": []
                }
              ]
            };
            exa.algorithms.push(exa.selectedAlgo);

            for (var key in exa.algorithms) {
              for (var param in exa.algorithms[key].parameters) {
                 exa.algorithms[key].parameters[param].value="";
              }
            }
            exa.algorithms = exa.algorithms.sort(function(a, b){return a.name>b.name})
        }
    }, function errorCallback(response) {
        exa.algorithms = [];
    });

    exa.submit = function(algorithm){
        exa.algorithmParams = JSON.parse(JSON.stringify(algorithm.parameters));
        for (var key in exa.algorithmParams) {
          exa.algorithmParams[key].value = exa.array2string(exa.algorithmParams[key].value);
        }
        exa.result = {"status": "Processing..."};
        Highcharts.chart('container', exa.result).destroy();
        $http({
            method: 'POST',
            url: '/mining/query/' + algorithm.name,
            data: exa.algorithmParams
        }).then(function successCallback(response) {
            if(response.status == 200){
                exa.name = algorithm.name;
                if(exa.name == 'K_MEANS'){      //visual output of K_MEANS
                    if (response.data.Error){
                        exa.result = response.data
                    }
                    else{
                         var result = response.data;
                         if(typeof result.chart !== 'undefined' ){  //every chart is a visual output 2D or 3D
                            Highcharts.chart('container', result);
			    delete exa.result;
                         }
                         else{                                  //everything else f.e. 4 variables, gives tabular data
                          exa.result = response.data;
                         }
                    }
                 }
                // Only for demo web page ?
                else if(exa.name == 'WP_VARIABLES_HISTOGRAM') {  //WP_VARIABLES_HISTOGRAM visual output
                    if (response.data.Error){
                        exa.result = response.data
                    }
                    else{
                        exa.result = response.data;
                        Highcharts.chart('container',  exa.result);
			delete exa.result;
                    }
                }
                // End only for demo web page
                else{                       //json output
                 exa.result = response.data;
                }

                if(exa.name == 'WP_LIST_DATASET'){
                  // Update the datasets variable used to display the available datasets in the Demo test page.
                  exa.datasets = exa.result.result;
                  for (var key in exa.datasets) {
                    exa.datasets[key].Datasets = exa.string2array(exa.datasets[key].Datasets);
                  }
                }
                else if(exa.name == 'WP_LIST_VARIABLES'){
                  // Update the variables variable used to display the available variables in the Demo test page.
                  exa.variables = exa.result.variables;
                }
}
        }, function errorCallback(response) {
            //result = response.data;
            exa.result = response;
        });
    }

    exa.allAlgos = [
      {
        "name": "WP_LINEAR_REGRESSION",
        "desc": "",
        "type": "multiple_local_global",
        "parameters": [
          {
            "name": "x",
            "desc": "",
            "type": "variable",
            "number": "1-1",
            "vartype": ["integer","real"],
            "value": "adnicategory*apoe4+subjectage+minimentalstate+gender"
          },
          {
            "name": "y",
            "desc": "",
            "type": "variable",
            "number": "1-1",
            "vartype": ["integer","real"],
            "value": "av45"
          },
          {
            "name": "dataset",
            "desc": "",
            "type": "dataset",
            "number": "1-n",
            "value": ""
          },
          {
            "name": "filter",
            "desc": "",
            "type": "filter",
            "value": ""
          }
        ]
      },
      {
        "name": "PIPELINE_ISOUP_MODEL_TREE_SERIALIZER",
        "desc": "PIPELINE_ISOUP_MODEL_TREE_SERIALIZER",
        "type": "pipeline",
        "responseContentType": "application/visjs+javascript",
        "parameters": [
          {
            "name": "target_attributes",
            "desc": "",
            "type": "variable",
            "number": "1-n",
            "vartype": ["integer","real"],
            "value": "apoe4"
          },
          {
            "name": "descriptive_attributes",
            "desc": "",
            "type": "variable",
            "number": "1-n",
            "vartype": ["integer","real"],
            "value": "subjectageyears,av45"
          }
        ]
      },
      {
        "name": "WP_LIST_DATASET",
        "desc": "",
        "type": "local_global",
        "parameters": []
      },
      {
        "name": "WP_VARIABLE_PROFILE",
        "desc": "",
        "type": "local_global",
        "parameters": [
          {
            "name": "variable",
            "desc": "",
            "type": "variable",
            "number": "1-1",
            "vartype": ["integer","real","text"],
            "value": "apoe4"
          },
          {
            "name": "dataset",
            "desc": "",
            "type": "dataset",
            "number": "1-n",
            "value": []
          },
          {
            "name": "filter",
            "desc": "",
            "type": "filter",
            "value": ""
          }
        ]
      },
      {
        "name": "WP_VARIABLE_SUMMARY",
        "desc": "",
        "type": "local_global",
        "parameters": [
          {
            "name": "variable",
            "desc": "",
            "type": "variable",
            "number": "1-1",
            "vartype": ["integer","real","text"],
            "value": "apoe4"
          },
          {
            "name": "dataset",
            "desc": "",
            "type": "dataset",
            "number": "1-n",
            "value": []
          },
          {
            "name": "filter",
            "desc": "",
            "type": "filter",
            "value": ""
          }
        ]
      },
      {
        "name": "K_MEANS",
        "desc": "JSON",
        "type": "multiple_local_global",
        "parameters": [
          {
            "name": "columns",
            "desc": "columns value",
            "type": "variable",
            "number": "1-n",
            "vartype": ["integer","real"],
            "value": "apoe4,subjectageyears"
          },
          {
            "name": "k",
            "desc": "#centers",
            "type": "integer",
            "value": "4"
          },
          {
            "name": "dataset",
            "desc": "",
            "type": "dataset",
            "number": "1-n",
            "value": []
          },
          {
            "name": "filter",
            "desc": "",
            "type": "filter",
            "value": ""
          }
        ]
      },
      {
        "name": "PIPELINE_ISOUP_REGRESSION_TREE_SERIALIZER",
        "desc": "PIPELINE_ISOUP_REGRESSION_TREE_SERIALIZER",
        "type": "pipeline",
        "responseContentType": "application/visjs+javascript",
        "parameters": [
          {
            "name": "target_attributes",
            "desc": "",
            "type": "variable",
            "number": "1-n",
            "value": "apoe4"
          },
          {
            "name": "descriptive_attributes",
            "desc": "",
            "type": "variable",
            "number": "1-n",
            "value": "subjectageyears,av45"
          }
        ]
      },
      {
        "name": "WP_LIST_VARIABLES",
        "desc": "",
        "type": "local",
        "parameters": []
      },
      {
        "name": "WP_VARIABLES_HISTOGRAM",
        "desc": "",
        "type": "multiple_local_global",
        "parameters": [
          {
            "name": "column1",
            "desc": "Column1 must be Real, Float or Integer",
            "type": "variable",
            "number": "1-1",
            "vartype": ["integer","real"],
            "value": "subjectageyears"
          },
          {
            "name": "column2",
            "desc": "Column2 must be Text or Null",
            "type": "variable",
            "number": "0-1",
            "vartype": ["text"],
            "value": ""
          },
          {
            "name": "nobuckets",
            "desc": "",
            "type": "integer",
            "value": "4"
          },
          {
            "name": "dataset",
            "desc": "",
            "type": "dataset",
            "number": "1-n",
            "value": []
          },
          {
            "name": "filter",
            "desc": "",
            "type": "filter",
            "value": ""
          }
        ]
      }

    ];
});
