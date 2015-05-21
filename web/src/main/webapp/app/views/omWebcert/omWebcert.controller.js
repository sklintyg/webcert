angular.module('webcert').controller('webcert.AboutWebcertCtrl', ['$rootScope', '$scope', 'common.fragaSvarCommonService',
    function($rootScope, $scope, fragaSvarCommonService) {
        'use strict';

        var unbindLocationChange = $rootScope.$on('$locationChangeStart', function($event, newUrl, currentUrl) {
            fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindLocationChange);
        });
        $scope.$on('$destroy', unbindLocationChange);
    }]
);