describe('Sign Utkast', function() {

    describe('Login through the welcome page', function() {
        it('can select user IFV1239877878-104B_IFV1239877878-1042', function() {
            browser.get('welcome.jsp');

            // login id IFV1239877878-104B_IFV1239877878-1042
            var id = 'IFV1239877878-104B_IFV1239877878-1042';
            element(by.id(id)).click();
            element(by.id('loginBtn')).click();

        });

        it('wait for dashboard', function() {
            browser.sleep(500);
        });

        it('and make sure the correct doctor is logged in', function() {
            var doctor = element(by.css('.logged-in'));
            expect(doctor.getText()).toContain("Åsa Andersson");
        });
    });

    describe('create fk', function(){
        it('fill in person number and select', function() {
            var pnr = element(by.id('pnr'));
            pnr.sendKeys('191212121212');
            pnrButton = element(by.id('skapapersonnummerfortsatt'));
            pnrButton.click();

        });

        it('select fk intyg', function() {
            var value = 1;
            element(by.id('intygType')).all(by.css('option[value="' + value + '"]')).click();

            // click förtsätta
            element(by.id('intygTypeFortsatt')).click();

            var smittskydd = element(by.css('[key="fk7263.label.smittskydd"]'));

            browser.wait(smittskydd.isDisplayed()).then(function(){
                expect(smittskydd.getText()).toContain("Avstängning enligt smittskyddslagen på grund av smitta");
            });

        });

        describe('fill in fk intyg', function(){
            it('nedsatt form8b', function(){
                var smittskydd = element(by.id('smittskydd'));
                browser.wait(smittskydd.isDisplayed()).then(function(){
                    smittskydd.click();
                    var nedsattMed25 = element(by.id('nedsattMed25'));
                    nedsattMed25.click();
                    //var nedsattMed50 = element(by.id('nedsattMed50'));
                    //nedsattMed50.click();
                    //var nedsattMed75 = element(by.id('nedsattMed75'));
                    //nedsattMed75.click();
                    //var nedsattMed100 = element(by.id('nedsattMed100'));
                    //nedsattMed100.click();
                });

            });

            it('resor form 6a', function(){
                var travelRadioJa = element(by.id('rekommendationRessatt'));
                travelRadioJa.click();
                expect(element(by.css('input[name="recommendationsToFkTravel"]:checked')).getAttribute('value')).toBe('JA');
            })

            it('can sign', function(){
                var signeraButton = element(by.id('signera-utkast-button'));

                browser.wait(signeraButton.isEnabled()).then( function(){
                    signeraButton.click();
                    element(by.id('viewCertAndQA')).isDisplayed();
                });

            });

        });
    });

});