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

angular.module('webcert').filter('TidigareIntygFilter',
    function() {
        'use strict';

        function isErsatt(intyg) {
            if (typeof intyg.relations !== 'undefined' &&
                typeof intyg.relations.latestChildRelations !== 'undefined' &&
                typeof intyg.relations.latestChildRelations.replacedByIntyg !== 'undefined') {
                return true;
            }
            return false;
        }

        function isKompletterad(intyg) {
            if (typeof intyg.relations !== 'undefined' &&
                typeof intyg.relations.latestChildRelations !== 'undefined' &&
                typeof intyg.relations.latestChildRelations.complementedByIntyg !== 'undefined') {
                return true;
            }
            return false;
        }

        return function(intygList, intygToInclude) {
            var result = [];

            switch(intygToInclude) {
            case 'revoked':
                angular.forEach(intygList, function(intyg) {
                    if (intyg.status === 'CANCELLED' || intyg.status === 'DRAFT_LOCKED' || intyg.status === 'DRAFT_LOCKED_CANCELLED' ||
                        isErsatt(intyg) || isKompletterad(intyg)) {
                        result.push(intyg);
                    }
                });
                break;
            case 'current':
                angular.forEach(intygList, function(intyg) {
                    if (intyg.status !== 'CANCELLED' && intyg.status !== 'DRAFT_LOCKED' && intyg.status !== 'DRAFT_LOCKED_CANCELLED' &&
                        !isErsatt(intyg) && !isKompletterad(intyg)) {
                        result.push(intyg);
                    }
                });
                break;
            case 'all':
                result = intygList;
                break;
            }
            
            return result;
        };
    });
