/* globals context cy */
/// <reference types="Cypress" />

describe('Välja patient', function () {

    before(function() {
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.loggaInVårdgivareNormal(this).then(() => {
            cy.visit('/#/create/choose-patient/index');
        })
    });

    it('är möjligt att ange korrekt personnummer och komma vidare', function() {
		cy.get('#skapapersonnummerfortsatt').should('be.disabled');

        // Mata in patientens personnummer
        cy
        .get('input:first').should('have.attr', 'placeholder', "ååååmmdd-nnnn")
		.type(this.vårdtagare.personnummer)
		.should('have.value', this.vårdtagare.personnummer);

        cy.get('#skapapersonnummerfortsatt').should('be.enabled').click();

        cy.url().should('include', "#/create/choose-intyg-type/" + this.vårdtagare.personnummer + "/index");
    });
});
