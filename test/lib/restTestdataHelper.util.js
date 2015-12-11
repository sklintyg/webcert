/**
 * Created by BESA on 2015-11-25.
 */
'use strict';

var restUtil = require('./util/rest.util.js');
var intygTemplates = require('./testdata/intygTemplates.js');
var intygGenerator = require('./util/intygGenerator.util.js');

module.exports = {
    createIntygFromTemplate: function(intygTemplate, intygId) {
        restUtil.login();
        var intyg = intygGenerator.buildIntyg(intygTemplates[intygTemplate]);
        intyg.id = intygId;
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
    createUtkast: function(intygType) {
        restUtil.login();
        var utkastTemplate = require('./testdata/utkast.' + intygType + '.generate.json');
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