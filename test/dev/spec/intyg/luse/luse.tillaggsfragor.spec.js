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

/*globals browser */
/*globals describe,it */
/*globals afterEach,beforeEach */
/*globals $httpBackend */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var LuseUtkastPage = wcTestTools.pages.intyg.luse.utkast;

var HttpBackend = require('http-backend-proxy');
var proxy = new HttpBackend(browser);

var texts = require('../../../../webcertTestTools/testdata/luse_questions.json');

// disabled until we get textfiles with tilläggsfrågor in them
describe('Luse tillaggsfragor variants', function() {
    it('Login through the welcome page with user', function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('mock backend responses block', function() {

        var intygsId = 'testIntygsId';
        var utkastData = {'version':0,'vidarebefordrad':false,'status':'DRAFT_COMPLETE','enhetsNamn':'WebCert-Enhet1','vardgivareNamn':'WebCert-Vårdgivare1','latestTextVersion':'0.9',
            'content':{
                'id':'eb53075e-a685-41f6-9a8b-b498300c912e',
                'grundData':{'skapadAv':{'personId':'IFV1239877878-1049','fullstandigtNamn':'Jan Nilsson','forskrivarKod':'0000000','befattningar':[],'specialiteter':[],'vardenhet':{'enhetsid':'IFV1239877878-1042','enhetsnamn':'WebCert-Enhet1','postadress':'Storgatan 1','postnummer':'12345','postort':'Småmåla','telefonnummer':'0101234567890','epost':'enhet1@webcert.invalid.se','vardgivare':{'vardgivarid':'IFV1239877878-1041','vardgivarnamn':'WebCert-Vårdgivare1'},'arbetsplatsKod':'1234567890'}},
                             'patient':{'personId':'19121212-1212','fullstandigtNamn':'Tolvan Tolvansson','fornamn':'Tolvan','efternamn':'Tolvansson','postadress':'Svensson, Storgatan 1, PL 1234','postnummer':'12345','postort':'Småmåla','samordningsNummer':false}},
                'textVersion':'0.9','underlag':[],'diagnoser':[],'tillaggsfragor':[
                    {'id':'9001','svar':'50'},{'id':'9002','svar':'200cm'}],'typ':'luse'}};

        afterEach(function () {
            proxy.onLoad.reset();
            specHelper.removeHttpBackendMock();
        });

        beforeEach(function () {
            specHelper.loadHttpBackendMock();
            proxy.context = {
                texts:texts,
                utkastData: utkastData
            };
        });

        function utkastResponse() {
            return [200, $httpBackend.context.utkastData];
        }

        function textResponse(method, url) {
            // Note: This method is run in the browser
            var version = url.split('?')[0].split('/')[5];
            var texts = $httpBackend.context.texts;
            if (version === '1.0') {
                texts.texter = {
                    'DFR_9001.1.RBK':'Vilken skostorlek har du?',
                    'DFR_9001.1.HLP':'Ange din skostorlek.',
                    'DFR_9003.1.RBK':'Det här är en ny extrafråga',
                    'DFR_9003.1.HLP':'Hjälp för ny fråga.'};
                texts.tillaggsfragor = [
                    {'id':'9001','text':'Vilken skostorlek har du?','help':'Ange din skostorlek.'},
                    {'id':'9003','text':'Det här är en ny extrafråga','help':'Hjälp för ny fråga'}];
            }
            return [200, texts];
        }

        it('textversion is 0.9, there are 2 tillaggsfragor', function () {
            proxy.context.utkastData.latestTextVersion = '0.9';
            proxy.syncContext();
            proxy.onLoad.whenGET(new RegExp('/moduleapi/utkast/luse/' + intygsId + '.*')).respond(utkastResponse);
            proxy.onLoad.whenGET(new RegExp('/api/utkast/questions/luse/.*')).respond(textResponse);
            proxy.onLoad.whenPOST(/.*/).passThrough();
            proxy.onLoad.whenGET(/.*/).passThrough();
            LuseUtkastPage.get(intygsId);

            expect(LuseUtkastPage.getTillaggsfragaText(9001)).toBe('Vilken skostorlek har du?');
            expect(LuseUtkastPage.getTillaggsfragaSvar(9001)).toBe('50');
            expect(LuseUtkastPage.getTillaggsfragaText(9002)).toBe('Hur lång är du?');
            expect(LuseUtkastPage.getTillaggsfragaSvar(9002)).toBe('200cm');
            expect(LuseUtkastPage.getTillaggsfraga(9003).isPresent()).toBeFalsy();

            expect(LuseUtkastPage.isSigneraButtonEnabled()).toBeTruthy();
        });

        it('textversion has been updated to 1.0', function () {
            proxy.context.utkastData.latestTextVersion = '1.0';
            proxy.syncContext();
            proxy.onLoad.whenGET(new RegExp('/moduleapi/utkast/luse/' + intygsId + '.*')).respond(utkastResponse);
            proxy.onLoad.whenGET(new RegExp('/api/utkast/questions/luse/.*')).respond(textResponse);
            proxy.onLoad.whenPOST(/.*/).passThrough();
            proxy.onLoad.whenGET(/.*/).passThrough();
            proxy.onLoad.whenPUT(new RegExp('/moduleapi/utkast/luse/.*')).respond({'version':2,'status':'DRAFT_COMPLETE','messages':[]});
            LuseUtkastPage.get(intygsId);

            // En extrafråga har tagits bort, en annan ska ha tillkommit
            expect(LuseUtkastPage.getTillaggsfragaText(9001)).toBe('Vilken skostorlek har du?');
            expect(LuseUtkastPage.getTillaggsfragaSvar(9001)).toBe('50');
            expect(LuseUtkastPage.getTillaggsfragaText(9003)).toBe('Det här är en ny extrafråga');
            expect(LuseUtkastPage.getTillaggsfragaSvar(9003)).toBe('');
            expect(LuseUtkastPage.getTillaggsfraga(9002).isPresent()).toBeFalsy();

            expect(LuseUtkastPage.isSigneraButtonEnabled()).toBeTruthy();
        });
    });
});
