# language: sv
@soap @notReady
Egenskap: Statusuppdateringar skickas till vårdsystem med djupintegration

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att vårdsystemet skickat ett intygsutkast

@soap
Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."
   Och när jag fyller i fältet "Min undersökning av patienten"
   Så är intygets status "DRAFT_INCOMPLETE"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet. Totalt: "1"
   Och när jag fyller i fältet "ICD-10"
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "1"
   Och när jag fyller i fältet "Funktionsnedsättning"
   Och när jag fyller i fältet "Aktivitetsbegränsning"
   Och när jag fyller i fältet "Arbete"
   Och när jag fyller i fältet "Arbetsförmåga"
   Så är intygets status "DRAFT_COMPLETE"
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "2"
   Och signerar intyget
   Så är intygets status "SIGNED"
   Och när jag skickar intyget till Försäkringskassan
   Så är innehåller databasfältet "SKICKAD_TILL_MOTTAGARE" värdet "FK"
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"
