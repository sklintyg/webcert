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

describe('wcVardenhetFilter', function() {
    'use strict';

    var $rootScope;
    var $scope;
    var $compile;
    var element;
    var User;
    var vardenhetFilterModel;

    beforeEach(function() {

        module('htmlTemplates');
        module('webcertTest');
        module('webcert', ['$provide', function($provide) {
            $provide.value('common.User', jasmine.createSpyObj('common.User', [ 'getValdVardenhet', 'getValdVardgivare', 'getVardenhetFilterList' ]));
            $provide.value('common.statService', jasmine.createSpyObj('common.statService', [ 'refreshStat', 'getLatestData' ]));
        }]);

        inject(['$rootScope', '$compile', 'common.User', 'webcert.vardenhetFilterModel',
            function(_$rootScope_, _$compile_, _User_, _vardenhetFilterModel_) {

                $rootScope = _$rootScope_;
                $compile = _$compile_;
                User = _User_;
                vardenhetFilterModel = _vardenhetFilterModel_;

                $scope = $rootScope.$new();
                element = $compile('<wc-vardenhet-filter></wc-vardenhet-filter>')($scope);
                $scope.$digest();
                $scope = element.isolateScope();

            }]);
    });

    describe('updateStats', function() {

        it('should update units when statService.stat-update message is received', function() {

            vardenhetFilterModel.units = [
                { id: 'wc-all'},
                { id: '2'}
            ];
            $scope.$broadcast('statService.stat-update', {fragaSvarValdEnhet: 1});

            expect(vardenhetFilterModel.units[0].fragaSvar).toEqual(1);
            expect(vardenhetFilterModel.units[0].tooltip).toBeTruthy();
        });
    });

});
