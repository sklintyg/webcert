angular.module('webcert').directive('wcInsertCertificate',
    function($compile, $log) {
        'use strict';

        return {
            restrict: 'A',
            replace: true,
            scope: {
                certificateType: '@'
            },
            link: function(scope, element) {

                $.get('/web/webjars/' + scope.certificateType + '/webcert/views/intyg.html').then(function(file) {
                    element.html(file);
                    element.replaceWith($compile(element.html())(scope));
                }).fail(function(error) {
                    $log.debug(error);
                });

                /*require([ 'text!./' + scope.certificateType + '/webcert/views/intyg.html' ], function(file) {
                 element.html(file);
                 element.replaceWith($compile(element.html())(scope));
                 });*/
            }
        };
    });