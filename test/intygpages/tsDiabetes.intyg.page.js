/**
 * Created by bennysce on 09/06/15.
 */
'use strict';

var BaseIntyg = require('./base.intyg.page.js');

var TsDiabetesIntyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-diabetes';
    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
});

module.exports = new TsDiabetesIntyg();
