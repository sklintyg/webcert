/**
 * Created by stephenwhite on 09/06/15.
 */
var IntygPage = function() {
    'use strict';

    var viewCertAndQa = element(by.id('viewCertAndQA'));

    this.get = function() {
        browser.get('http://www.angularjs.org');
    };

    this.viewCertAndQaIsDisplayed = function(){
        return viewCertAndQa.isDisplayed();
    };
};

module.exports = IntygPage;
