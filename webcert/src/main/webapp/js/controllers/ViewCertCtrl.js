define([
    'angular'
], function(angular) {
    'use strict';

    var moduleName = 'wc.ViewCertCtrl';

    /*
     * Controller for logic related to viewing signed certs
     */
    angular.module(moduleName, []).
        controller(moduleName, [ '$routeParams', '$scope',
            function($routeParams, $scope) {

                $scope.widgetState = {
                    certificateType: $routeParams.certificateType
                };
            }
        ]);

    return moduleName;
});
