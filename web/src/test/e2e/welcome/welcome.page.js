/**
 * Created by stephenwhite on 09/06/15.
 */
var SokSkrivPage = function() {
    var loginButton = element(by.id('loginBtn'));

    this.get = function() {
        browser.get('welcome.jsp');
    };

    this.login = function(userId){
        // login id IFV1239877878-104B_IFV1239877878-1042
        // var id = 'IFV1239877878-104B_IFV1239877878-1042';
        element(by.id(userÍd)).click();
        element(loginButton.click());
    }

};
