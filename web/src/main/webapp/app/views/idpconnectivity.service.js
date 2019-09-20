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

angular.module('webcert').factory('idpConnectivityService', ['$http', '$q', '$window',
    'common.MonitoringLogService', 'common.featureService', 'common.UserModel',
    function ($http, $q, $window, MonitoringLogService, featureService, UserModel) {
        'use strict';

        function _checkAndLogConnectivity() {
            if (!featureService.isFeatureActive(featureService.features.IDP_CONNECTIVITY_CHECK)) {return;} //Check if active
            if (!UserModel.user) {return;} //Check if we have user

            if ($window.localStorage){
                var checkTimestamp = $window.localStorage.getItem('last-idp-connectivity-check-date');
                if (checkTimestamp !== null && (new Date() - new Date(checkTimestamp) < 24*60*60*1000)) {return;} // Already checked today on this machine!
            }

            // The url's that we want to test, must not end with a / and should point to a url that serves a favicon.ico
            var testUrls = [
                'https://idp.ineratest.org',
                'https://idp.ineraqa.org',
                'https://idp.inera.se'
            ];

            /*
            CGI global object that will contain the CanReach 'class', takes a url and will create a promise that we run
            at the end of the script. Will contain the status of the url check.
            */
            var CGI = {};
            CGI.CanReach = function (url) {
                var canReach = {
                    promise: null,
                    url: url,
                    canReach: null,

                    testUrl: function () {
                        var _this = this;
                        this.promise = new $q(function (resolve, reject) {
                            _this.url = url;
                            var image = new Image();

                            image.addEventListener('load', function (event) {
                                _this.canReach = true;
                                resolve(true);
                            });
                            image.addEventListener('error', function (event) {
                                _this.canReach = false;
                                resolve(false);
                            });
                            image.src = _this.url + '/favicon.ico?' + Date.now();
                        });
                    }
                };
                canReach.testUrl();

                return canReach;
            };

            var requests = [];
            var promises = [];

            // Create CanReach instances and collect promises based on the testUrls array
            for (var i = 0; i < testUrls.length; i++) {
                var request = new CGI.CanReach(testUrls[i]);
                requests.push(request);
                promises.push(request.promise);
            }

            var ipPromise = $http.get('https://api.ipify.org');
            promises.push(ipPromise);

            var connectivity = [];
            // Execute all promises and act on the result, creates table rows that tells if we are able to connect to the urls.
            $q.all(promises).then(function (response) {
                var i = 0;
                for (; i < requests.length; i++) {
                    connectivity.push({url: requests[i].url, connected: requests[i].canReach});
                }

                var ip = response[i].data;
                MonitoringLogService.idpCheck(ip, JSON.stringify(connectivity));
                if ($window.localStorage) {$window.localStorage.setItem('last-idp-connectivity-check-date', new Date().toString());}
            });
        }

        return {
            checkAndLogConnectivity : _checkAndLogConnectivity
        };
    }]);
