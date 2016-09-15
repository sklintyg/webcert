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

angular.module('webcert').controller('integration.EnhetsvalPageCtrl',
        [ '$scope', '$window', '$uibModal', 'common.UserModel', function($scope, $window, $uibModal, UserModel) {
            'use strict';

            // Construct base destination url
            var baseDestUrl = getParameterByName('destination', $window.location.search) + '&enhet=';

            //Util function that parses destination argument from current url
            function getParameterByName(name, url) {
                var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'), results = regex.exec(url);
                if (!results || !results[2]) {
                    return '';
                }
                return decodeURIComponent(results[2].replace(/\+/g, ' '));
            }

            //When enhet is selected in dialog, redirect window to original destination + &enhet=<selected enhetsid>
            function onUnitSelected(enhet) {
                $window.location.replace(baseDestUrl + enhet.id);
            }

            function showDialog() {
                // We don't handle any results from this dialog - and it cant be closed other than by choosing
                // an enhet which will result in a full page redirect..
                $uibModal.open({
                    templateUrl: '/app/views/integrationEnhetsval/enhetsval.dialog.html',
                    backdrop: 'static',
                    keyboard: false,
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
