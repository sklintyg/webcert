/*
 * Copyright (C) 2016 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


angular.module('webcert').directive('wcUtkastFilter',
    [ '$cookies', '$q', 'webcert.UtkastFilterModel', 'webcert.UtkastProxy',
        function($cookies, $q, UtkastFilterModel, UtkastProxy) {
            'use strict';

            return {
                restrict: 'E',
                replace: true,
                templateUrl: '/app/views/ejSigneradeUtkast/wcUtkastFilter.directive.html',
                scope: {
                    onSearch: '&'
                },
                controller: function($scope) {

                    // Default API filter states
                    var defaultSavedByChoice = {
                        name: 'Visa alla',
                        hsaId: undefined
                    };

                    $scope.widgetState = {
                        loadingSavedByList: undefined,
                        savedByList: [],
                        searched: false,
                        activeErrorMessageKey: null
                    };
                    $scope.filter = UtkastFilterModel.build();

                    $scope.search = function() {
                        $scope.widgetState.searched = true;
                        $cookies.putObject('unsignedCertFilter', $scope.filter);
                        $scope.onSearch({filter : $scope.filter});
                    };

                    $scope.resetFilter = function() {
                        $cookies.remove('unsignedCertFilter');
                        resetFilterState();
                        $scope.onSearch({filter : $scope.filter});
                    };

                    function resetFilterState() {
                        $scope.filter.reset();
                    }

                    function loadSavedByList() {

                        $scope.widgetState.loadingSavedByList = true;

                        UtkastProxy.getUtkastSavedByList(function(list) {
                            $scope.widgetState.loadingSavedByList = false;
                            $scope.widgetState.savedByList = list;
                            if (list && (list.length > 0)) {
                                $scope.widgetState.savedByList.unshift(defaultSavedByChoice);
                            }
                        }, function() {
                            $scope.widgetState.loadingSavedByList = false;
                            $scope.widgetState.savedByList = [{
                                hsaId: undefined,
                                name: '<Kunde inte hämta lista>'
                            }];
                        });
                    }

                    function loadFilterForm() {

                        resetFilterState();

                        // Use saved choice if cookie has saved a filter
                        var savedFilter = $cookies.getObject('unsignedCertFilter');
                        if (savedFilter) {
                            angular.extend($scope.filter, savedFilter);
                            $scope.onSearch({filter : $scope.filter});
                        }
                    }

                    /**
                     *  Load initial data
                     */
                    loadSavedByList();
                    loadFilterForm();
                }
            };
        }
    ]
);