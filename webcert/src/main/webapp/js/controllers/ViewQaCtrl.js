define([], function() {
    'use strict';

    /*
     * Controller for logic related to viewing fraga/svar for signed certs
     */
    return [ '$scope', '$routeParams',
        function($scope, $routeParams) {

            $scope.certificateType = $routeParams.certificateType;

        }
    ];
});
