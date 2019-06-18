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

        // Klicka fram SRS-fliken och klicka på JA till samtycke
        cy.get('#tab-link-wc-srs-panel-tab').click();
        cy.contains("Patienten samtycker till att delta i SRS pilotprojekt").parent().within(() => {
            cy.get('[type="radio"]').check(['JA']);
        });

        // Hämta ut elementet som innehåller alla frågor och "Beräkna"-knappen, klicka på "Beräkna".
        cy.get('#questionaire').within(() => {
            cy.get('button').click();
            // Beräkna-knappen genererar en PDL-händelse med optional argument
            // TODO: Just nu skickas inte intygsid med vilket gör att mocken inte får händelsen till rätt URL
            // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

            // Verifiera att knappen inte går att trycka på.
            cy.get('button').should('be.disabled');
        });

        // ToDo: Fortsätt med PDL-testfallet här! Kommentarerna i början indikerar vad testfallet ska göra,
        // det som kommer under är bara kopierat från LISJP-testfallet och ska kanske tas bort.
        // // Ändra något svar på frågorna, klicka på "Beräkna" igen
        // // Verifiera PDL-händelse
        // cy.contains("Född i Sv").parent().within(() => {
        //     // ToDo: Här borde vi leta efter radioknapp med Value "Nej" men
        //     // värdet: value="[object Object]" är inte bra
        //     // cy.get('[type="radio"]')[1].check();
        // });

        // cy.get('#questionaire').within(() => {
        //     cy.get('button').click();
        //     // Beräkna-knappen genererar en PDL-händelse med optional argument
        //     // TODO: Just nu skickas inte intygsid med vilket gör att mocken inte får händelsen till rätt URL
        //     // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.SRS, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        //     // Verifiera att knappen inte går att trycka på.
        //     cy.get('button').should('be.disabled');
        // });

        // Logga ut, logga tillbaka in.
        // Verifiera att "Beräkna" inte är enabled
        // Justera något svar och tryck på beräkna
        // Verifiera PDL-händelse

        // // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session
        // cy.clearCookies();
        // cy.visit('/logout');
        // cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        // cy.visit(önskadUrl);
        // cy.url().should('include', this.utkastId);

        // // Populerar pdl-array med förväntade logposter "Läsa" och "Skriva" samt fyller i resten av intyget
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // intyg.sektionSjukdomensKonsekvenserFörPatienten(this.intygsdata.sjukdomensKonsekvenserFörPatienten);
        // intyg.sektionBedömning(this.intygsdata.bedömning);
        // intyg.sektionÅtgärder(this.intygsdata.åtgärder);
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // // Signerar intyget och populerar pdl-arrayen med förväntade logposter "Signera" och "Läsa"
        // intyg.signera();
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SIGNERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // // Skickar intyget till FK samt populerar pdl-arrayen med förväntad logpost "Utskrift" med argument att det är skickat till FK
        // intyg.skickaTillFk();
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.FKASSA, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // // Introducerar en wait då skrivUt går så fort att man riskerar att få samma timestamp som för "skicka"
        // cy.wait(1500);

        // // Skriver ut intyget samt populerar pdl-arrayen med förväntad logpost "Utskrift"
        // intyg.skrivUt("fullständigt", this.utkastId);
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFT, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // cy.log("Testar SJF");

        // // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session och SJF (Sammanhållen journalföring)
        // cy.clearCookies();
        // cy.visit('/logout');
        // cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet_2);

        // const sjfUrl = "/visa/intyg/" + this.utkastId + "?enhet=" + this.vårdenhet_2.id + "&sjf=true";
        // cy.visit(sjfUrl);

        // // Om vi inte väntar på (valfritt) elementet nedan i intyget
        // // så kommer "utskrift" att inträffa före "läsa"
        // cy.contains("Smittbärarpenning");

        // cy.url().should('include', this.utkastId);
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, pdl.enumHandelseArgument.LÄSASJF, this.utkastId, this.vårdenhet_2.uppdragsnamn, this.vårdenhet_2.vårdgivareId, this.vårdenhet_2.vårdgivareNamn, this.vårdenhet_2.id, this.vårdenhet_2.namn));
        // cy.log(this.utkastId + this.vårdenhet_2.vårdgivareId + this.vårdenhet_2.vårdgivareNamn + this.vårdenhet_2.id + this.vårdenhet_2.namn);

        // cy.log("Testar återigen utan SJF");

        // // Lite special logga ut/logga in -variant för att sedan öppna intyget på nytt med en ny session
        // cy.clearCookies();
        // cy.visit('/logout');
        // cy.loggaInVårdpersonalIntegrerat(this.vårdpersonal, this.vårdenhet);
        // cy.visit(önskadUrl);
        // cy.contains("Smittbärarpenning"); // Vänta på att intyget ska laddas färdigt
        // pdlEventArray.push(lisjpPdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // // ToDo: Bug?! Varför blir det 2 "Läsa" på rad?
        // pdlEventArray.push(lisjpPdlEvent(this, "Läsa", undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // // Förnya intyget -> utkast skapas. Populerar pdl-arrayen med förväntade logposter "Skriva" och "Läsa" samt nytt intygsID.
        // cy.url().should('include', this.utkastId);
        // intyg.fornya();
        // cy.contains("Smittbärarpenning"); // Vänta på att intyget ska laddas färdigt
        // cy.get('.intygs-id').invoke('text').then((text1) => {
        //     var intygsID_2 = text1.replace(/\s/g, '');
        //     intygsID_2 = intygsID_2.substring(intygsID_2.length-36, intygsID_2.length);
        //     cy.log('IntygsID fönyande utkast: ' + intygsID_2);
        //     cy.wrap(intygsID_2).as('intygsID_2');
        // });
        // cy.get('@intygsID_2').then((intygID_2)=> {
        //     pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.SKRIVA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        //     pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // });

        // // Skriva ut utkast. Populerar PDL-arrayen med förväntad logpost "Utskrift" 
        // cy.get('@intygsID_2').then((intygID_2)=> {
        //     intyg.skrivUt("fullständigt", intygID_2);
        //     pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.UTSKRIFT, pdl.enumHandelseArgument.UTSKRIFTUTKAST, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // });

        // // Raderar intygsutkast
        // intyg.raderaUtkast();
        // cy.get('@intygsID_2').then((intygID_2)=> {
        //     pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.RADERA, undefined, intygID_2, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));
        // });

        // // Redirect till originalintyget genererar en ny "Läsa"
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.LÄSA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        // // Makulerar ursprungsintyget
        // // intyg.makuleraIntyg('Annat allvarligt fel');
        // intyg.makuleraIntyg();
        // pdlEventArray.push(lisjpPdlEvent(this, pdl.enumHandelse.MAKULERA, undefined, this.utkastId, this.vårdenhet.uppdragsnamn, this.vårdenhet.vårdgivareId, this.vårdenhet.vårdgivareNamn, this.vårdenhet.id, this.vårdenhet.namn));

        cy.verifieraPdlLoggar(pdlEventArray);
    });
});
