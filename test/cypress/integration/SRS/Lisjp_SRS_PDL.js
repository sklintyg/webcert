/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/lisjpIntyg'
import * as pdl from '../../support/pdl_helpers'

// LISJP = Läkarintyg för sjukpenning, FK 7804

var pdlEventArray = [];

function lisjpPdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal.hsaId, assignment, env.vårdpersonal.titel, vgId_mod, vgNamn_mod, veId_mod, 
        veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
};

describe('LISJP-intyg med SRS', function () {

    before(function() {
        cy.fixture('FK_intyg/minLisjpData').as('intygsdata');
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

    it('skapar en minimalt ifylld LISJP med SRS-stöd', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i delar av intyget
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionSysselsättning(this.intygsdata.sysselsättning);
        intyg.sektionDiagnos(this.intygsdata.diagnos_SRS_nytt);

        // Vänta tills intyget har sparats så att PDL-händelsen "skriva" hinner registreras
        cy.contains("Utkastet är sparat").should('exist');

        // Klicka fram SRS-fliken, fyll i checkboxen för samtycke och klicka på Beräkna-knappen
        intyg.bytTillSrsPanel();
        intyg.srsPatientenSamtyckerChecked();
        intyg.srsKlickaBeräkna();
        // Beräkna-knappen genererar en PDL-händelse med optional argument
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Om läkaren inte håller med om patientens risk och därmed klickar på någon av radioknapparna så ska det
        // registreras som PDL-händelse
        cy.wait(2500);
        intyg.läkareAngerPatientrisk('Högre');
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, pdl.enumHandelseArgument.SRS_LÄKARES_ÅSIKT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Fyll i resten av intyget
        intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        intyg.sektionBedömning(this.intygsdata.bedömning);
        intyg.sektionÅtgärder(this.intygsdata.åtgärder);

        // Signerar intyget och populerar pdl-arrayen med förväntade logposter "Signera" och "Läsa"
        intyg.signera();
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Förnya intyget -> utkast skapas. Populerar pdl-arrayen med förväntade logposter "Skriva" och "Läsa" samt nytt intygsID.
        cy.url().should('include', this.utkastId);
        intyg.fornya();
        cy.contains("Smittbärarpenning"); // Vänta på att intyget ska laddas färdigt
        cy.get('.intygs-id').invoke('text').then((text1) => {
            var intygsID_2 = text1.replace(/\s/g, '');
            intygsID_2 = intygsID_2.substring(intygsID_2.length-36, intygsID_2.length);
            cy.log('IntygsID fönyande utkast: ' + intygsID_2);
            cy.wrap(intygsID_2).as('intygsID_2');
        });
        cy.get('@intygsID_2').then((intygID_2)=> {
            pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
            pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        });

        // Radera diagnoskod 1 och ersätt med ny
        intyg.raderaDiagnoskod(1);
        intyg.sektionDiagnos(this.intygsdata.diagnos_SRS_fornyat);
        cy.get('@intygsID_2').then((intygID_2)=> {
            pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        });

        // Verifiera i SRS-fliken att rätt diagnoser används i respektive "underflik",
        // d.v.s. "Råd och åtgärder", "Tidigare riskbedömning" och "Statistik"
        // Vilken diagnos som ska visas vart anges här:
        // https://inera.atlassian.net/wiki/spaces/IT/pages/9798635
        intyg.bytTillSrsPanel();
        intyg.verifieraDiagnosUnderRådOchÅtgärder(this.intygsdata.diagnos_SRS_fornyat.rad1.kod);
        intyg.verifieraDiagnosUnderTidigareRiskbedömning(this.intygsdata.diagnos_SRS_nytt.rad1.kod);
        intyg.verifieraDiagnosUnderStatistik(this.intygsdata.diagnos_SRS_fornyat.rad1.kod);

        // OBS!!! DENNA ÄR FEL! ÄR BARA HÄR FÖR ATT FÅ TESTFALLET ATT GÅ KLART!!!!
        // BUGG 1025
        // https://service.projectplace.com/#direct/card/10855900
        pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS_PREDIKTION, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));


        cy.verifieraPdlLoggar(pdlEventArray);
    });
});
