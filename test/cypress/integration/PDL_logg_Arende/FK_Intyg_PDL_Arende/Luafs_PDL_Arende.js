/* globals context cy */
/// <reference types="Cypress" />
import * as intyg from '../../../support/FK_intyg/luaeFsIntyg'
import * as pdl from '../../../support/pdl_helpers'

// LUAE-FS = Läkarutlåtande för aktivitetsersättning vid förlängd skolgång, FK 7802

var pdlEventArray = [];

function luaeFsPdlEvent(env, actType, actArgs, actLevel, assignment, vgId_mod, vgNamn_mod, veId_mod, veNamn_mod) {
    return pdl.pdlEvent(env, actType, actArgs, actLevel, env.vårdpersonal.hsaId, assignment, env.vårdpersonal.titel, vgId_mod, vgNamn_mod, veId_mod, 
        veNamn_mod, env.vårdtagare.personnummerKompakt, env.vårdenhet.vårdgivareId, env.vårdenhet.vårdgivareNamn, env.vårdenhet.id, env.vårdenhet.namn)   
};

describe('LUAE-FS-intyg PDL-loggning Ärende', function () {
    
    before(function() {
        cy.fixture('FK_intyg/minLuaeFsData').as('intygsdata');
        cy.fixture('vårdpersonal/arnoldJohansson').as('vårdpersonal');
        cy.fixture('vårdenheter/alfaEnheten').as('vårdenhet');
        cy.fixture('vårdenheter/nmt_vg1_ve1').as('vårdenhet_2');
        cy.fixture('vårdtagare/balanarNattjagare').as('vårdtagare');
    })

    beforeEach(function() {
        pdlEventArray = [];
        cy.skapaLuaeFsUtkast(this).then((utkastId) => {
            cy.wrap(utkastId).as('utkastId');
            cy.log("LUAE-FS-utkast med id " + utkastId + " skapat och används i testfallet");
            pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn)); 
        });
    });

    it('skapar ett minimalt ifylld LUAE-FS och skickar sedan fråga på intyget innan frågan hanteras', function () {
        cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);

        const önskadUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet.id
        intyg.besökÖnskadUrl(önskadUrl, this.vårdpersonal, this.vårdenhet, this.utkastId);

        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.sektionGrundFörMedicinsktUnderlag(this.intygsdata.grundFörMedicinsktUnderlag);
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.sektionDiagnos(this.intygsdata.diagnos);
        cy.wait(5000); // Finns inget bra element att leta efter för att se att intyget är sparat
        cy.contains("Utkastet är sparat").should('exist');

        intyg.loggaUtLoggaIn(this.vårdpersonal, this.vårdenhet);
        cy.visit(önskadUrl);
        cy.url().should('include', this.utkastId);
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        //cy.get('.intygs-id').should('be.visible');
        intyg.sektionFunktionsnedsättning(this.intygsdata.funkNedsättning);
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.signera();
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        intyg.skickaTillFk();
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.FKASSA, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // Introducerar en wait då skrivUt går så fort att man riskerar att få samma timestamp som för "skicka"
        cy.wait(1500);
        intyg.skapaAdmFragaLuaeFs();
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        intyg.hanteraFragaLuaeFs();
        pdlEventArray.push(luaeFsPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        

     
        cy.verifieraPdlLoggar(pdlEventArray);
    });
});
