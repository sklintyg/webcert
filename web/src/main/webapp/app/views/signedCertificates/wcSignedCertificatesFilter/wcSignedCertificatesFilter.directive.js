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

angular.module('webcert').directive('wcSignedCertificatesFilter', [
  '$rootScope', 'common.messageService', function($rootScope, messageService) {
    'use strict';

    return {
      restrict: 'E',
      transclude: false,
      replace: false,
      scope: {
        filterModel: '=',
        listModel: '=',
        onSearch: '&'
      },
      templateUrl: '/app/views/signedCertificates/wcSignedCertificatesFilter/wcSignedCertificatesFilter.directive.html',
      controller: function($scope) {

        $scope.messageService = messageService;
        $scope.showDateFromErrors = false;
        $scope.showDateToErrors = false;

        $scope.setLimitsForDateInput = function() {
          var todaysDate = moment($scope.maxDate);
          $scope.maxDate = todaysDate.format('YYYY-MM-DD');
          $scope.minDate = todaysDate.add(-3, 'months').format('YYYY-MM-DD');
        };

        $scope.showDateError = function () {
          return ($scope.showDateFromErrors || $scope.showDateToErrors) && $scope.filterForm.$invalid && !$scope.filterForm.$pristine;
        };

        $scope.setShowDateFromVisible = function() {
          $scope.showDateFromErrors = !!$scope.filterForm['filter-signeddate-from'].$viewValue;
        };

        $scope.setShowDateToVisible = function() {
          $scope.showDateToErrors = !!$scope.filterForm['filter-signeddate-to'].$viewValue;
        };

        $scope.reset = function() {
          $scope.showDateFromErrors = false;
          $scope.showDateToErrors = false;
          $scope.listModel.reset();
          $scope.filterModel.reset();
          $scope.filterModel.pageSize = $scope.listModel.limit;
          $rootScope.$broadcast('signedCertificateList.requestListUpdate', {startFrom: 0, reset: true}); //arguments are not needed since startFrom gets reset in model??????
        };


        $scope.setLimitsForDateInput();
      }
    };
  }]);

