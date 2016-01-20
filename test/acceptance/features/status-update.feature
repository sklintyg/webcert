# language: sv
@soap @notReady
Egenskap: Statusuppdateringar skickas till vårdsystem med djupintegration

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att vårdsystemet skickat ett intygsutkast

Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag fyller i fältet "Min undersökning av patienten"
   Så är intygets status "DRAFT_INCOMPLETE"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet. Totalt: "1"

   Och när jag fyller i fältet "ICD-10"
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "1"

   Och när jag fyller i fältet "Arbetsförmåga"
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "2"

   Och när jag fyller i resten av de nödvändiga fälten.
   Så är intygets status "DRAFT_COMPLETE"

   Och signerar intyget
   Så är intygets status "SIGNED"

   Och när jag skickar intyget till Försäkringskassan
   Så är innehåller databasfältet "SKICKAD_TILL_MOTTAGARE" värdet "FK"
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"


Scenario: Statusuppdateringar då intyg makuleras
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."
   Och när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"

   Och när jag fyller i resten av de nödvändiga fälten.
   Så är intygets status "DRAFT_COMPLETE"

   Och signerar intyget
   Så är intygets status "SIGNED"

   Och när jag makulerar intyget
   Så ska statusuppdatering "HAN5" skickas till vårdsystemet. Totalt: "1"

Scenario: Statusuppdateringar då intyg raderas
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"
   Och när jag fyller i resten av de nödvändiga fälten.
   Så är intygets status "DRAFT_COMPLETE"
     
   Och när jag raderar intyget
   Så ska statusuppdatering "HAN4" skickas till vårdsystemet. Totalt: "1"

Scenario: Statusuppdateringar med fråga från FK
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."
   Och när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"

   Och när jag fyller i resten av de nödvändiga fälten.
   Så är intygets status "DRAFT_COMPLETE"

   Och signerar intyget
   Så är intygets status "SIGNED"

   Och när jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag svarar på frågan
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

Scenario: Statusuppdateringar med fråga till FK
   Givet att jag är inloggad som läkare
   Och jag går in på intygsutkastet via djupintegrationslänk
   Så ska intygsutkastets status vara "Utkastet är sparat, men obligatoriska uppgifter saknas."

   Och när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"
   Och när jag fyller i resten av de nödvändiga fälten.
   Så är intygets status "DRAFT_COMPLETE"

   Och signerar intyget
   Så är intygets status "SIGNED"

   Och när jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag fyller i en ny fråga till Försäkringskassan
   Och sedan klickar på skicka
   Så ska statusuppdatering "HAN8" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan skickar ett svar
   Så ska statusuppdatering "HAN7" skickas till vårdsystemet. Totalt: "1"
   
   Och när jag markerar frågan som hanterad
   Så ska statusuppdatering "HAN10" skickas till vårdsystemet. Totalt: "1"
   
