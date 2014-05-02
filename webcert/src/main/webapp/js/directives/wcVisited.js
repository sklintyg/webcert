define([], function() {
    'use strict';

    /**
     * Directive to keep track of when the user has visited a field in order to show validation messages
     * only after the user have had the opportunity to enter some information.
     */
    return [function() {
        return {

            restrict: 'A',
            require: 'ngModel',

            link: function(scope, element, attrs, ctrl) {
                ctrl.$visited = false;
                // TODO: Replace bind with one after updating to Angular 1.2.x.
                element.bind('blur', function() {
                    element.addClass('wc-visited');
                    scope.$apply(function() {
                        ctrl.$visited = true;
                    });
                });
            }
        };
    }];
});
