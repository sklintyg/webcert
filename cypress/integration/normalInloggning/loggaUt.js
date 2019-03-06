/* globals context cy */
/// <reference types="Cypress" />

describe('Logga ut vårdgivare', function () {

    before(function() {
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
    });

    it('är möjligt att logga ut vårdgivare', function() {
        cy.loggaInVårdgivareNormal(this);
        cy.visit('/#/create/choose-patient/index');

        // Cookie-banner är ivägen för utloggning, den måste godkännas först
        const cookiebannerText = "Vi använder kakor (cookies)";
        cy.contains(cookiebannerText);
        cy.get("#cookie-usage-consent-btn").click();
        cy.contains(cookiebannerText).should('not.exist');

        // Klicka på "Logga ut" och verifiera att vi dirigeras till rätt sida
        cy.get("#logoutLink").click();
        cy.url().should('include', "/welcome.html");
    });
});
