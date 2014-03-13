define([
    'text!directives/wcCareUnitClinicSelector.html'
], function (template) {
    'use strict';

    return ['$rootScope', '$cookieStore', 'User', function ($rootScope, $cookieStore, User) {
        return {
            restrict : 'A',
            transclude : false,
            replace : true,
            template : template,
            controller : function ($scope) {

                $scope.units = User.getVardenhetFilterList(User.getValdVardenhet());
                $scope.selectedUnit = null;

                $scope.selectUnit = function (unit) {
                    $scope.selectedUnit = unit;
                    $rootScope.$broadcast('qa-filter-select-care-unit', $scope.selectedUnit);
                };

                //initial selection
                if ($scope.units.length === 1) {
                    $scope.selectUnit(selectFirstUnit($scope.units));
                } else if ($scope.units.length > 1 && $cookieStore.get('enhetsId')) {
                    $scope.selectUnit(selectUnitById($scope.units, $cookieStore.get('enhetsId')));
                }

                // Local function getting the first care unit's hsa id in the data struct.
                function selectFirstUnit (units) {
                    if (typeof units === 'undefined' || units.length === 0) {
                        return null;
                    } else {
                        return units[0];
                    }
                }

                function selectUnitById (units, unitName) {
                    for (var count = 0; count < units.length; count++) {
                        if (units[count].id === unitName) {
                            return units[count];
                        }
                    }
                    return selectFirstUnit(units);
                }
            }
        };
    }];
});
