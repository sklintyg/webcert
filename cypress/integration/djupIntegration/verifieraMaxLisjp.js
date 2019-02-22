/* globals context cy */
/// <reference types="Cypress" />

describe('skapa och signera maxifyllt LISJP och skicka till FK', function () {

    before(function() {
        cy.fixture('testData').as('testData');
        // Nedanstående är direkt kopierade från normalInloggningstestfallet
		cy.fixture('valjPatient').as('valjPatient');
		cy.fixture('valjIntyg').as('valjIntyg');
		cy.fixture('lisjpData').as('lisjpData');

    });

    beforeEach(function() {
        // Skapa intygsdraft
        cy.createLisjpDraftNonGeneric().then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("utkastId i beforeEach(): " + utkastId);
        });

        // Nedanstående är direkt kopierade från normalInloggningstestfallet
        // Flytta dessa ner i testfallet och gör om dem till konstanter istället?
		const today = Cypress.moment().format('YYYY-MM-DD');
		cy.wrap(Cypress.moment().add(1,  'days').format('YYYY-MM-DD')).as('todayPlus1');  // 25%  sjukskrivning start
		cy.wrap(Cypress.moment().add(11, 'days').format('YYYY-MM-DD')).as('todayPlus11'); // 25%  sjukskrivning slut
		cy.wrap(Cypress.moment().add(12, 'days').format('YYYY-MM-DD')).as('todayPlus12'); // 50%  sjukskrivning start
		cy.wrap(Cypress.moment().add(19, 'days').format('YYYY-MM-DD')).as('todayPlus19'); // 50%  sjukskrivning slut
		cy.wrap(Cypress.moment().add(20, 'days').format('YYYY-MM-DD')).as('todayPlus20'); // 75%  sjukskrivning start
		cy.wrap(Cypress.moment().add(28, 'days').format('YYYY-MM-DD')).as('todayPlus28'); // 75%  sjukskrivning slut
		cy.wrap(Cypress.moment().add(29, 'days').format('YYYY-MM-DD')).as('todayPlus29'); // 100% sjukskrivning start
		cy.wrap(Cypress.moment().add(41, 'days').format('YYYY-MM-DD')).as('todayPlus41'); // 100% sjukskrivning slut

		cy.wrap(Cypress.moment().subtract(5,  'days').format('YYYY-MM-DD')).as('todayMinus5');  // Patient examination date
		cy.wrap(Cypress.moment().subtract(6,  'days').format('YYYY-MM-DD')).as('todayMinus6');  // Date of phone contact with patient
		cy.wrap(Cypress.moment().subtract(15, 'days').format('YYYY-MM-DD')).as('todayMinus15'); // Journal entry date
		cy.wrap(Cypress.moment().subtract(14, 'days').format('YYYY-MM-DD')).as('todayMinus14'); // Midwife's assessment date
    });

    it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
        // Logga in läkare
        cy.loginArnoldDeep(); // Prova att INTE logga in som Arnold också

        // Redigera intygsdraft, signer och skicka
        cy.log("Efter then(), UtkastId är " + this.utkastId);
        cy.visit("/visa/intyg/" + this.utkastId + "?enhet=TSTNMT2321000156-1077").fillOutMaxLisjpForTolvanTolvansson(this);
    });

});