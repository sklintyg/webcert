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

'use strict';

module.exports = {
    ts: {
        ICD10: {
            a00: 'A00'
        },
        korkortstyper: {
            am: 'AM',
            a1: 'A1',
            a2: 'A2',
            a: 'A',
            b: 'B',
            be: 'BE',
            traktor: 'Traktor',
            c1: 'C1',
            c: 'C',
            ce: 'CE',
            d1: 'D1',
            d1e: 'D1E',
            d: 'D',
            de: 'DE',
            taxi: 'Taxi'
        },
        identitetStyrktGenom: {
            idkort: 'ID-kort',
            foretagstjanstekort: 'Företagskort eller tjänstekort',
            svensktkorkort: 'Svenskt körkort',
            personligkannedom: 'Personlig kännedom',
            forsakranenligt18kap4: 'Försäkran enligt 18 kap. 4§',
            pass: 'Pass'
        },
        diabetes: {
            typ: {
                typ1: 'Typ 1',
                typ2: 'Typ 2'
            },
            behandling: {
                endastkost: 'Endast kost',
                tabletter: 'Tabletter',
                insulin: 'Insulin'
            }
        }
    }
};
