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

//Register module
angular.module('rhsIndexApp', [

]);


angular.module('rhsIndexApp')
    .controller('IndexController', ['$scope', '$http', '$filter', function($scope, $http, $filter) {
        'use strict';

        $scope.pnr = '191212121212';
        $scope.q = {
            intygsId: '',
            skickatTidpunkt: '2016-07-13T17:23.00',
            skickatAv: 'FKASSA',
            amne: 'KOMPLT',
            meddelande: 'Detta är en vanlig frågetext ...',
            komplettering: {
                text: 'Detta är kompletteringstexten...',
                frageId: '6'
            },
            meddelandeNr: 1
        };
        $scope.loadIntyg = function() {
            console.log("Loading intyg for " + $scope.pnr);
            $http({
                method: 'GET',
                url: '/api/intyg/person/' + $scope.pnr
            }).then(function successCallback(response) {
                $scope.data = response.data;
                console.log(response.data);
            })
        };

        $scope.openForm = function(intyg) {
            console.log("Opening form for " + intyg.intygId);
            $scope.intyg = intyg;
            $scope.q.intygsId = intyg.intygId;
          //  $scope.q.skickatTidpunkt = $filter('date')(1288323623006, 'yyyy-MM-dd HH:mm:ss Z');
        }

        $scope.sendQuestion = function(q) {
            console.log("Sending question to backend: " + q.intygsId);
        }

    }]);



