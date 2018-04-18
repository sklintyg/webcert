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

describe('wcEnhetArendenFilter', function() {
    'use strict';

    var $rootScope;
    var $scope;
    var $compile;
    var element;
    var enhetArendenFilterModel;
    var enhetArendenFilterService;

    beforeEach(function() {

        module('htmlTemplates');
        module('webcertTest');
        module('webcert', ['$provide', function($provide) {
            $provide.value('webcert.vardenhetFilterModel', {});
        }]);

        inject(['$rootScope', '$compile', 'webcert.enhetArendenFilterModel', 'webcert.enhetArendenFilterService', '$httpBackend',
            function(_$rootScope_, _$compile_, _enhetArendenFilterModel_, _enhetArendenFilterService_, _$httpBackend_) {
                $rootScope = _$rootScope_;
                $compile = _$compile_;
                enhetArendenFilterModel = _enhetArendenFilterModel_;
                enhetArendenFilterService = _enhetArendenFilterService_;

                _$httpBackend_.expectGET('/api/fragasvar/lakare').respond(200, []);

                $scope = $rootScope.$new();
                element = $compile('<wc-enhet-arenden-filter></wc-enhet-arenden-filter>')($scope);
                $scope.$digest();
                $scope = element.isolateScope();
            }]);
    });

    describe('filterList', function() {

        it('should send event updating arenden list', function() {

            // Make sure update list event is called
            spyOn($rootScope, '$broadcast').and.stub();

            $scope.filterList();

            expect($rootScope.$broadcast).toHaveBeenCalled();
        });

        it('should reset filter parameters if user clicks reset', function() {
            // Make sure update list event is called
            spyOn($rootScope, '$broadcast').and.stub();

            enhetArendenFilterModel.filterForm.changedTo = '2010-01-01';
            $scope.resetFilterForm();

            expect(enhetArendenFilterModel.filterForm.changedTo).toBeUndefined();
            expect($rootScope.$broadcast).toHaveBeenCalled();
        });
    });

    describe('events', function() {

        it('should update active unit and update lakare list wcVardenhetFilter.unitSelected message is received', function() {

            spyOn(enhetArendenFilterModel, 'reset').and.callThrough();
            spyOn(enhetArendenFilterService, 'initLakareList').and.stub();

            $scope.$broadcast('wcVardenhetFilter.unitSelected', { id: 'unitId'});

            expect(enhetArendenFilterModel.reset).toHaveBeenCalled();
            expect(enhetArendenFilterService.initLakareList).toHaveBeenCalledWith('unitId');
        });
    });

});
