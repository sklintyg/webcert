'use strict';

/* Directives */
angular.module('wcDashBoardApp').directive("wcCareUnitClinicSelector", ['$rootScope', function ($rootScope) {
    return {
        restrict: "A",
        transclude: false,
        replace: true,
        template: '<div>\n' +
        '    <div class="control-group">\n' +
        '        <div class="btn-group">\n' +
        '            <select ng-model="valdVardenhet" ng-options="vardenhet.namn for vardenhet in vardenheter" ng-change="updateVardenhetsval()"></select>\n' +
        '        </div>\n' +
        '        <div class="btn-group">\n' +
        '            <select ng-disabled="mottagningar.length==0" ng-model="valdMottagning" ng-options="mottagning.namn for mottagning in mottagningar" ng-change="updateMottagningsval()"></select>\n' +
        '        </div>\n' +
        '    </div>\n',
        controller: function ($scope) {
            // init
            $scope.vardenheter = $rootScope.MODULE_CONFIG.VARDENHETER.vardenheter;

            $scope.valdVardenhet = selectFirstVardenhet($scope.vardenheter);
            $rootScope.$broadcast("vardenhet", $scope.valdVardenhet);

            $scope.mottagningar = $scope.valdVardenhet.mottagningar;
            $scope.valdMottagning = $scope.valdVardenhet.mottagningar[0];

            $scope.updateVardenhetsval = function () {
                $scope.mottagningar = $scope.valdVardenhet.mottagningar;
                $scope.valdMottagning = $scope.valdVardenhet.mottagningar[0];
                $rootScope.$broadcast("vardenhet", $scope.valdVardenhet);
            }

            $scope.updateMottagningsval = function () {
                $rootScope.$broadcast("mottagning", $scope.valdMottagning);
            }

            // Local function getting the first care unit's hsa id in the data struct.
            function selectFirstVardenhet(vardenheter) {
                if (typeof vardenheter === "undefined" || vardenheter.length == 0) {
                    return null;
                } else {
                    return vardenheter[0];
                }
            }
        }
    };
}]);