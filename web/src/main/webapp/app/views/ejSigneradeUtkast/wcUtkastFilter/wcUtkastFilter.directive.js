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


angular.module('webcert').directive('wcUtkastFilter', ['$timeout', 'webcert.UtkastProxy',
        function($timeout, UtkastProxy) {
            'use strict';

            return {
                restrict: 'E',
                templateUrl: '/app/views/ejSigneradeUtkast/wcUtkastFilter/wcUtkastFilter.directive.html',
                scope: {
                    onSearch: '&',
                    filter: '='
                },
                controller: function($scope) {

                    $scope.maxdate = moment().format('YYYY-MM-DD');

                    $scope.widgetState = {
                        loadingSavedByList: undefined,
                        savedByList: [],
                        searched: false,
                        activeErrorMessageKey: null
                    };

                    //Clicked Search
                    $scope.search = function() {
                        $scope.widgetState.searched = true;
                        $scope.onSearch();
                    };
                    //Clicked Återställ
                    $scope.resetFilter = function() {
                        resetFilterState();
                        $scope.widgetState.searched = false;
                        $timeout (function (){
                            $scope.onSearch();
                        });

                    };

                    function resetFilterState() {
                        $scope.filter.reset();
                        // fiddle with the DOM to get rid of invalid data which isn't bind through the model
                        angular.element('#filter-changedate-from').val('').removeClass('ng-invalid-date');
                        angular.element('#filter-changedate-to').val('').removeClass('ng-invalid-date');
                    }

                    function loadSavedByList() {

                        $scope.widgetState.loadingSavedByList = true;

                        UtkastProxy.getUtkastSavedByList(function(list) {
                            $scope.widgetState.loadingSavedByList = false;
                            $scope.widgetState.savedByList = list || [];
                            $scope.widgetState.savedByList.unshift({
                                    label: 'Visa alla',
                                    id: undefined
                            });

                            $scope.filter.savedByOptions = $scope.widgetState.savedByList;
                            //if only 1 option avaiable it must be 'Visa alla'
                            if ($scope.filter.savedByOptions.length === 1) {
                                $scope.filter.selection.savedBy = undefined;
                            }
                        }, function() {
                            $scope.widgetState.loadingSavedByList = false;
                            $scope.widgetState.savedByList = [{
                                id: undefined,
                                label: '<Kunde inte hämta lista>'
                            }];
                            $scope.filter.savedByOptions = $scope.widgetState.savedByList;
                        });
                    }



                    /**
                     *  Load initial data
                     */
                    loadSavedByList();
                }
            };
        }
    ]
);
