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

angular.module('webcert').factory('webcert.enhetArendenFilterService',
    [ 'webcert.enhetArendenProxy', 'webcert.enhetArendenModel', 'webcert.enhetArendenFilterModel',
    function(enhetArendenProxy, enhetArendenModel, enhetArendenFilterModel) {
        'use strict';

        function _initLakareList(unitId) {
            enhetArendenFilterModel.viewState.loadingLakare = true;
            var lakareUnitId = unitId === enhetArendenModel.ALL_UNITS ? undefined : unitId;
            enhetArendenProxy.getArendenLakareList(lakareUnitId, function(list) {
                enhetArendenFilterModel.viewState.loadingLakare = false;
                enhetArendenFilterModel.lakareList = list;
                //if (list && (list.length > 0)) {
                    enhetArendenFilterModel.lakareList.unshift(enhetArendenFilterModel.lakareListEmptyChoice);
                    enhetArendenFilterModel.filterForm.lakareSelector = enhetArendenFilterModel.lakareList[0].id;
                //}
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
