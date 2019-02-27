/* globals context cy */
/// <reference types="Cypress" />

describe('Välja patient', function () {

    before(function() {
        cy.fixture('arnoldJohansson').as('vårdgivare');
        cy.fixture('alfaEnheten').as('vårdenhet');
    });

    beforeEach(function() {
        cy.loggaInVårdgivareNormal(this).then(() => {
            cy.visit('/#/create/choose-patient/index');
        })
    });

    it('är möjligt att ange korrekt personnummer och komma vidare', function() {
        // Alias för "Fortsätt"-knappen
		cy.contains("Fortsätt").as('continueBtn');
		cy.get('@continueBtn').should('be.disabled');

        // Mata in patientens personnummer
        cy
        .get('input:first').should('have.attr', 'placeholder', "ååååmmdd-nnnn")
		.type("19121212-1212")
		.should('have.value', "19121212-1212");

		// Verfiera att knappen för att gå vidare är aktiverad och klicka på den
		cy.get('@continueBtn').should('be.enabled').then(() => {
			cy.get('@continueBtn').click();
        });

        // Länk baseras på att det är Tolvan Tolvansson som angavs (kan enkelt brytas ut i fixture)
        cy.url().should('include', "#/create/choose-intyg-type/19121212-1212/index");
    });
});
