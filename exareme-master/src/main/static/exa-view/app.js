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
                if(exa.name == 'K_MEANS'){
                   var result;
                   result = "foo="+response.data.res;
                   exa.result = response.data.res;
                   var foo;
                   var re = eval(result);
                   Highcharts.chart('container', re);
                }
                else{
                 exa.result = response.data;
                }
}
        }, function errorCallback(response) {
            //result = response.data;
            exa.result = response;
        });
    }
});
