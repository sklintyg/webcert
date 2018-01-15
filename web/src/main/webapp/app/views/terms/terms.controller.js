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
angular.module('webcert').controller('webcert.TermsCtrl', ['$log', '$rootScope', '$scope', '$window',
        '$sanitize', '$state', '$location',
        'common.AvtalProxy', 'common.UserModel',
        function($log, $rootScope, $scope, $window, $sanitize, $state, $location, AvtalProxy, UserModel) {
            'use strict';
            $scope.terms = {doneLoading:false, avtal:false};

            UserModel.termsAccepted = false;
            UserModel.transitioning = false;

            // load the avtal
            AvtalProxy.getLatestAvtal(function(avtalModel) {
                $scope.terms.avtal = avtalModel;
                $scope.terms.doneLoading = true;
            }, function() {

                $scope.terms.doneLoading = true;
            });

            function endsWith(str, suffix) {
                return str.indexOf(suffix, str.length - suffix.length) !== -1;
            }

            $scope.modalOptions = {
                terms : $scope.terms,
                modalBodyTemplateUrl : '/app/views/terms/terms.body.html',
                titleId: 'avtal.title.text',
                extraDlgClass: undefined,
                width: '600px',
                height: '90%',
                maxWidth: '600px',
                maxHeight: undefined,
                minWidth: undefined,
                minHeight: undefined,
                contentHeight: '100%',
                contentOverflowY: undefined,
                contentMinHeight: undefined,
                bodyOverflowY: 'scroll',
                buttons: [
                    {
                        name: 'approve',
                        clickFn: function() {
                            AvtalProxy.approve(
                                function() {
                                    UserModel.termsAccepted = true;
                                    $scope.modalOptions.modalInstance.dismiss('cancel');
                                    $state.transitionTo('webcert.create-index');
                                },
                                function() {
                                    UserModel.termsAccepted = false;
                                    $window.location = '/web/error';
                                }
                            );
                        },
                        text: 'avtal.approve.label',
                        id: 'acceptTermsBtn',
                        className: 'btn-success'
                    },
                    {
                        name: 'print',
                        clickFn: function() {
                            var head = '<!DOCTYPE html><html>' +
                                '<head>' +
                                '<title>Webcert - Användarvillkor</title>' +
                                '</head>';

                            var body = '<body onload="window.print()">' +
                                '<img class="pull-left" style="padding-bottom: 20px" src="/img/webcert_black.png" />' +
                                '<p style="clear:left;padding-bottom:50px;color:#535353">' +
                                '<span style="">Version: ' +
                                $scope.modalOptions.terms.avtal.avtalVersion + '</span><br>' +
                                '<span>Datum: ' + $scope.modalOptions.terms.avtal.versionDatum + '</span></p>' +
                                '<h1 style="color: black;font-size: 2em">Användarvillkor för Webcert</h1>' +
                                '<p style="clear:left;padding-bottom: 10px">' + $scope.modalOptions.terms.avtal.avtalText + '</p>' +
                                '<p style="clear:left;color:#535353;padding-top:50px">' + $location.absUrl() + '</p>' +
                                '</body>';

                            var footer = '</html>';

                            var template = head + body + footer;

                            var popupWin = null;
                            if (navigator.userAgent.toLowerCase().indexOf('chrome') > -1) {
                                popupWin = window.open('', '_blank',
                                    'width=400,scrollbars=no,menubar=no,toolbar=no,location=no,status=no,titlebar=no');
                                popupWin.window.focus();
                                popupWin.document.write(template);
                                setTimeout(function() {
                                    popupWin.close();
                                }, 100);
                                popupWin.onbeforeunload = function(/*event*/) {
                                    popupWin.close();
                                };
                                popupWin.onabort = function(/*event*/) {
                                    popupWin.document.close();
                                    popupWin.close();
                                };
                            } else {
                                popupWin = window.open('', '_blank',
                                    'width=800,scrollbars=yes,menubar=no,toolbar=no,location=no,status=no,titlebar=no');
                                popupWin.document.open();
                                popupWin.document.write(template);
                            }
                            popupWin.document.close();

                            return true;
                        },
                        text: 'avtal.print.label',
                        id: 'printTermsBtn'
                    },
                    {
                        name: 'logout',
                        clickFn: function() {
                            if (endsWith(UserModel.user.authenticationScheme, ':fake')) {
                                $window.location = '/logout';
                            } else {
                                iid_Invoke('Logout'); // jshint ignore:line
                                $window.location = '/saml/logout/';
                            }
                        },
                        text: 'avtal.logout.label',
                        id: 'logoutTermsBtn'
                    }
                ],
                showClose: false
            };
        }]
);
