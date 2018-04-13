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

describe('wcUtkastFilterModel', function() {
    'use strict';

    var moduleService;
    var wcUtkastFilterModel;

    beforeEach(function() {

        module('webcert', function($provide) {
            moduleService = jasmine.createSpyObj('common.moduleService', [ 'getModuleName' ]);
            $provide.value('common.moduleService', moduleService);
        });

        inject([ 'webcert.UtkastFilterModel', function(_wcUtkastFilterModel_) {
            wcUtkastFilterModel = _wcUtkastFilterModel_;
        } ]);
    });

    it('should initilize correctly', function() {
        var model = wcUtkastFilterModel.build(100);

        expect(model.pageSize).toEqual(100);

    });

    it('should convertToPayload correctly', function() {
        var model = wcUtkastFilterModel.build(100);
        model.status = 'DRAFT_COMPLETE';
        model.notified = 'NOTIFIED_NO';
        model.savedFrom = new Date('2018-02-03T16:24:00');
        model.savedTo = new Date('2018-02-09T16:24:00');
        var payload = model.convertToPayload();

        expect(payload.pageSize).toEqual(100);
        expect(payload.startFrom).toEqual(0);
        expect(payload.complete).toBe(true); //soon change to status
        expect(payload.notified).toBe(false);
        expect(payload.savedFrom).toEqual('2018-02-03');
        expect(payload.savedTo).toEqual('2018-02-10');
        expect(payload.notified).toBe(false);

    });

    it('should reset correctly', function() {
        var pristine = wcUtkastFilterModel.build(100);
        var model = wcUtkastFilterModel.build(100);
        model.status = 'DRAFT_COMPLETE';
        model.notified = 'NOTIFIED_NO';
        model.savedFrom = new Date();
        model.savedTo = new Date();

        model.reset();

        expect(model).toEqual(pristine);

    });

});
