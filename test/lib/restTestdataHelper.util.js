/**
 * Created by BESA on 2015-11-25.
 */
'use strict';

var restUtil = require('./util/rest.util.js');
var intygTemplates = require('./testdata/intygTemplates.js');
var intygGenerator = require('./util/intygGenerator.util.js');

module.exports = {
    createIntygFromTemplate: function(intygTemplate, intygCallback) {
        restUtil.login();
        return restUtil.createIntyg(intygGenerator.buildIntyg(intygTemplates[intygTemplate]));
    },
    deleteAllIntyg: function() {
        restUtil.login();
        return restUtil.deleteAllIntyg();
    },
    createUtkast: function(intygType) {
        restUtil.login();
        var utkastTemplate = require('./testdata/utkast.' + intygType + '.generate.json');
        return restUtil.createUtkast(intygType, utkastTemplate);
    },
    deleteAllUtkast: function() {
        restUtil.login();
        return restUtil.deleteAllUtkast();
    }
};