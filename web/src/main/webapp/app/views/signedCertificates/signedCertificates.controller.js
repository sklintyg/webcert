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

/*
* Controller for logic related to listing unsigned certs
*/
angular.module('webcert').controller('webcert.SignedCertificatesCtrl',
    ['$log', '$scope', '$timeout', '$rootScope', '$window', 'common.User', 'webcert.SignedCertificatesFilterModel',
      'webcert.SignedCertificatesListModel', 'webcert.SignedCertificatesProxy', 'common.UserModel', 'common.featureService',
      function($log, $scope, $timeout, $rootScope, $window, User, SignedCertificatesFilterModel, SignedCertificatesListModel,
          SignedCertificatesProxy, UserModel, featureService) {
        'use strict';

        $scope.orderByProperty = function(property, ascending) {
          $scope.filterModel.filterForm.orderBy = property;
          $scope.filterModel.filterForm.orderAscending = ascending;
          getCertificates(null, { startFrom: $scope.filterModel.startFrom });
        };

        $scope.showDropDown = function() {
          return $scope.listModel.totalCount > $scope.listModel.DEFAULT_PAGE_SIZE && $scope.viewLoaded && !$scope.listModel.runningQuery;
        };

        $scope.showPageNumbers = function() {
          return !$scope.listModel.runningQuery && $scope.listModel.totalCount > 0 && $scope.viewLoaded &&
              ($scope.listModel.pagesList.length > 1 || $scope.listModel.chosenPageList > 1) && !$scope.listModel.gettingPage;
        };

        $scope.showNumberOfHits = function() {
          return !$scope.listModel.runningQuery && $scope.listModel.totalCount > 0 && $scope.viewLoaded;
        };

        $scope.init = function() {
          $scope.chosenUnit = User.getValdVardenhet();
          $scope.filterModel = SignedCertificatesFilterModel.build();
          $scope.listModel = SignedCertificatesListModel.build();
          $scope.viewLoaded = false;
          $scope.filterModel.pageSize = $scope.listModel.DEFAULT_PAGE_SIZE;
          getCertificates(null, { startFrom: 0, start: true });
        };

        $scope.onSearch = function() {
          getCertificates(null, { startFrom: 0, search: true });
        };

        $scope.updateValues = function(data) {
          $rootScope.$broadcast('wcListDropdown.getLimits');
          $rootScope.$broadcast('wcListPageNumbers.getPages');
          $scope.listModel.startPoint = data.startFrom + 1;
          $scope.listModel.endPoint = data.startFrom + $scope.listModel.certificates.length;
          $scope.listModel.runningQuery = false;
          $scope.listModel.activeErrorMessageKey = null;
        };

        function getCertificates(event, data) {
          if(data.search) {
            $scope.listModel.resetListPageNumberValues();
          }
          $scope.filterModel.startFrom = data.startFrom;
          var filterQuery = $scope.filterModel.convertToPayload();
          $scope.listModel.runningQuery = true;
          SignedCertificatesProxy.getCertificates(filterQuery, function(successData) {
            if(successData.certificates !== undefined && !successData.errorFromIT) {
              if (data.start || $scope.listModel.nbrOfUnfilteredCertificates === undefined) {
                $scope.listModel.nbrOfUnfilteredCertificates = successData.totalCount;
                $scope.viewLoaded = true;
              }
              $scope.listModel.certificates = successData.certificates;
              $scope.listModel.totalCount = successData.totalCount;
              $scope.updateValues(data);
            } else {
              $scope.updateErrorValues();
            }
          }, function() {
            $scope.updateErrorValues();
          });
        }

        if(!UserModel.user.isLakareOrPrivat || UserModel.isDjupintegration() ||
            !featureService.isFeatureActive(featureService.features.SIGNED_CERTIFICATES_LIST)) {
          $window.location.href = '/error.jsp?reason=auth-exception';
        } else {
          $scope.init();
        }

        $scope.updateErrorValues = function() {
          $scope.listModel.activeErrorMessageKey = 'info.query.error';
          $scope.listModel.runningQuery = false;
          $scope.viewLoaded = true;
        };

        $scope.$on($scope.listModel.LIST_NAME + '.requestListUpdate', getCertificates);
      }]);

