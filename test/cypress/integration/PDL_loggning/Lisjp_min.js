/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'

// LISJP = Läkarintyg för sjukpenning, FK 7804

describe('LISJP-intyg', function () {

    before(function() {
        cy.fixture('FK_intyg/minLisjpData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en minimalt ifylld LISJP', function () {
        cy.loggaInVårdpersonalIntegrerat(this);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        cy.visit(önskadUrl);

        // Om vi dirigeras till sidan som säger att 'Intygsutkastet är raderat'
        // så försöker vi igen eftersom det antagligen gick för snabbt.
        cy.get('body').then(($body) => {
            if ($body.text().includes('Intygsutkastet är raderat och kan därför inte längre visas.')) {
                cy.log("Kom till 'Intygetsutkastet är raderat', antagligen gick det för snabbt. Provar igen.");
                cy.loggaInVårdpersonalIntegrerat(this); // Vi behöver logga in igen
                cy.visit(önskadUrl);
            }
        });
        cy.url().should('include', this.utkastId);

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        //intyg.signera();
        //intyg.skickaTillFk();
    });
});
