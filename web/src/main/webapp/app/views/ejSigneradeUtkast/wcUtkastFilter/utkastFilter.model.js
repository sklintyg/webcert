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
        this.selection = {};
        this.reset();
    }

    UtkastFilterModel.prototype.reset = function() {
        this.filterIsDirty = false;
        this.startFrom = 0;

        this.selection.notified = 'NOTIFIED_ALL'; // 3-state, undefined, true, false
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

        this.selection.status = null;
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

        this.selection.savedFrom = undefined; //Date
        this.selection.savedTo = undefined; //Date
        this.selection.savedBy = undefined; // selected doctors hasId
        this.savedByOptions = this.savedByOptions || [];
        this.selection.orderBy = undefined;
        this.selection.orderAscending = undefined;
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
        query.savedBy = this.selection.savedBy;
        query.notified = convertNotified(this.selection.notified);
        query.status = this.selection.status;

        query.savedFrom = $filter('date')(this.selection.savedFrom, 'yyyy-MM-dd');
        if (this.selection.savedTo) {
            // Date is used as datetime on backend
            var to = moment(this.selection.savedTo);
            to.add(1, 'd');
            query.savedTo = to.format('YYYY-MM-DD');
        }

        query.orderBy = this.selection.orderBy;
        query.orderAscending = this.selection.orderAscending;
        return query;
    };

    UtkastFilterModel.build = function(pageSize) {
        return new UtkastFilterModel(pageSize);
    };

    return UtkastFilterModel;
} ]);
