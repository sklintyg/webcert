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

describe('wcEnhetArendenList', function() {
    'use strict';

    var $q;
    var $rootScope;
    var $scope;
    var $compile;
    var $location;
    var $timeout;
    var element;
    var enhetArendenListModel;
    var enhetArendenListService;
    var ArendeVidarebefordraHelper;
    var ArendeProxy;
    var statService;

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

    var testIntygsReferens = {
        'intygsId': '6c10506a-1ef1-4ce2-a4c8-a82fa43a1e26',
        'intygsTyp': 'fk7263',
        'patientNamn': 'Tolvan Tolvansson',
        'signeringsDatum': '2015-01-27T16:01:10.000',
        'patientId': {
            'patientIdRoot': '1.2.752.129.2.1.3.1', 'patientIdExtension': '19121212-1212'}
    };

    beforeEach(function() {

        module('htmlTemplates');
        module('webcertTest');
        module('webcert', ['$provide', function($provide) {

            $provide.value('common.enhetArendenCommonService', jasmine.createSpyObj('common.enhetArendenCommonService',
                [ 'handleVidareBefordradToggle', 'decorateSingleItemMeasure', 'setVidareBefordradState',
                    'buildMailToLink', 'checkQAonlyDialog' ]));

            var dialogService = jasmine.createSpyObj('common.dialogService', [ 'showDialog' ]);
            var modalMock;
            modalMock = jasmine.createSpyObj('modal', [ 'close' ]);
            dialogService.showDialog.and.callFake(function() {
                return modalMock;
            });
            $provide.value('common.dialogService', dialogService);
            ArendeVidarebefordraHelper = jasmine.createSpyObj('common.ArendeVidarebefordraHelper', [ 'handleVidareBefordradToggle', 'buildMailToLink' ]);
            $provide.value('common.ArendeVidarebefordraHelper', ArendeVidarebefordraHelper);

            $provide.value('common.ArendeProxy', {});
            $provide.value('webcert.vardenhetFilterModel', {});

            // Because of filter used in template
            $provide.value('PersonIdFormatterFilter', function(){});

            $provide.value('common.authorityService', jasmine.createSpyObj('common.authorityService', ['isAuthorityActive']));
            var featureService = jasmine.createSpyObj('common.featureService', [ 'isFeatureActive' ]);
            featureService.features = {};
            $provide.value('common.featureService', featureService);

            var UserModel = jasmine.createSpyObj('common.UserModel', ['isLakare', 'isTandlakare', 'isPrivatLakare', 'isDjupintegration']);
            UserModel.isVardAdministrator = function () { return true; };

            $provide.value('common.UserModel', UserModel);
            $provide.value('common.messageService', jasmine.createSpyObj('common.messageService', ['getProperty']));

            $provide.value('common.statService', {getLatestData:function(){}});
            $provide.value('common.IntygProxy', {
                getIntygTypeInfo: function(id, onSuccess/*, onError*/) {
                    onSuccess({intygId: id, intygType: testIntygsReferens.intygsTyp, intygTypeVersion: '1.0'});
                }
            });

            // To prevent $window.location from reloading the page
            var $window = {};
            $window.location = {};
            $window.document = window.document; // document is now needed for ngCookies

            //configure this value with the provider.
            $provide.value('$window', $window);
        }]);

        inject(['$rootScope', '$compile', '$q', '$location', '$timeout', 'common.ArendeProxy', 'webcert.enhetArendenListModel',
            'webcert.enhetArendenListService', '$templateCache', 'webcert.enhetArendenFilterService', 'common.statService',
            function(_$rootScope_, _$compile_, _$q_, _$location_, _$timeout_, _ArendeProxy_, _enhetArendenListModel_,
                _enhetArendenListService_, $templateCache, _enhetArendenFilterService_, _statService_) {

                $templateCache.put('/web/webjars/common/webcert/components/headers/wcHeader.partial.html', '');

                $rootScope = _$rootScope_;
                $compile = _$compile_;
                $q = _$q_;
                $location = _$location_;
                $timeout = _$timeout_;
                enhetArendenListModel = _enhetArendenListModel_;
                enhetArendenListService = _enhetArendenListService_;
                statService = _statService_;
                ArendeProxy = _ArendeProxy_;

                spyOn(_enhetArendenFilterService_, 'initLakareList').and.stub();

                $scope = $rootScope.$new();
                element = $compile('<wc-enhet-arenden-list></wc-enhet-arenden-list>')($scope);
                spyOn(enhetArendenListService, 'getArenden').and.callFake(function(){
                    var deferred = $q.defer();
                    deferred.resolve({
                        query: {},
                        totalCount: testQAResponse.totalCount,
                        arendenList: testQAResponse.results
                    });
                    return deferred.promise;
                });
                $scope.$digest();
                $scope = element.isolateScope();
            }]);
    });

    describe('listing', function() {

        it('should load list of arenden at startup', function(){
            // filled on load at compile above using the list service stub
            expect(enhetArendenListModel.arendenList.length).toEqual(3);
        });

        it('should try to view an intyg when user clicks Visa on a intyg in the list', function() {
            spyOn($location, 'url').and.stub();
            $scope.openIntyg(testIntygsReferens.intygsId, testIntygsReferens.intygsTyp);
            expect($location.url).toHaveBeenCalledWith('/fragasvar/' + testIntygsReferens.intygsTyp.toLowerCase() +
                '/1.0/' +
                testIntygsReferens.intygsId);
        });

        it('should fetch PAGE_SIZE more results if user clicks fetch more button', function() {
            enhetArendenListModel.prevFilterQuery.startFrom = 0;
            enhetArendenListModel.arendenList = [];
            $scope.fetchMore();
            $scope.$digest(); // $q
            expect(enhetArendenListModel.arendenList.length).toBe(3);
        });

    });

    describe('vidarebefordra', function() {
        it('should open external mail client when user wants to forward a QA', function() {

            $scope.openMailDialog(testQAResponse.results[0]);
            $timeout.flush();
            expect(ArendeVidarebefordraHelper.handleVidareBefordradToggle).toHaveBeenCalled();
            expect(ArendeVidarebefordraHelper.buildMailToLink).toHaveBeenCalled();
        });
    });

});
