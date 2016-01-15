# language: sv
@soap @notReady
Egenskap: Statusuppdateringar skickas till vårdsystem med djupintegration

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att vårdsystemet skickat ett intygsutkast

@soap
Scenario: Ta emot en statusuppdatering
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."
   Och när jag fyller i fältet "Min undersökning av patienten"
   Så är intygets status "DRAFT_INCOMPLETE"
   Och när jag fyller i fältet "ICD-10"
   Så är intygets status "DRAFT_INCOMPLETE"
   Och när jag fyller i fältet "Funktionsnedsättning"
   Så är intygets status "DRAFT_INCOMPLETE"
   Och när jag fyller i fältet "Aktivitetsbegränsning"
   Så är intygets status "DRAFT_INCOMPLETE"
   Och när jag fyller i fältet "Arbete"
   Så är intygets status "DRAFT_INCOMPLETE"
   Och när jag fyller i fältet "Arbetsförmåga"
   Så är intygets status "DRAFT_COMPLETE"
   Och signerar intyget
   Så är intygets status "SIGNED"
   När jag går in på intygsutkastet via djupintegrationslänk
   Och när jag skickar intyget till Försäkringskassan
   Så är innehåller databasfältet "SKICKAD_TILL_MOTTAGARE" värdet "FK"
