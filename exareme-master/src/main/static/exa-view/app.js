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
        $http({
            method: 'POST',
            url: '/mining/query/' + algorithm.name,
            data: algorithm.parameters
            //transformResponse: function(data){
            //    var response = [];
            //    var split = data.split('\n');
            //    for(var r in split){
            //        if(split[r]) {
            //            response.push(JSON.parse(split[r]))
            //        }
            //    }
            //    return response;
            //}
        }).then(function successCallback(response) {
            if(response.status == 200){
                exa.result = response.data;
            }
        }, function errorCallback(response) {
            exa.result = {};
        });
    }
});
