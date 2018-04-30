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
    function() {
        'use strict';

        // General directive viewstate
        this.viewState = {
            loadingLakare: false
        };

        this.vidarebefordradOptions = [
            {id:'default', label: 'Visa alla'},
            {id:'true', label: 'Vidarebefordrade'},
            {id:'false', label: 'Ej vidarebefordrade'}
        ];

        // Status filter choices
        this.statusList = [
            {
                label: 'Visa alla',
                id: 'ALLA'
            },
            {
                label: 'Alla ej hanterade',
                id: 'ALLA_OHANTERADE'
            },
            {
                label: 'Alla hanterade',
                id: 'HANTERAD'
            },
            {
                label: 'Komplettera',
                id: 'KOMPLETTERING_FRAN_VARDEN'
            },
            {
                label: 'Svara',
                id: 'SVAR_FRAN_VARDEN'
            },
            {
                label: 'Läs inkommet svar',
                id: 'MARKERA_SOM_HANTERAD'
            },
            {
                label: 'Invänta svar',
                id: 'SVAR_FRAN_FK'
            }
        ];

        this.avsandareList = [
            {
                label: 'Visa alla',
                id: 'default'
            },
            {
                label: 'Försäkringskassan',
                id: 'FK'
            },
            {
                label: 'Vårdenheten',
                id: 'WC'
            }
        ];

        // Läkare filter choices
        this.lakareListEmptyChoice = {
            id: undefined,
            label: 'Visa alla'
        };
        this.lakareList = [this.lakareListEmptyChoice];
        var defaultLakareList = angular.copy(this.lakareList);

        // Filter form model
        this.filterForm = {};

        this.reset = function() {
            var statusList = this.statusList;
            this.filterForm = {
                vantarPaSelector: statusList[1].id,
                lakareSelector: defaultLakareList[0].id,
                questionFrom: 'default',
                vidarebefordrad: 'default',
                changedFrom: undefined,
                changedTo: undefined,
                orderBy: '',
                orderAscending: false
            };
        };

        this.reset();
    }]);
