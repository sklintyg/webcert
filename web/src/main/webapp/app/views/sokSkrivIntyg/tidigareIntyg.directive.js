angular.module('webcert').directive('tidigareIntyg',
    function() {
        'use strict';

        return {
            restrict: 'E',
            templateUrl: '/app/views/sokSkrivIntyg/tidigareIntyg.directive.html'
        }
    }
);