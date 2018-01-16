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

angular.module('webcert').service('webcert.enhetArendenFilterModel', [
    'webcert.enhetArendenModel',
    function(enhetArendenModel) {
        'use strict';

        // General directive viewstate
        this.viewState = {
            filteredYet: false,
            filterFormCollapsed: true,
            loadingLakare: false
        };

        // Status filter choices
        this.statusList = [
            {
                label: 'Visa alla',
                value: 'ALLA'
            },
            {
                label: 'Visa alla ej hanterade',
                value: 'ALLA_OHANTERADE'
            },
            {
                label: 'Markera som hanterad',
                value: 'MARKERA_SOM_HANTERAD'
            },
            {
                label: 'Komplettera',
                value: 'KOMPLETTERING_FRAN_VARDEN'
            },
            {
                label: 'Svara',
                value: 'SVAR_FRAN_VARDEN'
            },
            {
                label: 'Invänta svar från Försäkringskassan',
                value: 'SVAR_FRAN_FK'
            },
            {
                label: 'Inget',
                value: 'HANTERAD'
            }
        ];

        // Läkare filter choices
        this.lakareListEmptyChoice = {
            hsaId: undefined,
            name: 'Alla'
        };
        this.lakareList = [this.lakareListEmptyChoice];

        // Filter form model
        this.filterForm = {};

        this.reset = function() {
            var statusList = this.statusList;
            var lakareList = this.lakareList;
            this.filterForm = {
                vantarPaSelector: statusList[1],
                lakareSelector: lakareList[0],
                questionFrom: 'default',
                vidarebefordrad: 'default',
                changedFrom: undefined,
                changedTo: undefined
            };
        };

        this.reset();
    }]);
