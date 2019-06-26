/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/FK_intyg/lisjpIntyg'
import * as pdl from '../../../support/pdl_helpers'

// LISJP = Läkarintyg för sjukpenning, FK 7804

var pdlEventArray = [];

function lisjpPdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal.hsaId, assignment, env.vårdpersonal.titel, vgId_mod, vgNamn_mod, veId_mod, 
        veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
};

describe('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/minLisjpDataSrs').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })

    beforeEach(function() {
        pdlEventArray = [];
        cy.skapaLisjpUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LISJP-utkast med id " + utkastId + " skapat och används i testfallet");
            pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it.skip('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i halva intyget
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.contains("Utkastet är sparat").should('exist');
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Klicka fram SRS-fliken och fyll i checkboxen för samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilot").parent().within(() => {
            cy.get('[type="checkbox"]').check();
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questions').within(() => {
            cy.get('button').click();

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // Beräkna-knappen genererar en PDL-händelse med optional argument. Om denna rad ligger inuti "within" ovan så köas den så att den faktiskt
        // hamnar efter kommande pdlEvenArray.push
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det registreras
        // som PDL-händelse
        cy.wait(2500);
        cy.get('#risk-opinion-higher').check();
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // ToDo: PDL-testfallet kan byggas ut.
        // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // Verifiera PDL-händelse

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // För övriga idéer, titta på PDL-testfallet för "vanlig" LISJP

        cy.verifieraPdlLoggar(pdlEventArray);
    });
});
