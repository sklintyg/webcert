angular.module('webcert').controller('webcert.AboutWebcertCtrl', ['$rootScope', '$scope', 'common.fragaSvarCommonService', 'common.AvtalProxy',
    function($rootScope, $scope, fragaSvarCommonService, avtalProxy) {
        'use strict';

        var unbindLocationChange = $rootScope.$on('$locationChangeStart', function($event, newUrl, currentUrl) {
            fragaSvarCommonService.checkQAonlyDialog($scope, $event, newUrl, currentUrl, unbindLocationChange);
        });
        $scope.$on('$destroy', unbindLocationChange);

        $scope.avtalText = '';

        function bindLatestAvtalToScope(avtal) {
            $scope.avtalText = avtal.avtalText;
        }

        avtalProxy.getLatestAvtal(bindLatestAvtalToScope, function(err) {
            //if (window.console) window.console.log(err);
        });
    }]
);