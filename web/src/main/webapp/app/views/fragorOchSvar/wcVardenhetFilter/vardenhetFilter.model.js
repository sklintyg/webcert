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

angular.module('webcert').service('webcert.vardenhetFilterModel', [
    '$cookies', 'common.User', 'webcert.enhetArendenModel',
    function($cookies, User, enhetArendenModel) {
        'use strict';

        this.units = null;
        this.selectedUnit = null;

        this.showInactive = false;
        this.ALL_ARENDEN = 'wc-all';

        this.initialize = function(vardEnheter) {
            this.units = vardEnheter;
            this.units = this.units.slice(0, 1)
                .concat(this.units.slice(1, this.units.length).sort(
                    function(a, b) {
                        return (a.namn > b.namn) - (a.namn < b.namn);
                    }));
            this.units.unshift({id: this.ALL_ARENDEN, namn: 'Alla frågor och svar'});

            //initial selection, now handles cases when no enhetsId cookie has been set.
            if (this.units.length > 2 && $cookies.getObject('enhetsId')) {
                this.selectedUnit = selectUnitById(this.units, $cookies.getObject('enhetsId'));
            } else {
                this.selectedUnit = selectFirstUnit(this.units);
            }
        };

        // Local function getting the first care unit's hsa id in the data struct.
        function selectFirstUnit(units) {
            if (typeof units === 'undefined' || units.length === 0) {
                return null;
            } else {
                return units[0];
            }
        }

        function selectUnitById(units, unitName) {
            for (var count = 0; count < units.length; count++) {
                if (units[count].id === unitName) {
                    return units[count];
                }
            }
            return selectFirstUnit(units);
        }

    }]);
