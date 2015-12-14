/**
 * Created by BESA on 2015-11-25.
 */
'use strict';

var restUtil = require('./../util/rest.util.js');
var intygTemplates = require('./../testdata/intygTemplates.js');
var intygGenerator = require('./../util/intygGenerator.util.js');

module.exports = {
    createIntygFromTemplate: function(intygTemplateName, intygId) {
        restUtil.login();
        var template = intygTemplates[intygTemplateName];
        template.intygId = intygId;
        var intyg = intygGenerator.buildIntyg(template);
        return restUtil.createIntyg(intyg);
    },
    deleteAllIntyg: function() {
        restUtil.login();
        return restUtil.deleteAllIntyg();
    },
    deleteIntyg: function(id) {
        restUtil.login();
        return restUtil.deleteIntyg(id);
    },
    createUtkast: function(intygType, template) {
        restUtil.login();

        var utkastTemplate = template;
        if(typeof template === 'undefined') {
            utkastTemplate = {
                "intygType": intygType,
                "patientPersonnummer": "19121212-1212",
                "patientFornamn": "Tolvan",
                "patientEfternamn": "Tolvansson",
                "patientPostadress": "Svensson, Storgatan 1, PL 1234",
                "patientPostnummer": "12345",
                "patientPostort": "Småmåla"
            }
        }
        return restUtil.createUtkast(intygType, utkastTemplate);
    },
    deleteAllUtkast: function() {
        restUtil.login();
        return restUtil.deleteAllUtkast();
    },
    deleteUtkast: function(id) {
        restUtil.login();
        return restUtil.deleteUtkast(id);
    }
};