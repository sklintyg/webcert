/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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
angular.module('webcert').controller('normal.EnhetsvalPageCtrl',
        [ '$scope', '$window', '$state', '$stateParams', '$uibModal', 'common.UserModel', 'common.User',
                function($scope, $window, $state, $stateParams, $uibModal, UserModel, UserService) {
                    'use strict';

                    var dialogInstance;

                    $scope.$on('$destroy', function() {
                        if (dialogInstance) {
                            dialogInstance.close();
                            dialogInstance = undefined;
                        }
                    });
                    function onUnitSelected(enhet) {
                        //persist unit selection - then close dialog and transition to original destination
                        UserService.setValdVardenhet(enhet, function() {
                            dialogInstance.close();
                            dialogInstance = undefined;
                            $state.go($stateParams.destinationState.name, {}, {
                                location: 'replace'
                            });
                        }, function() {
                            dialogInstance.close();
                            dialogInstance = undefined;
                            //Not much we can do here - redirect to error page
                            $window.location.href = '/error.jsp?reason=login.failed';
                        });

                    }

                    $scope.onUnitSelected = onUnitSelected;

                    function showDialog() {
                        dialogInstance = $uibModal.open({
                            templateUrl: '/app/views/appStartEnhetsval/enhetsval.dialog.html',
                            backdrop: 'static',
                            keyboard: false,
                            windowClass: 'wc-integration-enhet-selector',
                            controller: function($scope, $uibModalInstance, userModel, onUnitSelected) {
                                $scope.user = userModel;
                                $scope.onUnitSelected = onUnitSelected;
                            },
                            resolve: {
                                userModel: function() {
                                    return UserModel.user;
                                },
                                onUnitSelected: function() {
                                    return onUnitSelected;
                                }
                            }
                        });
                    }

                    showDialog();

                } ]);
