/**
 * Created by bennysce on 09/06/15.
 */
'use strict';

var BaseIntyg = require('./base.intyg.page.js');

var TsBasIntyg = BaseIntyg._extend({
    init: function init() {
        init._super.call(this);
        this.intygType = 'ts-bas';
    },
    get: function get(intygId) {
        get._super.call(this, intygId);
    },
});

module.exports = new TsBasIntyg();
