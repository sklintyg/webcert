'use strict';

/* Controllers */
webcertApp.controller('WebcertCtrl', [ '$scope', '$filter', '$location', '$window', function WebcertCtrl($scope, $filter, $location, $window) {

    $scope.newCertificate = function(type) {
        console.log("new certificate - " + type);
        var path = "/m/" + type.toLowerCase() + "/webcert/intyg/123456"
        $window.location.href = path;
    }

} ]);
