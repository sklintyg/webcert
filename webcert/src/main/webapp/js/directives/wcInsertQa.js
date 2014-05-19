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

                require(['text!./' + scope.certificateType + '/webcert/views/qa.html'], function(file) {
                    $log.debug(file);
                    element.html(file);
                    element.replaceWith($compile(element.html())(scope));
                });
            }
        };
    }];
});


/*            controller: (function() {
 var controller = null;
 require([$routeParams.certificateType + '/webcert/js/controllers/ViewCertCtrl'], function(file) {
 controller = file;
 return controller;
 });
 return controller;
 })(),
 template: (function() {
 var template = null;
 require(['text!./controllers/' + $routeParams.certificateType + '/webcert/js/directives/view.html'], function(file) {
 template = file;
 return template;
 });
 return template;
 })()*/

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