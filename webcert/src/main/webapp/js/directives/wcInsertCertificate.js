define(['angular'], function(angular) {
    'use strict';

    return ['$log', '$compile', function($log, $compile) {
        return {
            restrict: 'A',
            replace: true,
            scope: {
                certificateType: '@'
            },
            link: function(scope, element, attrs) {

                require(['text!./' + scope.certificateType + '/webcert/views/intyg.html'], function(file) {
                    element.html(file);
                    element.replaceWith($compile(element.html())(scope));
                });
            }
        };
    }];
});
