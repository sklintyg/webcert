/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../support/FK_intyg/luaeNaIntyg'
import * as pdl from '../../support/pdl_helpers'

// LUAE-NA = Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmånga, FK 7801

var pdlEventArray = [];

function luaeNaPdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal.hsaId, assignment, env.vårdpersonal.titel, vgId_mod, vgNamn_mod, veId_mod, 
        veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
};

describe('LUAE-NA-intyg PDL-loggning Ärende', function () {

    before(function() {
        cy.fixture('FK_intyg/minLuaeNaData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/tolvanTolvansson').as('vårdtagare');
    });

    beforeEach(function() {
        cy.skapaLuaeNaUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUAE-NA-utkast med id " + utkastId + " skapat och används i testfallet");
            pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        });
    });

    it('skapar en maximalt ifylld LUAE-NA och skickar den till FK, skickar fråga till FK och hanterar den', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        // Gå till intyget, redigera det, signera och skicka till FK
        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.sektionDiagnos(this.intygsdata.diagnos);
        intyg.sektionBakgrund(this.intygsdata.bakgrund);
        intyg.sektionFunktionsnedsättningar(this.intygsdata.funkNedsättningar);
        intyg.sektionAktivitetsbegränsningar(this.intygsdata.aktivitetsbegränsningar);
        intyg.sektionMedicinskaFörutsättningarFörArbete(this.intygsdata.medicinskaFörutsättningar);
        cy.wait(5000); // Finns inget bra element att leta efter för att se att intyget är sparat
        cy.contains("Utkastet är sparat").should('exist');
        intyg.signera();
        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.skickaTillFk();
        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.FKASSA, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.skapaAdmFragaLuaeNa();
        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.hanteraFragaLuaeNa();
        pdlEventArray.push(luaeNaPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        
        cy.verifieraPdlLoggar(pdlEventArray);
    });
});
