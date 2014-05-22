define([
    'angular',
    'text!./wcCareUnitClinicSelector.html',
    'webjars/common/webcert/js/services/User'
], function(angular, template, User) {
    'use strict';

    var moduleName = 'wcCareUnitClinicSelector';

    angular.module(moduleName, [ User ]).
        directive(moduleName, [ '$cookieStore', '$rootScope', User,
            function($cookieStore, $rootScope, User) {
                return {
                    restrict: 'A',
                    transclude: false,
                    replace: true,
                    template: template,
                    controller: function($scope) {

                        $scope.units = User.getVardenhetFilterList(User.getValdVardenhet());
                        $scope.units.unshift({id: 'wc-all', namn: 'Alla mottagningars frågor och svar'});
                        $scope.selectedUnit = null;

                        $scope.$on('wc-stat-update', function(event, message) {

                            // Get the latest stats
                            var unitStats = message;

                            // Get the chosen vardgivare
                            var valdVardgivare = User.getValdVardgivare();

                            // Find stats for the chosen vardenhets units below the chosen vardgivare
                            var valdVardenheterStats = {};
                            angular.forEach(unitStats.vardgivare, function(vardgivareStats) {
                                if (vardgivareStats.id === valdVardgivare.id) {
                                    valdVardenheterStats = vardgivareStats.vardenheter;
                                }
                            });

                            // Set stats for each unit available for the filter
                            angular.forEach($scope.units, function(unit) {

                                // If it's the all choice, we know we want the total of everything
                                if (unit.id === 'wc-all') {
                                    unit.fragaSvar = unitStats.fragaSvarValdEnhet;
                                } else {
                                    // Otherwise find the stats for the unit
                                    angular.forEach(valdVardenheterStats, function(unitStat) {
                                        if (unit.id === unitStat.id) {
                                            unit.fragaSvar = unitStat.fragaSvar;
                                        }
                                    });
                                }
                            });
                        });

                        $scope.selectUnit = function(unit) {
                            $scope.selectedUnit = unit;
                            $rootScope.$broadcast('qa-filter-select-care-unit', $scope.selectedUnit);
                        };

                        // Local function getting the first care unit's hsa id in the data struct.
                        function selectFirstUnit(units) {
                            if (typeof units === 'undefined' || units.length === 0) {
                                return null;
                            } else {
                                return units[0];
                            }
                        }

                        function selectUnitById(units, unitName) {
                            for (var count = 0; count < units.length; count++) {
                                if (units[count].id === unitName) {
                                    return units[count];
                                }
                            }
                            return selectFirstUnit(units);
                        }

                        //initial selection
                        if ($scope.units.length === 2) {
                            $scope.selectUnit(selectFirstUnit($scope.units));
                        } else if ($scope.units.length > 2 && $cookieStore.get('enhetsId')) {
                            $scope.selectUnit(selectUnitById($scope.units, $cookieStore.get('enhetsId')));
                        }
                    }
                };
            }
        ]);

    return moduleName;
});
