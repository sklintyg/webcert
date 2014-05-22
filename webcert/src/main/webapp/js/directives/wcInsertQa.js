define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wcInsertQa';

    angular.module(moduleName, []).
        directive(moduleName, [ '$compile', function($compile) {
            return {
                restrict: 'A',
                replace: true,
                scope: {
                    certificateType: '@'
                },
                link: function(scope, element) {

                    require([ 'text!./' + scope.certificateType + '/webcert/views/fragasvar.html' ], function(file) {
                        element.html(file);
                        element.replaceWith($compile(element.html())(scope));
                    });
                }
            };
        }]);

    return moduleName;
});
