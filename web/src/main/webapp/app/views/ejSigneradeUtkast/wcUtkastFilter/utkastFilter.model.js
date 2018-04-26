/*
 * Copyright (C) 2018 Inera AB (http://www.inera.se)
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

angular.module('webcert').factory('webcert.UtkastFilterModel', [ '$filter', function($filter) {
    'use strict';

    /**
     * Constructor
     */
    function UtkastFilterModel(pageSize) {
        this.pageSize = pageSize;
        this.reset();
    }

    UtkastFilterModel.prototype.reset = function() {
        this.startFrom = 0;

        this.notified = 'NOTIFIED_ALL'; // 3-state, undefined, true, false
        this.notifiedOptions = [ {
            id: 'NOTIFIED_ALL',
            label: 'Visa alla'
        }, {
            id: 'NOTIFIED_YES',
            label: 'Vidarebefordrade'
        }, {
            id: 'NOTIFIED_NO',
            label: 'Ej vidarebefordrade'
        } ];

        this.status = null;
        this.statusOptions = [ {
            id: null,
            label: 'Visa alla'
        }, {
            id: 'DRAFT_INCOMPLETE',
            label: 'Uppgifter saknas'
        }, {
            id: 'DRAFT_COMPLETE',
            label: 'Kan signeras'
        }, {
            id: 'DRAFT_LOCKED',
            label: 'Låsta'
        } ];

        this.savedFrom = undefined; //Date
        this.savedTo = undefined; //Date
        this.savedBy = undefined; // selected doctors hasId
        this.savedByOptions = this.savedByOptions || [];
        this.orderBy = undefined;
        this.orderAscending = undefined;
    };

    UtkastFilterModel.prototype.convertToPayload = function() {

        function convertNotified(value) {
            switch (value) {
            case 'NOTIFIED_YES':
                return true;
            case 'NOTIFIED_NO':
                return false;
            default:
                return undefined;
            }
        }

        var query = {
            startFrom: this.startFrom,
            pageSize: this.pageSize
        };
        query.savedBy = this.savedBy;
        query.notified = convertNotified(this.notified);
        query.status = this.status;

        query.savedFrom = $filter('date')(this.savedFrom, 'yyyy-MM-dd');
        if (this.savedTo) {
            // Date is used as datetime on backend
            var to = moment(this.savedTo);
            to.add(1, 'd');
            query.savedTo = to.format('YYYY-MM-DD');
        }

        query.orderBy = this.orderBy;
        query.orderAscending = this.orderAscending;
        return query;
    };

    UtkastFilterModel.build = function(pageSize) {
        return new UtkastFilterModel(pageSize);
    };

    return UtkastFilterModel;
} ]);
