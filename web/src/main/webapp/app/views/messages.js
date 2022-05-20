/*
 * Copyright (C) 2021 Inera AB (http://www.inera.se)
 *
 * This file is part of sklintyg (https://github.com/sklintyg).
 *
 * sklintyg is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sklintyg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/* jshint maxlen: false */
angular.module('webcert').constant('webcert.messages', {
  'sv': {
    'webcert.header': 'Webcert',
    'webcert.description': 'Välkommen till Webcert.',
    'dashboard.title': 'Mina andra enheter',
    'dashboard.unanswered.title': 'Ej hanterade ärenden',
    'dashboard.unanswered.subtitle': 'Nedan visas alla ej hanterade ärenden, så som kompletteringsbegäran och administrativa frågor, för den eller de enheter du väljer.',

    'dashboard.unsigned.title': 'Ej signerade utkast',
    'dashboard.unsigned.subtitle': 'Nedan visas alla ej signerade utkast för den enhet du är inloggad på.',
    'dashboard.about.title': 'Om Webcert',

      'dashboard.signed-certificates.title': 'Signerade intyg',
      'dashboard.signed-certificates.subtitle': 'Nedan visas dina signerade intyg för den enhet du är inloggad på.',

    //labels
    'label.default-intyg-type': 'Välj typ av intyg',

    'label.unsignedintyg': 'Ej signerade intyg',
    'label.unansweredintyg': 'Intyg med ej hanterad fråga',
    'label.readytosignintyg': 'Intyg färdiga att signera (massignering)',
    'label.showallintyg': 'Visa alla intyg',
    'label.showfewerintyg': 'Visa färre intyg',
    'label.patient': 'Patient:',
    'label.signselectedintyg': 'Signera valda intyg',

    'label.confirmaddress': 'Återanvänd uppgifter',
    'label.confirmsign': 'Signera intyget',
    'label.fornyaintyg': 'Förnya intyg',
    'label.ersattintyg': 'Ersätt intyg',

    'label.qaonlywarning': 'Du har valt att lämna frågor och svar',
    'label.qaonlywarning.body': '<p>Observera att intyg ska utfärdas via journalsystemet och inte via Webcert.</p><p>Information i Webcert som inte är frågor och svar kan inte visas i journalsystemet.</p>',
    'label.signed-certificates.signingdate.help': 'Av prestanda skäl är det är ej möjligt att välja datum längre än 3 månader bakåt i tiden.',

    // validation messages
    'validation.invalidfromdate': 'Från-datum är felaktigt. Använd formatet åååå-mm-dd.',
    'validation.invalidtodate': 'Till-datum är felaktigt. Använd formatet åååå-mm-dd.',
    'validation.invalidtobeforefromdate': 'Till-datum är före från-datum.',
    'validation.invalidpersonnummer': 'Ange ett giltigt person- eller samordningsnummer.',

    //info messages
    'info.nounsignedintygfound': '<strong>Inga ej signerade intyg hittades.</strong>',
    'info.nounsigned.intyg.for.unit': 'Det finns inga ej signerade utkast för den enhet du är inloggad på.',
    'info.nosigned.intyg.for.unit': 'Det finns inga signerade intyg de senaste 3 månaderna för den enhet du är inloggad på.',
    'info.nounansweredintygfound': '<strong>Inga intyg med ohanterade frågor hittades.</strong>',
    'info.noreadytosignintygfound': '<strong>Inga klarmarkerade intyg hittades.</strong>',
    'info.loadingintyg': '<strong>Laddar intyg...</strong>',
    'info.loadingdata': '<strong>Uppdaterar lista...</strong>',
    'info.nounanswered.arende.for.unit': 'Det finns inga ohanterade ärenden för den enhet eller de enheter du är inloggad på.',
    'info.nointygfound': 'Det finns inga tidigare intyg för patienten.',
    'info.query.error': '<strong>Sökningen kunde inte utföras.</strong>',
    'info.intygload.error': '<strong>Kunde inte hämta intyg.</strong>',
    'info.intygload.offline': '<strong>Intygstjänsten ej tillgänglig, endast Intyg utfärdade av Webcert visas.</strong>',
    'info.running.query': '<strong>Söker...</strong>',
    'info.querydraft.inprogress': 'Hämtar ej signerade utkast...',
    'info.intygstyp.replaced': '${oldIntygstyp} är ersatt av ${newIntygstyp}. För att gå vidare, välj det nya intyget i listan istället.',

    //error messages
    'error.unsignedintyg.couldnotbeloaded': '<strong>Kunde inte hämta ej signerade intyg.</strong>',
    'error.unansweredintyg.couldnotbeloaded': '<strong>Kunde inte hämta listan med ej hanterade ärenden.</strong>',
    'error.readytosignintyg.couldnotbeloaded': '<strong>Kunde inte hämta intyg klara för signering.</strong>',
    'error.failedtocreateintyg': 'Kunde inte skapa intyget. Försök igen senare.',
    'error.failedtomakuleraintyg': 'Kunde inte makulera intyget. Försök igen senare.',
    'error.failedtocopyintyg': 'Kunde inte kopiera intyget. Försök igen senare.',
    'error.failedtofornyaintyg': 'Kunde inte förnya intyget. Försök igen senare.',
    'error.failedtofornyaintyg.personidnotfound': 'Kunde inte förnya intyget. Det nya person-id:t kunde inte hittas.',
    'error.failedtofornyaintyg.replaced': 'Intyget kunde inte förnyas eftersom det har blivit ersatt av ett senare intyg.',
    'error.failedtofornyaintyg.complemented': 'Intyget kunde inte förnyas eftersom det har blivit kompletterat av ett senare intyg.',
    'error.failedtoersattintyg': 'Kunde inte ersätta intyget. Försök igen senare.',
    'error.failedtosendintyg': 'Kunde inte skicka intyget. Försök igen senare.',
    'error.pu.namenotfound': 'Personnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt.<br>Observera att det inte går att ange reservnummer. Webcert hanterar enbart person- och samordningsnummer.',
    'error.pu.samordningsnummernotfound': 'Samordningsnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt.<br>Observera att det inte går att ange reservnummer. Webcert hanterar enbart person- och samordningsnummer.',
    'error.pu.nopersonnummer': 'Det personnummer du har angett kunde tyvärr inte hämtas från folkbokföringsregistret.',
    'error.pu.noname': 'Förnamn eller efternamn för det personnummer du har angett kunde tyvärr inte hämtas från folkbokföringsregistret.',

    'error.pu.server-error': 'På grund av tekniskt fel gick det inte att hämta personuppgifter, försök igen om en liten stund. Om problemet kvarstår, kontakta i första hand din lokala IT-avdelning och i andra hand <LINK:ineraKundserviceAnmalFel>.',
    'error.pu_problem': '<p>Personuppgiftstjänsten svarar inte. Åtgärden kan inte genomföras eftersom den kräver att personuppgifter kan hämtas från personuppgiftsregistret. Prova igen om en stund.</p><p>Om problemet kvarstår, kontakta i första hand din lokala IT-avdelning och i andra hand <LINK:ineraKundserviceAnmalFel>.</p>',

    //Table headings
    //Tidigare intyg tabell
    'th.label.intyg-type': 'Typ av intyg',
    'th.help.intyg-type': 'Typ av intyg',
    'th.label.status': 'Status',
    'th.help.status': 'Visar utkastets/intygets status:<ul><li>Utkast, uppgifter saknas = Utkastet är sparat, men obligatoriska uppgifter saknas.</li><li>Utkast, kan signeras = Utkastet är komplett, sparat och kan signeras.</li><li>Utkast, låst = Utkastet är låst.</li><li>Signerat = Intyget är signerat.</li><li>Skickat = Intyget är skickat till mottagaren.</li><li>Makulerat = Intyget är makulerat.</li><li>Ersatt = Intyget är ersatt.</li><li>Kompletterat = Intyget är kompletterat.</li></ul>',
    'th.label.saved-date': 'Senast sparat',
    'th.help.saved-date': 'Datum och klockslag då utkastet senast sparades.',
    'th.label.saved-signed-by': 'Sparat/signerat av',
    'th.help.saved-signed-by': 'Person som senast sparade utkastet/intyget alternativt person som signerade intyget.',
    'th.no-result': 'Inga resultat att visa.',

    //Ej signerade utkast
    'th.label.draft-forwarded': 'Vidarebefordrad',
    'th.help.draft-forwarded': 'Visar om utkastet är vidarebefordrat.',
    'th.label.draft-type': 'Typ av intyg',
    'th.help.draft-type': 'Intygstyp',
    'th.label.draft-status': 'Status',
    'th.help.draft-status': 'Visar utkastets status:<ul><li>Utkast, uppgifter saknas = utkastet är sparat, men obligatoriska uppgifter saknas.</li><li>Utkast, kan signeras = utkastet är komplett, sparat och kan signeras.</li><li>Utkast, låst = Utkastet är låst.</li></ul>',
    'th.label.draft-saved-date': 'Senast sparat',
    'th.help.draft-saved-date': 'Datum och klockslag då utkastet senast sparades.',
    'th.label.draft-patient': 'Patient',
    'th.help.draft-patient': 'Patientens personnummer.',
    'th.label.draft-saved-by': 'Sparat av',
    'th.help.draft-saved-by': 'Person som senast sparade utkastet.',

    //Signed certificates
    'th.label.signed-certificate-type': 'Typ av intyg',
    'th.help.signed-certificate-type': 'Intygstyp',
    'th.label.signed-certificate-status': 'Status',
    'th.help.signed-certificate-status': 'Visar signerade intygets status:<ul><li>Skickat= intyget är signerat och skickat till mottagaren.</li><li>Ej skickat= intyget är signerat men inte skickat, intyget kan öppnas och skickas.</li>',
    'th.label.signed-certificate-signed-date': 'Signerad',
    'th.help.signed-certificate-signed-date': 'Datum och klockslag då intyget signerades.',
    'th.label.signed-certificate-patient': 'Patient',
    'th.help.signed-certificate-patient': 'Patientens personnummer.',

    //Fraga/Svar
    'arende.measure.svarfranvarden': 'Svara',
    'arende.measure.svarfranfk': 'Invänta svar',
    'arende.measure.komplettering': 'Komplettera',
    'arende.measure.markhandled.fk': 'Hanterat ärende',
    'arende.measure.markhandled.wc': 'Läs inkommet svar',
    'arende.measure.handled': 'Inget',

    'arende.fragestallare.fk': 'Försäkringskassan',
    'arende.fragestallare.wc': 'Vårdenheten',

    'th.label.arende-action': 'Åtgärd',
    'th.help.arende-action': 'Åtgärd som krävs för att frågan/svaret ska anses hanterad.',
    'th.label.arende-sender': 'Avsändare',
    'th.help.arende-sender': 'Vem som initierade frågan.',
    'th.label.arende-patient': 'Patient',
    'th.help.arende-patient': 'Patientens personnummer.',
    'th.label.arende-signed-by': 'Signerat av',
    'th.help.arende-signed-by': 'Läkare som signerat intyget.',
    'th.label.arende-sent-recv-date': 'Skickat/mottaget',
    'th.help.arende-sent-recv-date': 'Datum och klockslag för senaste händelse.',
    'th.label.arende-forwarded': 'Vidarebefordrad',
    'th.help.arende-forwarded': 'Visar om ärendet är vidarebefordrat.',
    'th.help.forward': 'Skapar ett e-postmeddelande i din e-postklient med en direktlänk till frågan/svaret.',
    'th.help.open': 'Öppnar intyget och frågan/svaret.',

    //DOI Dialog
    'doi.label.titleText': 'Dödsbevis saknas',
    'doi.label.bodyText': 'Är du säker att du vill skapa ett dödsorsaksintyg? Det finns inget dödsbevis i nuläget inom vårdgivaren.<br><br> Dödsorsaksintyget bör alltid skapas efter dödsbeviset.',
    'doi.label.button1text': 'Skapa dödsorsaksintyg',
    //DB Dialog
    'db.label.titleText': 'Kontrollera namn och personnummer',
    'db.label.info': 'Du är på väg att utfärda ett dödsbevis för ${fornamn} ${efternamn} ${personnummer}.',
    'db.label.bodyText': 'När dödsbeviset signeras, skickas det samtidigt till Skatteverket och dödsfallet registreras.<br>Ett dödsbevis utfärdat på en person får stora konsekvenser för den enskilda personen.<br>Kontrollera därför en extra gång att personuppgifterna stämmer.',
    'db.draft.label.bodyText': 'När dödsbeviset signeras, skickas det samtidigt till Skatteverket och dödsfallet registreras.<br>Ett dödsbevis utfärdat på en person får stora konsekvenser för den enskilda personen.<br>Kontrollera därför en extra gång att personuppgifterna stämmer.<br><br><b>Obs! Om fel personuppgifter visas ovan, välj radera.</b>',
    'db.label.button1text': 'Gå vidare',
    'db.label.checkbox.text': 'Jag har kontrollerat att uppgifterna stämmer.'
  },
  'en': {
    'webcert.header': 'Webcert Application (en)'
  }
});
