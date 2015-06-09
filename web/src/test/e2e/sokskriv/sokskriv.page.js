/**
 * Created by stephenwhite on 09/06/15.
 */
var SokSkrivPage = function() {
    var nameInput = element(by.model('yourName'));
    var greeting = element(by.binding('yourName'));

    this.get = function() {
        browser.get('http://www.angularjs.org');
    };

    this.setName = function(name) {
        nameInput.sendKeys(name);
    };

    this.getGreeting = function() {
        return greeting.getText();
    };
};
