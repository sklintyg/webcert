angular.module('showcase').controller('showcase.BootstrapCtrl',
    ['$scope', '$timeout',
        function($scope, $timeout) {
            'use strict';

            //Vidarebefodra knapp states
            $scope.cert1 = {
                vidarebefordrad: false
            };
            $scope.cert2 = {
                vidarebefordrad: true
            };

            //Visa/dölj länk knapp
            $scope.linkBtnState = false;


            $scope.fetchingMoreInProgress = false;
            $scope.simulateFetchMore = function() {
                $scope.fetchingMoreInProgress = true;
                $timeout(function() {
                    $scope.fetchingMoreInProgress = false;
                }, 2000);
            };

            $scope.acceptprogressdone = true;
            $scope.simulateProgress = function() {
                $scope.acceptprogressdone = false;
                $timeout(function() {
                    $scope.acceptprogressdone = true;
                }, 2000);
            };

            $scope.onHideAlert = function() {
                $scope.hideAlert = true;
                $timeout(function() {
                    $scope.hideAlert = false;
                }, 2000);
            };

            $scope.tableitems = [];
            for (var i = 0; i < 10; i++) {
                $scope.tableitems.push({id: i, value: "Item " + i});
            }

        }]);

