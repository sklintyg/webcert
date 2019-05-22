/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/TS_intyg/tsBasIntyg'

describe('TS-BAS-intyg', function () {
    
    before(function() {
        cy.fixture('TS_intyg/minTsBas').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    })

    beforeEach(function() {
        cy.skapaTsBasUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("TS-BAS-utkast med id " + utkastId + " skapat och används i testfallet");
        });
    });

    it('skapar en minimalt ifylld TS-Bas', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        cy.visit(önskadUrl);

        // Om vi dirigeras till sidan som säger att 'Intygsutkastet är raderat'
        // så försöker vi igen eftersom det antagligen gick för snabbt.
        cy.get('body').then(($body) => {
            if ($body.text().includes('Intygsutkastet är raderat och kan därför inte längre visas.')) {
                cy.log("Kom till 'Intygetsutkastet är raderat', antagligen gick det för snabbt. Provar igen.");
                cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet); // Vi behöver logga in igen
                cy.visit(önskadUrl);
            }
        });
        cy.url().should('include', this.utkastId);

        intyg.sektionIntygetAvser(this.intygsdata.intygetAvser);
        intyg.sektionIdentitet(this.intygsdata.identitet);
        intyg.sektionSynfunktioner(this.intygsdata.synfunktioner);
        intyg.sektionHörselBalans(this.intygsdata.hörselOchBalans, this.intygsdata.intygetAvser);
        intyg.sektionRörelseorganensFunktioner(this.intygsdata.rörelseorganensFunktioner, this.intygsdata.intygetAvser);
        intyg.sektionHjärtKärlSjukdomar(this.intygsdata.hjärtOchKärl);
        intyg.sektionDiabetes(this.intygsdata.diabetes);
        intyg.sektionNeurologiskaSjukdomar(this.intygsdata.neurologiskaSjukdomar);
        intyg.sektionEpilepsi(this.intygsdata.epilepsi);
        intyg.sektionNjurSjukdomar(this.intygsdata.njursjukdomar);
        intyg.sektionDemens(this.intygsdata.demens);
        intyg.sektionSömnOchVakenhetsStörningar(this.intygsdata.sömnOchVakenhetsStörningar);
        intyg.sektionAlkoholNarkotikaLäkemedel(this.intygsdata.alkoholNarkotika);
        intyg.sektionPsykiskaSjukdomarStörningar(this.intygsdata.psykiskaSjukdomar);
        intyg.sektionADHD(this.intygsdata.ADHD);
        intyg.sektionSjukhusvård(this.intygsdata.sjukhusvård);
        intyg.sektionÖvrigMedicinering(this.intygsdata.övrigMedicinering);
        intyg.sektionÖvrigKommentar(this.intygsdata.övrigKommentar);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.signera();
    });
});