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
    [ '$state', 'common.UtkastNotifyService', 'webcert.UtkastProxy',
        function($state, utkastNotifyService, UtkastProxy) {
            'use strict';

            return {
                restrict: 'E',
                replace: true,
                scope: {
                    utkastList: '='
                },
                templateUrl: '/app/views/ejSigneradeUtkast/wcUtkastList.directive.html',
                controller: function($scope) {

                $scope.intygTypes = null;

                function loadIntygTypes() {
                    if ($scope.intygTypes === null) {
                        UtkastProxy.getUtkastTypesCachedUnfiltered(function(data) {
                            $scope.intygTypes = data;
                        });
                    }
                }
                loadIntygTypes();

                $scope.getTypeName = function(intygsType) {
                    var name;
                    if ($scope.intygTypes !== null) {
                        var intygTypes = $scope.intygTypes.filter(function(intygType) {
                            return (intygType.id === intygsType);
                        });
                        if (intygTypes && intygTypes.length > 0) {
                            name = intygTypes[0].label;
                        }
                    }
                    return name;
                };

                    $scope.openIntyg = function(intyg) {
                        $state.go(intyg.intygType + '-edit', {
                            certificateId: intyg.intygId
                        });
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
                }
            };
        }
    ]
);
