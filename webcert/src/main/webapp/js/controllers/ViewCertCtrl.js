define([], function() {
    'use strict';

    /*
     * Controller for logic related to viewing signed certs
     */
    return [ '$scope', '$routeParams',
        function($scope, $routeParams) {

            $scope.widgetState = {
                certificateType: $routeParams.certificateType
            };
        }
    ];
});
