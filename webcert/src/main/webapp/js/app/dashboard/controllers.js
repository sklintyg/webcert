'use strict';

/* Controllers */
angular.module('wcDashBoardApp').controller('WebcertCtrl', [ '$scope', '$location', '$window', function WebcertCtrl($scope, $location, $window) {

    $scope.newCertificate = function(type) {
        var path = "/m/" + type.toLowerCase() + "/webcert/intyg/123456"
        $window.location.href = path;
    }

} ]);
