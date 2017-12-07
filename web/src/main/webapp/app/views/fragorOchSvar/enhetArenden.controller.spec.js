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

describe('FragorOchSvarCtrlSpec', function() {
    'use strict';

    var $controller;
    var $cookies;
    var $scope;
    var $location;
    var $httpBackend;
    var $timeout;
    var ArendeProxy;
    var ArendeVidarebefordraHelper;

    var testDefaultQuery = {
        enhetId: undefined,
        startFrom: 0,
        pageSize: 10,

        questionFromFK: false,
        questionFromWC: false,
        hsaId: undefined, // läkare
        vidarebefordrad: undefined, // 3-state

        changedFrom: undefined,
        changedTo: undefined,

        vantarPa: 'ALLA_OHANTERADE'
    };

    var testQAResponse = {
        'totalCount': 3,
        'results': [
        {
            'meddelandeId': 13,
            'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
            'intygTyp': 'fk7263',
            'signeratAv': 'Åsa Andersson',
            'status': 'PENDING_EXTERNAL_ACTION',
            'patientId': '19121212-1212',
            'receivedDate': '2015-01-27T16:02:31.812',
            'vidarebefordrad': 'false',
            'fragestallare': 'WC',
            'amne': 'AVSTAMNINGSMOTE',
            'enhetsnamn': 'WebCert-Enhet1',
            'vardgivarnamn': 'WebCert-Vårdgivare1'
        },
        {
            'meddelandeId': 14,
            'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
            'intygTyp': 'fk7263',
            'signeratAv': 'Åsa Andersson',
            'status': 'PENDING_EXTERNAL_ACTION',
            'patientId': '19121212-1212',
            'receivedDate': '2015-01-27T16:02:31.812',
            'vidarebefordrad': 'false',
            'fragestallare': 'WC',
            'amne': 'AVSTAMNINGSMOTE',
            'enhetsnamn': 'WebCert-Enhet1',
            'vardgivarnamn': 'WebCert-Vårdgivare1'
        },
        {
            'meddelandeId': 15,
            'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
            'intygTyp': 'fk7263',
            'signeratAv': 'Åsa Andersson',
            'status': 'PENDING_EXTERNAL_ACTION',
            'patientId': '19121212-1212',
            'receivedDate': '2015-01-27T16:02:31.812',
            'vidarebefordrad': 'false',
            'fragestallare': 'WC',
            'amne': 'AVSTAMNINGSMOTE',
            'enhetsnamn': 'WebCert-Enhet1',
            'vardgivarnamn': 'WebCert-Vårdgivare1'
        }]
    };
    var testWCQAResponse = {
        'totalCount': 3,
        'results': [
        {
            'meddelandeId': 13,
            'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
            'intygTyp': 'fk7263',
            'signeratAv': 'Åsa Andersson',
            'status': 'PENDING_EXTERNAL_ACTION',
            'patientId': '19121212-1212',
            'receivedDate': '2015-01-27T16:02:31.812',
            'vidarebefordrad': 'false',
            'fragestallare': 'WC',
            'amne': 'AVSTAMNINGSMOTE',
            'enhetsnamn': 'WebCert-Enhet1',
            'vardgivarnamn': 'WebCert-Vårdgivare1'
        },
        {
            'meddelandeId': 15,
            'intygId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
            'intygTyp': 'fk7263',
            'signeratAv': 'Åsa Andersson',
            'status': 'PENDING_EXTERNAL_ACTION',
            'patientId': '19121212-1212',
            'receivedDate': '2015-01-27T16:02:31.812',
            'vidarebefordrad': 'false',
            'fragestallare': 'WC',
            'amne': 'AVSTAMNINGSMOTE',
            'enhetsnamn': 'WebCert-Enhet1',
            'vardgivarnamn': 'WebCert-Vårdgivare1'
        }]
    };

    var testIntygsReferens = {
        'intygsId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
        'intygsTyp': 'fk7263',
        'patientNamn': 'Tolvan Tolvansson',
        'signeringsDatum': '2015-01-27T16:01:10.000',
        'patientId': {
            'patientIdRoot': '1.2.752.129.2.1.3.1', 'patientIdExtension': '19121212-1212'}
    };

    beforeEach(function() {

        module('webcertTest');
        module('webcert', ['$provide', function($provide) {

            var statService = jasmine.createSpyObj('common.statService', [ 'refreshStat' ]);
            $provide.value('common.statService', statService);
            $provide.value('common.enhetArendenCommonService', jasmine.createSpyObj('common.enhetArendenCommonService',
                [ 'handleVidareBefodradToggle', 'decorateSingleItemMeasure', 'setVidareBefordradState',
                    'buildMailToLink', 'checkQAonlyDialog' ]));
            $provide.value('common.User', {});
            ArendeProxy = jasmine.createSpyObj('common.ArendeProxy', [ 'setVidarebefordradState' ]);
            $provide.value('common.ArendeProxy', ArendeProxy);

            ArendeVidarebefordraHelper = jasmine.createSpyObj('common.ArendeVidarebefordraHelper', [ 'handleVidareBefodradToggle', 'buildMailToLink' ]);
            $provide.value('common.ArendeVidarebefordraHelper', ArendeVidarebefordraHelper);

            var modalMock;
            var dialogService = jasmine.createSpyObj('common.dialogService', [ 'showDialog' ]);
            modalMock = jasmine.createSpyObj('modal', [ 'close' ]);
            dialogService.showDialog.and.callFake(function() {
                return modalMock;
            });
            $provide.value('common.dialogService', dialogService);
            $provide.value('webcert.ManageCertificate', {});

            var $window = {};
            $window.location = {};
            $window.document = window.document; // document is now needed for ngCookies

            //configure this value with the provider.
            $provide.value('$window', $window);
        }]);

        inject(['$rootScope', '$location', '$timeout', '$httpBackend', '$controller', '$cookies',
            function($rootScope, _$location_, _$timeout_, _$httpBackend_, _$controller_, _$cookies_) {
                $scope = $rootScope.$new();
                $location = _$location_;
                $httpBackend = _$httpBackend_;
                $timeout = _$timeout_;
                $controller = _$controller_;
                $cookies = _$cookies_;
                $controller('webcert.FragorOchSvarCtrl', { $scope: $scope });
            }]);
    });

    describe('FragorOchSvarCtrl listing and filtering of QAs ', function() {

        it('should get and fill currentList with 3 entries when savedFilterQuery is not set', function() {
            $cookies.remove('savedFilterQuery');
            $httpBackend.expectGET('/api/fragasvar/sok?pageSize=10&questionFromFK=false&questionFromWC=false&startFrom=0&vantarPa=ALLA_OHANTERADE').respond(200,
                testQAResponse);
            $scope.filterList();
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(3);
        });

        it('should update and filter list with questions from WC when savedFilterQuery is set', function() {

            var defaultQuery = angular.copy(testDefaultQuery);
            defaultQuery.questionFromWC = true;
            $cookies.putObject('savedFilterQuery', defaultQuery);
            $httpBackend.
                expectGET('/api/fragasvar/sok?pageSize=10&questionFromFK=false&questionFromWC=false&startFrom=0&vantarPa=ALLA_OHANTERADE').respond(200,
                testWCQAResponse);
            $scope.filterList();
            $httpBackend.flush();
            expect($scope.widgetState.currentList.length).toBe(2);
        });

        it('should try to view an intyg when user clicks Visa on a intyg in the list', function() {
            spyOn($location, 'url').and.callThrough();
            $scope.openIntyg(testIntygsReferens.intygsId, testIntygsReferens.intygsTyp);
            expect($location.url).toHaveBeenCalledWith('/fragasvar/' + testIntygsReferens.intygsTyp.toLowerCase() +
                '/' +
                testIntygsReferens.intygsId, true);
        });

        it('should fetch PAGE_SIZE more results if user clicks fetch more button', function() {
            $scope.filterQuery.startFrom = 0;
            $scope.fetchMore();
            expect($scope.filterQuery.startFrom).toBe($scope.filterQuery.pageSize);
        });

        it('should reset filter parameters and delete saved cookie if user clicks reset', function() {
            $scope.filterQuery = {};
            $scope.resetFilterForm();
            expect($scope.filterQuery).toEqual(testDefaultQuery);
        });
    });

    describe('FragorOchSvarCtrl set active unit message', function() {

        beforeEach(function() {
            spyOn($location, 'url').and.callThrough();
        });

        it('should update active unit and fetch QA when wcVardenhetFilter.unitSelected message is received', function() {

            var unit = {'namn': 'WebCert-Enhet2', 'id': 'IFV1239877878-1045', 'fragaSvar': 0, 'intyg': 0};

            $httpBackend.expectGET('/api/fragasvar/lakare?enhetsId=IFV1239877878-1045').respond(200, {});
            $httpBackend.expectGET('/api/fragasvar/sok?enhetId=IFV1239877878-1045&pageSize=10&questionFromFK=false&questionFromWC=false&startFrom=0&vantarPa=ALLA_OHANTERADE').respond(200,
                {});
            $scope.$broadcast('wcVardenhetFilter.unitSelected', unit);
            $httpBackend.flush();

            expect($cookies.getObject('enhetsId')).toBe('IFV1239877878-1045');
            expect($scope.activeUnit).toEqual(unit);
        });
    });

    describe('FragorOchSvarCtrl QA forwarding', function() {

        it('should change forwarded state on a QA when clicking a forward checkbox', function() {
            $scope.onVidareBefordradChange(testQAResponse.results[0]);
            expect(ArendeProxy.setVidarebefordradState).toHaveBeenCalled();
        });

        it('should open external mail client when user wants to forward a QA', function() {

            $scope.openMailDialog(testQAResponse.results[0]);
            $timeout.flush();
            expect(ArendeVidarebefordraHelper.handleVidareBefodradToggle).toHaveBeenCalled();
            expect(ArendeVidarebefordraHelper.buildMailToLink).toHaveBeenCalled();
        });
    });

});
