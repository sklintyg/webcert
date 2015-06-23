angular.module('webcert').directive('wcInsertQa',
    function($compile, $log, $http, $templateCache) {
        'use strict';

        return {
            restrict: 'A',
            replace: true,
            scope: {
                certificateType: '@'
            },
            link: function(scope, element) {
                if(scope.certificateType === 'fk7263') {
                    $http.get('/web/webjars/' + scope.certificateType + '/webcert/views/intyg/fragasvar/fragasvar.html', { cache: $templateCache }).
                        success(function(file) {
                            element.html(file);
                            element.replaceWith($compile(element.html())(scope));
                        }).
                        error(function(error) {
                            $log.debug(error);
                        });
                }
            }
        };
    });
