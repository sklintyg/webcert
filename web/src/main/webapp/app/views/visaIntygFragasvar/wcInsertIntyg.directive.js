angular.module('webcert').directive('wcInsertCertificate',
    function($compile, $log, $http, $templateCache) {
        'use strict';

        return {
            restrict: 'A',
            replace: true,
            scope: {
                certificateType: '@'
            },
            link: function(scope, element) {

                $http.get('/web/webjars/' + scope.certificateType + '/webcert/views/intyg/intyg.html', { cache: $templateCache }).
                    success(function(file) {
                        element.html(file);
                        element.replaceWith($compile(element.html())(scope));
                    }).
                    error(function(error) {
                        $log.debug(error);
                    });
            }
        };
    });