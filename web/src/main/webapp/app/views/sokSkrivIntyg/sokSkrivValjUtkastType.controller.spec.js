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

describe('ChooseCertTypeCtrl', function() {
    'use strict';

    var $controller;
    var UtkastProxy;
    var IntygProxy;
    var $scope;
    var $location;

    beforeEach(function(){

        module('webcert', function($provide) {
            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);

            UtkastProxy = jasmine.createSpyObj('webcert.UtkastProxy', [ 'getUtkastTypes', 'initCopyDialog' ]);
            $provide.value('webcert.UtkastProxy', UtkastProxy);

            IntygProxy = jasmine.createSpyObj('webcert.IntygProxy', [ 'getIntygForPatient' ]);
            $provide.value('webcert.IntygProxy', IntygProxy);

            $provide.value('common.IntygCopyRequestModel', {});
            $provide.value('common.IntygService', {});
            $provide.value('common.PatientModel', {});
            $provide.value('common.PatientProxy', {});
        });

        inject(function($rootScope, _$location_, _$controller_) {
            $scope = $rootScope.$new();
            $location = _$location_;
            $controller = _$controller_;
        });
    });

    describe('openIntyg', function() {

        var controller;
        var cert;

        beforeEach(function() {
            controller = $controller('webcert.ChooseCertTypeCtrl', { $scope: $scope });
            cert = {
                source: 'WC',
                intygType: 'fk7263',
                intygId: 'abc123'
            };
        });

        it('should set utkast path', function() {
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/' + cert.intygType + '/edit/' + cert.intygId);
        });

        it('should set signed path', function() {
            cert.source = 'IT';
            $scope.openIntyg(cert);
            expect($location.path()).toBe('/intyg/' + cert.intygType + '/' + cert.intygId);
        });
    }) ;
});
