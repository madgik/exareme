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
controller('ExaController', function($http, $scope){
  var exa = this;
  exa.properties ={};
  exa.result = [];

  $http({
    method: 'GET',
    url: '/mining/algorithms.json'
  }).then(function successCallback(response) {
    // this callback will be called asynchronously
    // when the response is available
    if(response.status == 200){
      exa.properties = response.data;
    }
  }, function errorCallback(response) {
    // called asynchronously if an error occurs
    // or server returns response with an error status.
    exa.properties = {};
  });

  exa.query = function(index){

    $http({
        method: 'POST',
        url: '/mining/query/' + $scope.exa.properties['algorithms'][index]['name'],
        data: $scope.exa.properties['algorithms'][index]['parameters'],
        transformResponse: function(data){
            var response = [];
            var split = data.split('\n');
            for(var r in split){
                if(split[r]) {
                    response.push(JSON.parse(split[r]))
                }
            }
            return response;
        }
    }).then(function successCallback(response) {
      // this callback will be called asynchronously
      // when the response is available
      if(response.status == 200){
            exa.result = response.data;
        }
    }, function errorCallback(response) {
      // called asynchronously if an error occurs
      // or server returns response with an error status.
      exa.result = [];
    });
  };
});
