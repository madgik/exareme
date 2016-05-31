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
controller('ExaController', function($http){
  var exa = this;
  exa.properties ={};
  $http({
    method: 'GET',
    url: '/someUrl'
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
});
