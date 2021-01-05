/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
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

angular.module('webcert').directive('wcSignedCertificatesList',
    ['common.UtkastNotifyService', 'common.IntygHelper', 'common.moduleService',
      function(utkastNotifyService, IntygHelper, moduleService) {
        'use strict';

        return {
          restrict: 'E',
          replace: false,
          scope: {
            certificateList: '=',
            sortingProperty: '=',
            sortingAscending: '=',
            onOrder: '&',
            listModel:'='
          },
          templateUrl: '/app/views/signedCertificates/wcSignedCertificatesList/wcSignedCertificatesList.directive.html',
          controller: function($scope) {
            $scope.openIntyg = function(certificate) {
              IntygHelper.goToIntyg(certificate.certificateType, certificate.certificateTypeVersion, certificate.certificateId);
            };

            $scope.orderByProperty = function(property) {
              var ascending = true;
              if(property === 'signingDate'){
                ascending = false;
              }
              if ($scope.sortingProperty === property) {
                ascending = !$scope.sortingAscending;
              }
              $scope.sortingProperty = property;
              $scope.sortingAscending = ascending;
              $scope.onOrder({property: property, ascending: ascending});
            };

            $scope.getTypeName = function(intygsType) {
              return moduleService.getModuleName(intygsType);
            };

            $scope.getStatus = function(certificate) {
              if(certificate.sent) {
                return 'sent';
              } else {
                return 'notsent';
              }
            };

          }
        };
      }
    ]
);
