/*
 * Copyright (C) 2015 Inera AB (http://www.inera.se)
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

/**
 * Created by BESA on 2015-11-25.
 * Holds helper functions for actions that are needed often in specs.
 */
/*globals browser */
'use strict';

var pages = require('./../pages.js');
var WelcomePage = pages.welcome;
var SokSkrivIntygPage = pages.app.views.sokSkrivIntyg;

module.exports = {
    login: function(userOptional) {
        WelcomePage.get();
        WelcomePage.login(userOptional || 'IFV1239877878-104B_IFV1239877878-1042');
        browser.sleep(5000); // need to sleep here since we aren't in the angular app yet
        expect(SokSkrivIntygPage.getDoctorText()).toContain('Åsa Andersson');
    },
    createUtkastForPatient: function(patientId, intygType) {
        SokSkrivIntygPage.selectPersonnummer(patientId);
        SokSkrivIntygPage.selectIntygType('string:'+ intygType);
        SokSkrivIntygPage.continueToUtkast();
        var UtkastPage = pages.intygpages[intygType+'Utkast'];
        expect(UtkastPage.isAt()).toBe(true);
    },
    generateTestGuid: function(){
        function s4() {
            return Math.floor((1 + Math.random()) * 0x10000)
                .toString(16)
                .substring(1);
        }
        return s4() + s4() + '-' + s4() + '-' + s4() + '-' +
            s4() + '-' + s4() + s4() + s4();
    },
    loadHttpBackendMock: function() {
        browser.addMockModule('httpBackendMock', function () {
            function loadScript(urls) {
                for (var i = 0; i < urls.length; i++) {
                    var newScript = document.createElement('script');
                    newScript.type = 'text/javascript';
                    newScript.src = urls[i];
                    newScript.async = false;
                    newScript.defer = false;

                    document.getElementsByTagName('head')[0].appendChild(newScript);
                }
            }

            loadScript([
                '/web/webjars/angularjs/1.4.7/angular-mocks.js'
            ]);

            angular.module('httpBackendMock', ['ngMockE2E']);
        });
    }
};
