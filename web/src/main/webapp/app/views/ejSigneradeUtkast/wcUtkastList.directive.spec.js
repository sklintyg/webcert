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

describe('wcUtkastListSpec', function() {
    'use strict';

    var $scope;
    var $state;
    var UtkastProxy;
    var mockResponse;
    var utkastNotifyService;
    var element;

    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(function() {
        module('htmlTemplates');
        module('webcertTest');
        module('webcert', ['$provide', '$stateProvider', function($provide, _$stateProvider_) {

            var statService = jasmine.createSpyObj('common.statService', ['refreshStat']);
            $provide.value('common.statService', statService);

            utkastNotifyService =
                jasmine.createSpyObj('common.UtkastNotifyService', ['onNotifyChange', 'notifyUtkast']);
            $provide.value('common.UtkastNotifyService', utkastNotifyService);

            UtkastProxy = jasmine.createSpyObj('webcert.UtkastProxy', ['getUtkastType']);
            $provide.value('webcert.UtkastProxy', UtkastProxy);

            _$stateProvider_.state('fk7263-edit', {
                url: '/fk7263/edit/:certificateId?:patientId&:hospName&:sjf'
            });
        }]);

        inject(['$rootScope', '$compile', '$state', 'mockResponse',
            function($rootScope, $compile, _$state_, _mockResponse_) {

                $state = _$state_;
                mockResponse = _mockResponse_;

                var tpl = angular.element(
                    '<wc-utkast-list utkast-list="testList"></wc-utkast-list>'
                );
                $scope = $rootScope.$new();
                $scope.testList = mockResponse.utkastList;
                element = $compile(tpl)($scope);
                $scope.$digest();
            }
        ]);
    });

    describe('visa intyg button', function() {
        it('should try to view an intyg', function() {
            spyOn($state, 'go');
            element.isolateScope().openIntyg(mockResponse.utkast);
            expect($state.go).toHaveBeenCalledWith(mockResponse.utkast.intygType.toLowerCase() +
                '-edit', {certificateId: mockResponse.utkast.intygId});
        });
    });

    describe('UnsignedCertCtrl QA forwarding', function() {

        it('should change forwarded state on a utkast when clicking a forward checkbox', function() {
            element.isolateScope().onNotifyChange(mockResponse.utkast);
            expect(utkastNotifyService.onNotifyChange).toHaveBeenCalled();
        });

        it('should open external mail client when user wants to forward a utkast', function() {
            element.isolateScope().openMailDialog(mockResponse.utkast);
            expect(utkastNotifyService.notifyUtkast).toHaveBeenCalled();
        });
    });

});
