/*
 * Copyright (C) 2017 Inera AB (http://www.inera.se)
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

angular.module('webcert').factory('webcert.enhetArendenListService',
    ['$log', '$filter', '$q',
      'common.enhetArendenCommonService',
      'webcert.enhetArendenProxy', 'webcert.enhetArendenModel', 'webcert.enhetArendenFilterModel', 'webcert.enhetArendenConverterService',
      function($log, $filter, $q,
          enhetArendenCommonService,
          enhetArendenProxy, enhetArendenModel, enhetArendenFilterModel, enhetArendenConverterService) {
        'use strict';

        function _getArenden(startFrom) {

          var deferred = $q.defer();

          var filterQuery = enhetArendenConverterService.convertFormModelToFilterQuery(enhetArendenFilterModel.filterForm,
              enhetArendenModel.enhetId);
          filterQuery.startFrom = startFrom;
          enhetArendenProxy.getArenden(filterQuery, function(successData) {

            var arendenList = successData.results;

            function decorateList(list) {
              angular.forEach(list, function(qa) {
                enhetArendenCommonService.decorateSingleItemMeasure(qa);
              });
            }

            decorateList(arendenList);

            deferred.resolve({
              query: filterQuery,
              totalCount: successData.totalCount,
              arendenList: arendenList
            });

          }, function(errorData) {
            deferred.reject(errorData);
          });

          return deferred.promise;
        }

        // Return public API for the service
        return {
          getArenden: _getArenden
        };
      }]);
