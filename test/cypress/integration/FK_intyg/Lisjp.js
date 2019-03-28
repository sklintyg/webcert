/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

// LISJP = Läkarintyg för sjukpenning, FK 7804

describe('LISJP-intyg', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLisjpData').as('intygsdata');
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LISJP och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        cy.visit(önskadUrl);

        // Om vi dirigeras till sidan som säger att 'Intygsutkastet är raderat'
        // så försöker vi igen eftersom det antagligen gick för snabbt.
        cy.get('body').then(($body) => {
            if ($body.text().includes('Intygsutkastet är raderat och kan därför inte längre visas.')) {
                cy.log("Kom till 'Intygetsutkastet är raderat', antagligen gick det för snabbt. Provar igen.");
                cy.loggaInVårdgivareIntegrerat(this); // Vi behöver logga in igen
                cy.visit(önskadUrl);
            }
        });
        cy.url().should('include', this.utkastId);
        cy.log("Nu har vi loggat in, skapat upp draft och navigerat till intyget för att fylla i det"); // TODO: Ta bort

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        intyg.sektionÖvrigaUpplysningar(this.intygsdata.övrigaUpplysningar);
        intyg.sektionKontakt(this.intygsdata.kontakt);

        // Signera intyget
        cy.contains("Klart att signera");
        cy.contains("Obligatoriska uppgifter saknas").should('not.exist');
        cy.contains("Utkastet sparas").should('not.exist');

        // cy.click() fungerar inte alltid. Det finns ärenden rapporterade
        // (stängd pga inaktivitet):
        // https://github.com/cypress-io/cypress/issues/2551
        // https://www.cypress.io/blog/2019/01/22/when-can-the-test-click/ :
        // "If a tree falls in the forest and no one has attached a “fall” event listener, did it really fall?"

        const click = $el => { return $el.click() }

        // Parent() p.g.a. att ett element täcker knappen
        cy.get('#signera-utkast-button').parent().should('be.visible')

        cy.get('#signera-utkast-button')
        .pipe(click, {timeout: 60000}) // ToDo: Lång timeout (problem endast på Jenkins, överlastad slav?)
        .should($el => {
            expect($el.parent()).to.not.be.visible;
        })

        // Välj intygsmottagare
        // TODO: Ger en utökad timeout då modalen i perioder inte hinner laddas. Detta bör ses över
        cy.get('#approve-receiver-SKANDIA-radio-no', {timeout: 20000}).check();
        cy.get('#save-approval-settings-btn').click();

        // Skicka iväg intyget
        cy.get("#sendBtn", { timeout: 60000 }).click();
        cy.get("#button1send-dialog").click(); // Modal som dyker upp och frågar om man verkligen vill skicka
        cy.contains("Intyget är skickat till Försäkringskassan");
    });
});
