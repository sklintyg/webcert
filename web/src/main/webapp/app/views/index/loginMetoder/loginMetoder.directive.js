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

angular.module('webcert').directive('wcLoginMetoder', ['$window', '$http', 'moduleConfig',
    function($window, $http, moduleConfig) {
        'use strict';

        return {
            restrict: 'E',
            scope: {
            },
            link: function(scope) {

                scope.showELegWarning = (function() {
                    var re = /(?:Trident\/\d+)|(?:MSIE \d+)/;
                    var userAgent = $window.navigator.userAgent;
                    return !re.test(userAgent);
                }());

                scope.loginMethods = [{
                    url:'/saml/login/alias/defaultAlias?idp=' + moduleConfig.SAKERHETSTJANST_IDP_URL,
                    name:'SITHS-kort'
                },{
                    url:'/saml/login/alias/defaultAlias?idp=' + moduleConfig.CGI_FUNKTIONSTJANSTER_IDP_URL,
                    name:'E-legitimation'
                }];

                function loadIntygTypes() {
                    scope.intygTypes = [];
                    $http.get('/api/modules/active').then(function(response) {
                        scope.intygTypes = response.data;
                    });
                }
                loadIntygTypes();
            },
            templateUrl: '/app/views/index/loginMetoder/loginMetoder.directive.html'
        };
    }]);
