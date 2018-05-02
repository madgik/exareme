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
                            var chart = new Highcharts.chart('container', result);
                            // Add mouse and touch events for rotation in 3D scatter plot
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
                            exa.result ="";
                         }
                         else{                                  //everything else f.e. 4 variables, give tabular data
                          exa.result = response.data;
                         }
                    }
                 }

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
                
                else{                                           //json output
                 exa.result = response.data;
                }
}
        }, function errorCallback(response) {
            //result = response.data;
            exa.result = response;
        });
    }
});
