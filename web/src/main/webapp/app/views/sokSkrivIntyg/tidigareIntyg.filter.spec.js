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
describe('Filter: TidigareIntygFilter', function() {
    'use strict';

    var _filter;

    var ersatt = {
        status: 'SIGNED',
        relations: {
            latestChildRelations: {
                replacedByIntyg: 'intyg'
            }
        }
    };

    var kompletterat = {
        status: 'SIGNED',
        relations: {
            latestChildRelations: {
                complementedByIntyg: 'intyg'
            }
        }
    };

    var revoked = {
        status: 'CANCELLED'
    };

    var normal = {
        status: 'SIGNED'
    };

    var list = [ ersatt, kompletterat, revoked, normal ];

    beforeEach(angular.mock.module('webcert'));

    beforeEach(inject(function(_TidigareIntygFilterFilter_) {
        _filter = _TidigareIntygFilterFilter_;
    }));

    it('using "all" should return all intyg', function() {
        expect(_filter(list, 'all').length).toBe(4);
        expect(_filter(list, 'all')).toContain(ersatt, kompletterat, revoked, normal);
    });

    it('using "revoked" should not return normal intyg', function() {
        expect(_filter(list, 'revoked').length).toBe(3);
        expect(_filter(list, 'revoked')).toContain(ersatt, kompletterat, revoked);
    });

    it('using "current" should only return normal intyg', function() {
        expect(_filter(list, 'current').length).toBe(1);
        expect(_filter(list, 'current')).toContain(normal);
    });

});
