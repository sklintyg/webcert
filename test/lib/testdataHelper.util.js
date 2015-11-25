/**
 * Created by BESA on 2015-11-25.
 */
'use strict';

var restUtil = require('./rest.util.js');
var intygTemplates = require('./testdata/intygTemplates.js');
var intygGenerator = require('./intygGenerator.util.js');
var fkUtkastTemplate = require('./testdata/utkast.fk7263.generate.json');

module.exports = {
    createIntygFromTemplate: function(intygTemplate, intygCallback) {
        // login with doctor Jan Nilsson
        restUtil.login({
            'fornamn': 'Jan',
            'efternamn': 'Nilsson',
            'hsaId': 'IFV1239877878-1049',
            'enhetId': 'IFV1239877878-1042',
            'lakare': true,
            'forskrivarKod': '2481632'
        }).then(function(data) {
            console.log('Login OK');
        });

        // Clean all intyg
        restUtil.deleteAllIntyg().then(function(response){});

        return restUtil.createIntyg(intygGenerator.buildIntyg(intygTemplates[intygTemplate]));
    },
    createUtkastFromTemplate: function() {
        // login with doctor Jan Nilsson
        restUtil.login({
            'fornamn': 'Jan',
            'efternamn': 'Nilsson',
            'hsaId': 'IFV1239877878-1049',
            'enhetId': 'IFV1239877878-1042',
            'lakare': true,
            'forskrivarKod': '2481632'
        }).then(function(data) {
            console.log('Login OK');
        });

        // Clean all utkast
        restUtil.deleteAllUtkast().then(function(response){});

        fkUtkastTemplate.patientPersonnummer = '191212121212';
        return restUtil.createUtkast('fk7263', fkUtkastTemplate);
    }
};