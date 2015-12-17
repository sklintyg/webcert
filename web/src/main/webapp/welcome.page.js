/* globals browser */

/**
 * Created by stephenwhite on 09/06/15.
 */
'use strict';


var loginButton = element(by.id('loginBtn'));
var jsonDisplay = element(by.id('userJsonDisplay'));

module.exports = {
    get: function () {
        browser.get('welcome.jsp');
    },
    login: function (userId) {
        // login id IFV1239877878-104B_IFV1239877878-1042
        // var id = 'IFV1239877878-104B_IFV1239877878-1042';
        element(by.id(userId)).click();
        loginButton.click();
    },
    loginByName: function (name) {
        element(by.cssContainingText('option', name)).click();
        loginButton.click();
    },
    loginByJSON: function(userJson) {
        jsonDisplay.clear().sendKeys(userJson);
        loginButton.click();
    }
};
