angular.module('webcert').directive('wcCareUnitClinicSelector',
    [ '$cookieStore', '$rootScope', '$timeout', 'common.User',
        function($cookieStore, $rootScope, $timeout, User) {
            'use strict';

            return {
                restrict: 'A',
                transclude: false,
                replace: true,
                templateUrl: '/js/directives/wcCareUnitClinicSelector.html',
                controller: function($scope) {

                    $scope.units = User.getVardenhetFilterList(User.getValdVardenhet());
                    $scope.units.unshift({id: 'wc-all', namn: 'Alla frågor och svar på denna vårdenhet'});
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
                                unit.tooltip =
                                    'Totalt antal ej hanterade frågor och svar för den vårdenhet där du är inloggad. ' +
                                    'Här visas samtliga frågor och svar på vårdenhetsnivå och på mottagningsnivå.';
                            } else {
                                // Otherwise find the stats for the unit
                                angular.forEach(valdVardenheterStats, function(unitStat) {
                                    if (unit.id === unitStat.id) {
                                        unit.fragaSvar = unitStat.fragaSvar;
                                        unit.tooltip =
                                            'Det totala antalet ej hanterade frågor och svar som finns registrerade på ' +
                                            'vårdenheten. Det kan finnas frågor och svar som gäller denna vårdenhet men ' +
                                                'som inte visas här. För säkerhets skull bör du även kontrollera frågor ' +
                                                'och svar för övriga vårdenheter och mottagningar.';
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

                    //initial selection, now handles cases when no enhetsId cookie has been set.
                    if ($scope.units.length > 2 && $cookieStore.get('enhetsId')) {
                        $scope.selectUnit(selectUnitById($scope.units, $cookieStore.get('enhetsId')));
                    } else {
                        $scope.selectUnit(selectFirstUnit($scope.units));
                    }
                }
            };
        }]);
