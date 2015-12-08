/**
 * Created by BESA on 2015-11-25.
 * Holds helper functions for actions that are needed often in specs.
 */
/*globals browser,pages */
'use strict';

var WelcomePage = pages.welcome;
var SokSkrivIntygPage = pages.app.views.sokSkrivIntyg;

module.exports = {
    login: function(userOptional) {
        WelcomePage.get();
        WelcomePage.login(userOptional || 'IFV1239877878-104B_IFV1239877878-1042');
        browser.sleep(500); // need to sleep here since we aren't in the angular app yet
        expect(SokSkrivIntygPage.getDoctorText()).toContain('Åsa Andersson');
    },
    createUtkastForPatient: function(patientId, intygType) {
        SokSkrivIntygPage.selectPersonnummer(patientId);
        SokSkrivIntygPage.selectIntygType('string:'+ intygType);
        SokSkrivIntygPage.continueToUtkast();
        var UtkastPage = pages.intygpages[intygType+'Utkast'];
        expect(UtkastPage.isAt()).toBe(true);
    }
};