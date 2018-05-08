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
    for(var i = 0; i < variables.length; i++) {
      if (validVarType.indexOf(variables[i][1]) > -1) {
        result.push(variables[i]);
      }
    }
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
  exa.showFormula = false;
  exa.showVar = true;
  exa.showJson = false;
  exa.showProgress = false;
  exa.showHighChart = false;
  exa.showJS = false;

  exa.toggleSubmitButton = function() {
    exa.showSubmit = true;
  }

  exa.toggleFormulaButton = function() {
    if (exa.showFormula) {
      exa.showFormula = false;
      exa.showVar = true;
    }
    else {
      exa.showFormula = true;
      exa.showVar = false;
    }
  }

  exa.showResult = function(what) {
    if (what === 'PROGRESS') {
      exa.showJson = false;
      exa.showProgress = true;
      exa.showHighChart = false;
      exa.showJS = false;
    }
    if (what === 'CHART') {
      exa.showJson = false;
      exa.showProgress = false;
      exa.showHighChart = true;
      exa.showJS = false;
    }
    if (what === 'JSON') {
      exa.showJson = true;
      exa.showProgress = false;
      exa.showHighChart = false;
      exa.showJS = false;
    }
    if (what === 'JS') {
      exa.showJson = false;
      exa.showProgress = false;
      exa.showHighChart = false;
      exa.showJS = true;
    }
  }

  // Function to convert parameters in the form  of list of datasets or numeric values to string
  exa.object2string = function(object) {
    if (Array.isArray ( object ) || typeof object === 'number'){
      return object.toString();
    }
    else {
      return object;
    }
  };

  // Function to convert a string to an array
  // (WP_LIST_DATASET output will give a list of datasets if more than one, but a string otherwise...)
  exa.string2array = function(string) {
    if (Array.isArray ( string )){
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
      exa.algorithms = [];
      /*
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
            "vartype": ["integer","real","text"],
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

  function IsJsonString(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
  }

  exa.submit = function(algorithm){
    exa.algorithmParams = JSON.parse(JSON.stringify(algorithm.parameters));
    for (var key in exa.algorithmParams) {
      exa.algorithmParams[key].value = exa.object2string(exa.algorithmParams[key].value);
    }
    if(algorithm.name !== 'WP_LIST_VARIABLES'){
      exa.result = {"status": "Processing..."};
    }
    Highcharts.chart('container', exa.result).destroy();
    exa.showResult('PROGRESS');
    $http({
      method: 'POST',
      url: '/mining/query/' + algorithm.name,
      data: exa.algorithmParams
    }).then(function successCallback(response) {
      exa.showResult('JSON');
      if(response.status == 200){
        exa.name = algorithm.name;
        if(exa.name == 'K_MEANS'){      //visual output of K_MEANS
          if (response.data.Error){
            exa.result = response.data
          }
          else{
            var result = response.data;
            if(typeof result.chart !== 'undefined' ){  //every chart is a visual output 2D or 3D
              var chart = Highcharts.chart('container', result);
              // Add mouse and touch events for rotation
              (function (H) {
                function dragStart(eStart) {
                  eStart = chart.pointer.normalize(eStart);

                  var posX = eStart.chartX,
                  posY = eStart.chartY,
                  alpha = chart.options.chart.options3d.alpha,
                  beta = chart.options.chart.options3d.beta,
                  sensitivity = 5; // lower is more sensitive

                  function drag(e) {
                    // Get e.chartX and e.chartY
                    e = chart.pointer.normalize(e);

                    chart.update({
                      chart: {
                        options3d: {
                          alpha: alpha + (e.chartY - posY) / sensitivity,
                          beta: beta + (posX - e.chartX) / sensitivity
                        }
                      }
                    }, undefined, undefined, false);
                  }

                  chart.unbindDragMouse = H.addEvent(document, 'mousemove', drag);
                  chart.unbindDragTouch = H.addEvent(document, 'touchmove', drag);

                  H.addEvent(document, 'mouseup', chart.unbindDragMouse);
                  H.addEvent(document, 'touchend', chart.unbindDragTouch);
                }
                H.addEvent(chart.container, 'mousedown', dragStart);
                H.addEvent(chart.container, 'touchstart', dragStart);
              }(Highcharts));
              exa.showResult('CHART');
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
            exa.showResult('CHART');
          }
        }
        // End only for demo web page
        else if(exa.name == 'WP_LIST_VARIABLES'){
          // Update the variables variable used to display the available variables in the Demo test page.
          exa.variables = response.data.variables;
          // Do not display the result through exa.result
          exa.showJson = false;
        }
        else if (exa.name.indexOf('PIPELINE_ISOUP_') >= 0){
          if (response.data.Error){
            exa.result = response.data
          }
          else{
            exa.result = response.data;
            eval(exa.result);
            exa.showResult('JS');
          }
        }
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
      }
    }, function errorCallback(response) {
      //result = response.data;
      exa.result = response;
    });
  }

  exa.getVariables();

  exa.allAlgos = [
    {
      "name": "WP_LINEAR_REGRESSION",
      "desc": "",
      "type": "multiple_local_global",
      "parameters": [
        {
          "name": "x",
          "desc": "The right part of the linear predictor function, that contains the indepedent variables in an equation supporting the symbols +, :, * , R notation. The independent variables are variables of the input dataset and they should be Real, Float, Integer or text. It cannot be empty.",
          "type": "formula",
          "number": "1-1",
          "vartype": ["integer","real"],
          "value": "adnicategory*apoe4+subjectage+minimentalstate+gender"
        },
        {
          "name": "y",
          "desc": "The left part of the linear predictor function, that contains the dependent variable. The dependent variable is a variable of the input dataset and it should be Real, Float or Integer. It cannot be empty.",
          "type": "variable",
          "number": "1-1",
          "vartype": ["integer","real"],
          "value": "av45"
        },
        {
          "name": "dataset",
          "desc": "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty.",
          "type": "dataset",
          "number": "1-n",
          "value": ""
        },
        {
          "name": "filter",
          "desc": "It contains a filtering operation.It can be empty.",
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
        },
        {
          "name": "dataset",
          "desc": "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty.",
          "type": "dataset",
          "number": "1-n",
          "value": []
        },
        {
          "name": "filter",
          "desc": "It contains a filtering operation.It can be empty.",
          "type": "filter",
          "value": ""
        }
      ]
    },/*
    {
      "name": "WP_LIST_DATASET",
      "desc": "",
      "type": "local_global",
      "parameters": []
    },*/
    {
      "name": "WP_VARIABLE_PROFILE",
      "desc": "",
      "type": "local_global",
      "parameters": [
        {
          "name": "variable",
          "desc": " It is a variable of the input dataset.The variable should be Real, Float, Integer, Text or null. It cannot be empty.",
          "type": "variable",
          "number": "1-1",
          "vartype": ["integer","real","text"],
          "value": "apoe4"
        },
        {
          "name": "dataset",
          "desc": "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty.",
          "type": "dataset",
          "number": "1-n",
          "value": []
        },
        {
          "name": "filter",
          "desc": "It contains a filtering operation.It can be empty.",
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
          "vartype": ["integer","real"],
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
          "desc": "It contains two or more variables of the input dataset. They should be Real, Float, Integer. It cannot be empty.",
          "type": "variable",
          "number": "1-n",
          "vartype": ["integer","real"],
          "value": "apoe4,subjectageyears"
        },
        {
          "name": "k",
          "desc": "It is the number of the clusters. It should be integer.",
          "type": "integer",
          "value": "4"
        },
        {
          "name": "dataset",
          "desc": "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty. ",
          "type": "dataset",
          "number": "1-n",
          "value": []
        },
        {
          "name": "filter",
          "desc":"It contains a filtering operation. It can be empty.",
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
        },
        {
          "name": "dataset",
          "desc": "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty.",
          "type": "dataset",
          "number": "1-n",
          "value": []
        },
        {
          "name": "filter",
          "desc": "It contains a filtering operation.It can be empty.",
          "type": "filter",
          "value": ""
        }
      ]
    },/*
    {
      "name": "WP_LIST_VARIABLES",
      "desc": "",
      "type": "local",
      "parameters": []
    },*/
    {
      "name": "WP_VARIABLES_HISTOGRAM",
      "desc": "",
      "type": "multiple_local_global",
      "parameters": [
        {
          "name": "column1",
          "desc": "It is a variable of the input dataset. The variable should be Real, Float, Integer or null. It cannot be empty.",
          "type": "variable",
          "number": "1-1",
          "vartype": ["integer","real"],
          "value": "subjectageyears"
        },
        {
          "name": "column2",
          "desc": "It is a variable of the input dataset. The variable should be text. It can also be empty.",
          "type": "variable",
          "number": "0-1",
          "vartype": ["text"],
          "value": ""
        },
        {
          "name": "nobuckets",
          "desc": "It is a parameter of the algorithm. It should be integer. It cannot be empty. ",
          "type": "integer",
          "value": "4"
        },
        {
          "name": "dataset",
          "desc": "It contains the names of one or more datasets, in which the algorithm will be executed. It cannot be empty.",
          "type": "dataset",
          "number": "1-n",
          "value": []
        },
        {
          "name": "filter",
          "desc": "It contains a filtering operation.It can be empty.",
          "type": "filter",
          "value": ""
        }
      ]
    }

  ];
});
