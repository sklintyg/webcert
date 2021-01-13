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

angular.module('webcert').factory('webcert.enhetArendenFilterService',
    ['webcert.enhetArendenProxy', 'webcert.enhetArendenModel', 'webcert.enhetArendenFilterModel', 'common.UserModel',
      function(enhetArendenProxy, enhetArendenModel, enhetArendenFilterModel, UserModel) {
        'use strict';

        function _initLakareList(unitId) {
          enhetArendenFilterModel.viewState.loadingLakare = true;
          var lakareUnitId = unitId === enhetArendenModel.ALL_UNITS ? undefined : unitId;
          enhetArendenProxy.getArendenLakareList(lakareUnitId, function(list) {
            enhetArendenFilterModel.viewState.loadingLakare = false;
            enhetArendenFilterModel.lakareList = list;
            enhetArendenFilterModel.lakareList.unshift(enhetArendenFilterModel.lakareListEmptyChoice);
            if (!UserModel.isDjupintegration() && !UserModel.isVardAdministrator()) {

              var userLakare = {
                id: UserModel.user.hsaId, label: UserModel.user.namn
              };

              var inList = false;
              enhetArendenFilterModel.lakareList.forEach(function(lakare) {
                if (lakare.id === userLakare.id) {
                  inList = true;
                }
              });

              if (!inList) {
                enhetArendenFilterModel.lakareList.push(userLakare);
              }
              enhetArendenFilterModel.filterForm.lakareSelector = userLakare.id;
            } else {
              enhetArendenFilterModel.filterForm.lakareSelector = enhetArendenFilterModel.lakareList[0].id;
            }
            enhetArendenFilterModel.defaultLakareSelector = enhetArendenFilterModel.filterForm.lakareSelector;

          }, function() {
            enhetArendenFilterModel.viewState.loadingLakare = false;
            enhetArendenFilterModel.lakareList = [];
            enhetArendenFilterModel.lakareList.push({
              hsaId: undefined,
              name: '<Kunde inte hämta lista>'
            });
          });
        }

        // Return public API for the service
        return {
          initLakareList: _initLakareList
        };
      }]);
