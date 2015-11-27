/**
 * Created by stephenwhite on 09/06/15.
 */

var loginButton = element(by.id('loginBtn'));

module.exports = {
    get: function() {
        browser.get('welcome.jsp');
    },
    login: function(userId){
        // login id IFV1239877878-104B_IFV1239877878-1042
        // var id = 'IFV1239877878-104B_IFV1239877878-1042';
        element(by.id(userId)).click();
        loginButton.click();
    }
};
