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

angular.module('webcert').directive('wcUtkastList',
    [ 'common.UtkastNotifyService', 'common.moduleService', 'webcert.intygListService', 'common.IntygHelper',
        function(utkastNotifyService, moduleService, intygListService, IntygHelper) {
            'use strict';

            return {
                restrict: 'E',
                replace: false,
                scope: {
                    utkastList: '=',
                    filter: '=',
                    onOrder: '&'
                },
                templateUrl: '/app/views/ejSigneradeUtkast/wcUtkastList/wcUtkastList.directive.html',
                controller: function($scope) {

                    $scope.sortingProperty = $scope.filter.selection.orderBy;
                    $scope.sortingAscending = $scope.filter.selection.orderAscending;
                    $scope.displayVidarebefordra = intygListService.checkVidareBefordraAuth($scope.utkastList);

                    $scope.getTypeName = function(intygsType) {
                        return moduleService.getModuleName(intygsType);
                    };

                    $scope.openIntyg = function(intyg) {
                        IntygHelper.goToDraft(intyg.intygType, intyg.intygTypeVersion, intyg.intygId);
                    };

                    // Handle forwarding
                    $scope.openMailDialog = function(utkast) {
                        utkast.updateState = {
                            vidarebefordraInProgress: false
                        };
                        utkastNotifyService.notifyUtkast(utkast.intygId, utkast.intygType, utkast, utkast.updateState);
                    };

                    $scope.onNotifyChange = function(utkast) {
                        utkast.updateState = {
                            vidarebefordraInProgress: false
                        };
                        utkastNotifyService.onNotifyChange(utkast.intygId, utkast.intygType, utkast, utkast.updateState);
                    };

                    $scope.orderByProperty = function(property) {
                        var ascending = false;
                        if ($scope.filter.selection.orderBy === property) {
                            ascending = !$scope.filter.selection.orderAscending;
                        }
                        $scope.sortingProperty = property;
                        $scope.sortingAscending = ascending;
                        $scope.onOrder({property: property, ascending: ascending});
                    };
                }
            };
        }
    ]
);
