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
/*globals beforeAll,afterAll */
/*globals protractor */
'use strict';

var wcTestTools = require('webcert-testtools');
var specHelper = wcTestTools.helpers.spec;
var SokSkrivIntygPage = wcTestTools.pages.sokSkrivIntyg.pickPatient;

var HttpBackend = require('http-backend-proxy');
var proxy = new HttpBackend(browser);

describe('Luse kompletteringsbegäran tests', function() {

    beforeAll(function() {
        browser.ignoreSynchronization = false;
        specHelper.login();
    });

    describe('mock backend responses block', function() {

        afterEach(function () {
            specHelper.removeHttpBackendMock();
        });

        beforeEach(function () {
            specHelper.loadHttpBackendMock();
        });

        var utkastData = {'version':0,'vidarebefordrad':false,'status':'DRAFT_COMPLETE','enhetsNamn':'NMT vg3 ve1','vardgivareNamn':'NMT vg3',
            'content':{'id':'testIntygId1',
                'grundData':{
                    'skapadAv':{'personId':'TSTNMT2321000156-1079','fullstandigtNamn':'Arnold Johansson','forskrivarKod':'0000000','befattningar':['204010'],
                        'specialiteter':[],'vardenhet':{'enhetsid':'TSTNMT2321000156-1077','enhetsnamn':'NMT vg3 ve1','postadress':'NMT gata 3','postnummer':'12345',
                        'postort':'Testhult','telefonnummer':'0101112131416', 'epost':'enhet3@webcert.invalid.se', 'vardgivare':{'vardgivarid':'TSTNMT2321000156-102Q',
                        'vardgivarnamn':'NMT vg3'},'arbetsplatsKod':'1234567890'}},
                    'patient':{'personId':'19121212-1212','fullstandigtNamn':'Tolvan Tolvansson','fornamn':'Tolvan','efternamn':'Tolvansson','postadress':'Svensson, Storgatan 1, PL 1234',
                    'postnummer':'12345','postort':'Småmåla','samordningsNummer':false}, 'relation':{'relationKod':'KOMPLT','relationIntygsId':'testIntygId2',
                    'meddelandeId':'3019eaa0-5cbd-0426-249a-d0de732262c8'}},
                'textVersion':'1.0',
                'typ':'luse'},
            'latestTextVersion':'1.0',
            'relations':[
                {'intygsId':'testIntygId1','kod':'KOMPLT','status':'DRAFT_COMPLETE','date':'2016-11-25T08:42:24.640'},
                {'intygsId':'testIntygId2','status':'SENT','date':'2016-11-25T08:41:06.460'}]};

        function arendeData(kompletteringar) {
            return [{
                'fraga': {
                    'kompletteringar': kompletteringar,
                    'internReferens': '3019eaa0-5cbd-0426-249a-d0de732262c8',
                    'status': 'PENDING_INTERNAL_ACTION',
                    'amne': 'KOMPLT',
                    'meddelandeRubrik': 'Komplettering',
                    'vidarebefordrad': false,
                    'frageStallare': 'FK',
                    'externaKontakter': [],
                    'meddelande': '',
                    'signeratAv': 'Arnold Johansson',
                    'svarSkickadDatum': '2016-07-13T17:23:00.000',
                    'intygId': '4f7b994b-22a8-417d-93e7-efa473d27d14',
                    'enhetsnamn': 'NMT vg3 ve1',
                    'vardgivarnamn': 'NMT vg3',
                    'timestamp': '2016-11-25T08:41:59.957',
                    'arendeType': 'FRAGA'
                }, 'senasteHandelse': '2016-11-25T08:41:59.957', 'paminnelser': []
            }];
        }

        // INTYG-3252 frågeId 1 without instans parameter set
        it('should show kompletteringsbegäran for group baseratPa', function () {
            SokSkrivIntygPage.get();
            proxy.whenGET(new RegExp('/moduleapi\/utkast\/luse\/testIntygId1\?.*')).respond(200, utkastData);
            proxy.whenGET(new RegExp('/moduleapi\/arende\/testIntygId2\?.*')).respond(200, arendeData(
                [{'position':0,'frageId':'1','text':'Detta är kompletteringstexten datum','jsonPropertyHandle':'baseratPa'}]
            ));
            proxy.whenPOST(/.*/).passThrough();
            proxy.whenGET(/.*/).passThrough();
            browser.setLocation('luse/edit/testIntygId1');

            // Kompletteringsbegäran should be displayed immediately under validationGroup-baserasPa
            expect(element(by.id('validationGroup-baserasPa')).getAttribute('class')).toContain('groupkomplettering');
            expect(element(by.css('#validationGroup-baserasPa > .validation')).isDisplayed()).toBeTruthy();
            expect(element(by.css('#validationGroup-baserasPa > .validation')).getText()).toEqual('Kompletteringsbegäran');
            expect(element(by.css('.fieldkomplettering #form_undersokningAvPatienten')).isPresent()).toBeFalsy();
        });

        it('should show kompletteringsbegäran for field undersokningAvPatienten', function () {
            SokSkrivIntygPage.get();
            proxy.whenGET(new RegExp('/moduleapi\/utkast\/luse\/testIntygId1\?.*')).respond(200, utkastData);
            proxy.whenGET(new RegExp('/moduleapi\/arende\/testIntygId2\?.*')).respond(200, arendeData(
                [{'position':1,'frageId':'1','text':'Detta är kompletteringstexten datum','jsonPropertyHandle':'undersokningAvPatienten'}]
            ));
            proxy.whenPOST(/.*/).passThrough();
            proxy.whenGET(/.*/).passThrough();
            browser.setLocation('luse/edit/testIntygId1');

            // Kompletteringsbegäran should not be displayed immediately under validationGroup-baserasPa
            expect(element(by.css('#validationGroup-baserasPa > .validation')).isPresent()).toBeFalsy();
            // Kompletteringsbegäran should be displayed under form_undersokningAvPatienten
            expect(element(by.css('div[data-komplettering-section="undersokningAvPatienten"]')).isDisplayed()).toBeTruthy();
            expect(element(by.css('div[data-komplettering-section="undersokningAvPatienten"]')).getText()).toEqual('Kompletteringsbegäran');
            expect(element(by.css('.fieldkomplettering #form_undersokningAvPatienten')).isPresent()).toBeTruthy();
        });
    });
});
