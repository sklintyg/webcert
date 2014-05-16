define(['angular'], function(angular) {
    'use strict';

    return ['$log', '$routeParams', function($log, $routeParams) {
        return {
            restrict: 'A',
            replace: true,
            controller: (function() {
                var controller = require($routeParams.certificateType + '/webcert/js/controllers/ViewCertCtrl');
                return controller;
            })(),
            template: (function() {
                var template = require('text!./controllers/' + $routeParams.certificateType + '/webcert/js/directives/view.html');
                return template;
            })()
        };
    }];
});

/*
scope: {
    //                certType: '='
},
link: function(scope, element/*, attrs, ctrl*//*) {

    $log.debug('Opening intyg ' + scope.certType + ',' + $routeParams.certificateType);

    scope.certType = 'view-' + $routeParams.certificateType;
    /*                var certView = angular.element('<div {{certType}}></div>');
     $compile(certView)(scope);
     element.parent().append(certView);*/
/*
    scope.directiveCode = '<div ' + scope.certType + '></div>';
},
*/