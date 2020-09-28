/*
 * Copyright (C) 2020 Inera AB (http://www.inera.se)
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
angular.module('rhsIndexApp', []);

var testsignatur = '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';

function guid() {
  function s4() {
    return Math.floor((1 + Math.random()) * 0x10000)
    .toString(16)
    .substring(1);
  }

  return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
      s4() + '-' + s4() + s4() + s4();
}

function isDefined(value) {
  return value !== null && typeof value !== 'undefined';
}

function isEmpty(value) {
  return value === null || typeof value === 'undefined' || value === '';
}

function returnJoinedArrayOrNull(value) {
  return value !== null && value !== undefined ? value.join(', ') : null;
}

function valueOrNull(value) {
  return value !== null && value !== undefined ? value : null;
}

function timeInMillis() {
  return new Date().getTime();
}

angular.module('rhsIndexApp')
.controller('IndexController', ['$scope', '$http', function($scope, $http) {
  'use strict';

  $scope.personId = '19121212-1212';
  $scope.selectedEnhet = '';
  $scope.q = {};
  $scope.q.intygsId = '';
  $scope.utkast = null;

  $scope.isNetID = false;
  $scope.isBankID = false;

  $scope.ongoingSignatures = [];
  $scope.ongoingGrpSignatures = [];

  $scope.pollHandle = null;
  $scope.grpPollHandle = null;

  $scope.signingStarted = false;

  function cleanup() {
    clearInterval($scope.pollHandle);
    clearInterval($scope.grpPollHandle);
    $scope.isNetID = false;
    $scope.isBankID = false;
    $scope.ticketId = null;
    $scope.pollHandle = null;
    $scope.grpPollHandle = null;
    $scope.q = {};
    $scope.signingStarted = false;
  }

  $scope.loadOngoingGrpSignatures = function() {
    $http({
      method: 'GET',
      url: '/services/grp-api/statuses'
    })
    .then(function successCallback(response) {
      // Populate table
      $scope.ongoingGrpSignatures = response.data;
    }, function errorCallback(response) {
      console.log('Error during GRP load of ongoing signatures. Msg: ' +
          JSON.stringify(response.data));
    });
  };

   $scope.signeraAvbrytGrp = function(transactionId) {
    $http.put('/services/grp-api/cancel/' + transactionId, null, null).then(
        function(response) {
          // Success callback
          console.log('signeraAvbrytGrp success');
          cleanup();
        },
        function(response) {
          // Error callback
          console.log('signeraAvbrytGrp error: ' + JSON.stringify(response.data));
          cleanup();
        })

  };

  $scope.setGrpStatus = function(orderRef, status) {
    // Här måste vi använda ett testbarhets-API för att låtsas vara klara med GRP-signeringen. Kanske använda
    // GRP-stubben?
    // /services/grp-api
    $http.put('/services/grp-api/status', {orderRef: orderRef, status: status}, null).then(
        function(response) {
          // Success
          console.log('Set state to ' + status + ' successful: ' + JSON.stringify(response));
          cleanup();
        },
        function(response) {
          // Failure
          cleanup();
        }
    )
  };

  $scope.bekraftaSigneringGrp = function() {
    // Här måste vi använda ett testbarhets-API för att låtsas vara klara med GRP-signeringen. Kanske använda
    // GRP-stubben?
    // /services/grp-api
    if (!isDefined($scope.orderRef)) {
      alert('Wait until there is an orderRef!');
      return;
    }
    $http.put('/services/grp-api/status', {orderRef: $scope.orderRef, status: 'COMPLETE'}, null).then(
        function(response) {
          // Success
          console.log("Signing successful: " + JSON.stringify(response));
          cleanup();
        },
        function(response) {
          // Failure
          console.log("Signing NOT successful: " + JSON.stringify(response));
        }
    )
  };

    $scope.signeraAvbryt = function() {
    cleanup();
  };

  $scope.openForm = function(intyg) {
    $scope.q.intygsId = intyg.intygsId;
    $scope.utkast = intyg;
  };

  $scope.loadUtkastReadyToSign = function() {
    $http({
      method: 'GET',
      url: '/testability/intyg/' + $scope.selectedEnhet + '/drafts?cachekiller=' + timeInMillis()
    }).then(function successCallback(response) {
      $scope.resultat = '';
      $scope.data = response.data;
    });
  };

  $scope.loadUnits = function() {
    $http({
      method: 'GET',
      url: '/testability/intyg/signingunits?cachekiller=' + timeInMillis()
    }).then(function successCallback(response) {
      $scope.units = response.data;
    });
  };

  $scope.loadUnits();
  setInterval(function() {
    $scope.loadOngoingGrpSignatures();
  }, 3000);

}]);
