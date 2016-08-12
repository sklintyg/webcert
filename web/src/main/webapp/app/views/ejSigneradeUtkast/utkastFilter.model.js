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

angular.module('webcert').factory('webcert.UtkastFilterModel',
    ['$filter', function($filter) {
        'use strict';

        /**
         * Constructor
         */
        function UtkastFilterModel() {
            this.reset();
        }

        UtkastFilterModel.prototype.reset = function() {
            this.notified = undefined; // 3-state, undefined, true, false
            this.complete = undefined; // 3-state, undefined, true, false
            this.savedFrom = undefined;
            this.savedTo = undefined;
            this.savedBy = undefined; // selected doctor hasId
        };


        UtkastFilterModel.prototype.convertToPayload = function() {
            var converted = angular.copy(this);
            converted.savedFrom = $filter('date')(converted.savedFrom, 'yyyy-MM-dd');
            if (converted.savedTo) {
                // Date is used as datetime on backend
                var to = moment(converted.savedTo);
                to.add(1, 'd');
                converted.savedTo = to.format('YYYY-MM-DD');
            }
            return converted;
        }


        UtkastFilterModel.build = function() {
            return new UtkastFilterModel();
        };

        return UtkastFilterModel;
    }
    ]);
