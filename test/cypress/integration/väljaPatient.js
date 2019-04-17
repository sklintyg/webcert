/* globals context cy */
/// <reference types="Cypress" />

describe('Välja patient', function () {

    beforeEach(function() {
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    it('är möjligt att ange korrekt personnummer', function() {

        /*
            Inloggningen borde vara i beforeEach() men det blir problem då alla
            fixtures måste läsas in i beforeEach() (för alla alias som skapas
            i before() rensas efter första testfallet i sviten).
        */
        cy.loggaInVårdpersonalNormal(this).then(() => {
            cy.visit('/#/create/choose-patient/index');
        })

		cy.get('#skapapersonnummerfortsatt').should('be.disabled');

        // Mata in patientens personnummer
        cy
        .get('input:first').should('have.attr', 'placeholder', "ååååmmdd-nnnn")
		.type(this.vårdtagare.personnummer).should('have.value', this.vårdtagare.personnummer);

        cy.get('#skapapersonnummerfortsatt').should('be.enabled').click();

        cy.url().should('include', "#/create/choose-intyg-type/" + this.vårdtagare.personnummer + "/index");
    });

    it('är inte möjligt att gå vidare utan fullständigt personnummer', function() {

        // Bör egentligen ske i beforeEach(). Se kommentar i översta testfallet.
        cy.loggaInVårdpersonalNormal(this).then(() => {
            cy.visit('/#/create/choose-patient/index');
        })

        cy.get('#skapapersonnummerfortsatt').should('be.disabled');

        // Mata in ett ofullständigt personnummer
        const ofullständigtPersNum = "19121212";
        cy
        .get('input:first').should('have.attr', 'placeholder', "ååååmmdd-nnnn")
		.type(ofullständigtPersNum)
        .should('have.value', ofullständigtPersNum + "-");

        // Klicka utanför textrutan för att släppa fokus
        cy.get('#skapa-valj-patient').click({force: true});
        cy.contains('Ange ett giltigt person- eller samordningsnummer.');

        cy.get('#skapapersonnummerfortsatt').should('be.disabled');
    });

    it('är möjligt att logga ut vårdpersonal', function() {
        cy.loggaInVårdpersonalNormal(this);
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
