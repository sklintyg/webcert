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

angular.module('webcert').factory('webcert.enhetArendenConverterService',
    ['$filter', 'webcert.enhetArendenModel', 'common.UserModel',
      function($filter, enhetArendenModel, UserModel) {
        'use strict';

        // Filter query request model. Actual model expected by backend proxy
        var defaultFilterQuery = {
          enhetId: undefined,

          questionFromFK: false,
          questionFromWC: false,
          hsaId: undefined, // läkare
          vidarebefordrad: undefined, // 3-state

          changedFrom: undefined,
          changedTo: undefined,

          vantarPa: undefined,

          orderBy: 'receivedDate',
          orderAscending: false,

          patientPersonId: undefined
        };

        function _convertFormModelToFilterQuery(filterForm, enhetId, pageSize) {
          /*jshint maxcomplexity:false */

          // Converts view values and sets them on a copy of query object
          var filterQuery = angular.copy(defaultFilterQuery);

          if (enhetId !== enhetArendenModel.ALL_UNITS) {
            filterQuery.enhetId = enhetId;
          }

          filterQuery.vantarPa = filterForm.vantarPaSelector;

          if (filterForm.lakareSelector) {
            filterQuery.hsaId = filterForm.lakareSelector;
          }

          if (filterForm.changedFrom) {
            filterQuery.changedFrom = $filter('date')(filterForm.changedFrom, 'yyyy-MM-dd');
          }

          if (filterForm.changedTo) {
            filterQuery.changedTo = $filter('date')(filterForm.changedTo, 'yyyy-MM-dd');
          }

          if (pageSize) {
            filterQuery.pageSize = pageSize;
          }

          if (filterForm.startFrom) {
            filterQuery.startFrom = filterForm.startFrom;
          }

          if (filterForm.questionFrom === 'FK') {
            filterQuery.questionFromFK = true;
            filterQuery.questionFromWC = false;
          } else if (filterForm.questionFrom === 'WC') {
            filterQuery.questionFromFK = false;
            filterQuery.questionFromWC = true;
          } else {
            filterQuery.questionFromFK = false;
            filterQuery.questionFromWC = false;
          }

          if (filterForm.vidarebefordrad !== 'default') {
            filterQuery.vidarebefordrad = filterForm.vidarebefordrad;
          }

          if (filterForm.orderAscending) {
            filterQuery.orderAscending = filterForm.orderAscending;
          }

          if (filterForm.orderBy) {
            filterQuery.orderBy = filterForm.orderBy;
          }

          if (filterForm.patientPersonId) {
            filterQuery.patientPersonId = filterForm.patientPersonId.replace('-', '');
          }

          return filterQuery;
        }

        // Return public API for the service
        return {
          convertFormModelToFilterQuery: _convertFormModelToFilterQuery
        };
      }]);
