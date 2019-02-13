/* globals context cy */
/// <reference types="Cypress" />

describe('Välja patient', function () {

    before(function() {
        cy.fixture('testData').as('testData');
        cy.fixture('valjPatient').as('valjPatient');
    });

    beforeEach(function() {
        // Logga in som Arnold
        cy.loginArnoldOchGaTillValjPatient();
    });

    it('är möjligt att ange korrekt personnummer och komma vidare', function() {
        // Alias för "Fortsätt"-knappen
		cy.contains(this.valjPatient.fortsattKnappText).as('continueBtn');
		cy.get('@continueBtn').should('be.disabled');

        // Mata in patientens personnummer
		cy
			.get('input:first').should('have.attr', 'placeholder', this.valjPatient.placeholderText)
			.type(this.valjPatient.personnummer)
			.should('have.value', this.valjPatient.personnummer);

		// Verfiera att knappen för att gå vidare är aktiverad och klicka på den
		cy.get('@continueBtn').should('be.enabled').then(() => {
			cy.get('@continueBtn').click();
        });

        // Länk baseras på att det är Tolvan Tolvansson som angavs (kan enkelt brytas ut i fixture)
        cy.url().should('include', "#/create/choose-intyg-type/19121212-1212/index");
    });
});
