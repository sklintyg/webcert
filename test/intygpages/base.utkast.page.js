/**
 * Created by bennysce on 02-12-15.
 */
/*globals browser*/
'use strict';

var Class = require('jclass');

var BaseUtkast = Class._extend({
    init: function() {
        this.at = null;
        this.signeraButton = element(by.id('signera-utkast-button'));
        this.pageHelper = null;
    },
    setPageHelper: function(pageHelper) {
        this.pageHelper = pageHelper;
    },
    get: function(intygType, intygId) {
        browser.get('/web/dashboard#/' + intygType + '/edit/' + intygId);
    },
    isAt: function() {
        return this.at.isDisplayed();
    },
    whenSigneraButtonIsEnabled: function() {
        return browser.wait(this.signeraButton.isEnabled());
    },
    signeraButtonClick: function() {
        this.signeraButton.click();
    }
});

module.exports = BaseUtkast;
