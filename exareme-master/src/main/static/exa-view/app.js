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
controller('ExaController', function($scope, $http){
    var exa = this;
    exa.algorithms = [];
    exa.result = [];
    exa.name = '';

    $http({
        method: 'GET',
        url: '/mining/algorithms.json'
    }).then(function successCallback(response) {
        if (response.status == 200) {
            exa.algorithms = response.data;
        }
    }, function errorCallback(response) {
        exa.algorithms = [];
    });

    exa.submit = function(algorithm){
        exa.result = {};
        Highcharts.chart('container', exa.result).destroy();
        $http({
            method: 'POST',
            url: '/mining/query/' + algorithm.name,
            data: algorithm.parameters
        }).then(function successCallback(response) {
            if(response.status == 200){
                exa.name = algorithm.name;
                if(exa.name == 'K_MEANS_VISUAL'){      //visual output of K_MEANS
                    if (response.data.Error){
                        exa.result = response.data
                    }
                    else{
                         var result = response.data;
                         if(typeof result.chart !== 'undefined' ){  //every chart is a visual output 2D or 3D
                            Highcharts.chart('container', result);
                            exa.result ="";
                         }
                         else{                                  //everything else f.e. 4 variables, gives tabular data
                          exa.result = response.data;
                         }
                    }
                 }
                /*
                else if(exa.name == 'WP_VARIABLES_HISTOGRAM') {  //WP_VARIABLES_HISTOGRAM visual output
                    if (response.data.Error){
                        exa.result = response.data
                    }
                    else{
                        exa.result = response.data;
                        Highcharts.chart('container',  exa.result);
                        exa.result ="";
                    }
                }
                */
                else{                       //json output
                 exa.result = response.data;
                }
}
        }, function errorCallback(response) {
            //result = response.data;
            exa.result = response;
        });
    }
});
