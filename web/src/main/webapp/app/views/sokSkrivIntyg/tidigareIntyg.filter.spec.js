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
