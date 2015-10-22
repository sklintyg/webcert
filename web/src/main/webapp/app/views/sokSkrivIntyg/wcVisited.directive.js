/**
 * Directive to keep track of when the user has visited a field in order to show validation messages
 * only after the user have had the opportunity to enter some information.
 */
angular.module('webcert').directive('wcVisited',
    function() {
        'use strict';

        return {

            restrict: 'A',
            require: 'ngModel',

            link: function(scope, element, attrs, ctrl) {
                ctrl.$visited = false;
                element.one('blur', function() {
                    element.addClass('wc-visited');
                    scope.$apply(function() {
                        ctrl.$visited = true;
                    });
                });
            }
        };
    });
