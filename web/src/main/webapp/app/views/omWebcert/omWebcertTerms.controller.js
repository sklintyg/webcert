angular.module('webcert').controller('webcert.AboutWebcertTermsCtrl',
    ['$scope', '$log', 'common.AvtalProxy',
        function($scope, $log, avtalProxy) {
            'use strict';

            $scope.state = {
                doneLoading: false
            };

            avtalProxy.getLatestAvtal(function(avtal) {
                $scope.avtal = avtal;
                $scope.state.doneLoading = true;
            }, function(err) {
                $log.error('Kunde inte hämta senaste avtal');
                $scope.state.doneLoading = true;
            });
        }]
);