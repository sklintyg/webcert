/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/luseIntyg'

// LUSE = Läkarutlåtande för sjukersättning, FK 7800

describe('LUSE-intyg', function () {

    before(function() {
        cy.fixture('FK_intyg/maxLuseData').as('intygsdata');
        cy.fixture('vårdgivare/arnoldJohansson').as('vårdgivare');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLuseUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUSE-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en maximalt ifylld LUSE och skickar den till FK', function () {
        cy.loggaInVårdgivareIntegrerat(this);

        // Gå till intyget, redigera det, signera och skicka till FK
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

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionBakgrund(this.intygsdata.bakgrund);
        intyg.sektionFunktionsnedsättningar(this.intygsdata.funkNedsättningar);
        intyg.sektionAktivitetsbegränsningar(this.intygsdata.aktivitetsbegränsningar);
        intyg.sektionMedicinskBehandling(this.intygsdata.medicinskBehandling);
        intyg.sektionMedicinskaFörutsättningarFörArbete(this.intygsdata.medicinskaFörutsättningar);
        intyg.sektionÖvrigt(this.intygsdata.övrigt);
        intyg.sektionKontakt(this.intygsdata.kontakt);
        intyg.signera();
        intyg.skickaTillFk();
    });
});
