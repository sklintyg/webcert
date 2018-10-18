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
    var mockResponse;
    var utkastNotifyService;
    var intygListService;
    var IntygHelper;
    var element;

    beforeEach(angular.mock.module('htmlTemplates'));
    beforeEach(function() {
        module('htmlTemplates');
        module('webcertTest');
        module('webcert', ['$provide', '$stateProvider', function($provide, _$stateProvider_) {

            var statService = jasmine.createSpyObj('common.statService', ['refreshStat']);
            $provide.value('common.statService', statService);

            var moduleService = jasmine.createSpyObj('common.moduleService', ['getModuleName']);
            $provide.value('common.moduleService', moduleService);

            $provide.value('PersonIdFormatterFilter', function(){});

            utkastNotifyService =
                jasmine.createSpyObj('common.UtkastNotifyService', ['onNotifyChange', 'notifyUtkast']);
            $provide.value('common.UtkastNotifyService', utkastNotifyService);

            intygListService =
                jasmine.createSpyObj('webcert.intygListService', ['checkVidareBefordraAuth']);
            $provide.value('webcert.intygListService', intygListService);

            IntygHelper =
                jasmine.createSpyObj('common.IntygHelper', ['goToDraft']);
            $provide.value('common.IntygHelper', IntygHelper);

            _$stateProvider_.state('fk7263-edit', {
                url: '/fk7263/edit/:certificateId'
            });
        }]);

        inject(['$rootScope', '$compile', '$state', 'mockResponse',
            function($rootScope, $compile, _$state_, _mockResponse_) {

                $state = _$state_;
                mockResponse = _mockResponse_;

                var tpl = angular.element(
                    '<wc-utkast-list utkast-list="testList" filter="testFilter"></wc-utkast-list>'
                );
                $scope = $rootScope.$new();
                $scope.testList = mockResponse.utkastList;
                $scope.testFilter = {selection: { orderBy: '', orderAscending: '' }};
                element = $compile(tpl)($scope);
                $scope.$digest();
            }
        ]);
    });

    describe('visa intyg button', function() {
        it('should try to view an intyg', function() {
            element.isolateScope().openIntyg(mockResponse.utkast);
            expect(IntygHelper.goToDraft).toHaveBeenCalledWith(mockResponse.utkast.intygType.toLowerCase(), mockResponse.utkast.intygTypeVersion,
                    mockResponse.utkast.intygId);
        });
    });

    describe('EjSigneradeUtkastCtrl QA forwarding', function() {

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
