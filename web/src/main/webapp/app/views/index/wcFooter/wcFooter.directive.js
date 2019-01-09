/*
 * Copyright (C) 2019 Inera AB (http://www.inera.se)
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

angular.module('webcert').directive('wcFooter', ['common.dialogService',
    function(dialogService) {
    'use strict';
        return {
            restrict: 'E',
            templateUrl: '/app/views/index/wcFooter/wcFooter.directive.html',
            scope: {
            },
            link: function(scope) {
                var dialogInstance;

                scope.showInfoAboutEleg = function() {
                    dialogInstance = dialogService.showDialog({
                        dialogId: 'login-metoder-eleg-help',
                        templateUrl: 'app/views/index/wcFooter/loginMetoderElegHelp.html',
                        button1click: function() {
                            dialogInstance.close();
                        },
                        autoClose: false,
                        size: 'lg'
                    });
                };

                scope.showInfoAboutSITHS = function() {
                    dialogInstance = dialogService.showDialog({
                        dialogId: 'login-metoder-siths-help',
                        templateUrl: 'app/views/index/wcFooter/loginMetoderSithsHelp.html',
                        button1click: function() {
                            dialogInstance.close();
                        },
                        autoClose: false,
                        size: 'lg'
                    });
                };

                scope.openAboutCookies = function () {
                    dialogInstance = dialogService.showDialog({
                        dialogId: 'about-cookies-modal',
                        templateUrl: 'app/views/index/wcFooter/aboutCookies.html',
                        button1click: function() {
                            dialogInstance.close();
                        },
                        autoClose: false,
                        size: 'lg'
                    });
                };
            }
        };
} ]);
