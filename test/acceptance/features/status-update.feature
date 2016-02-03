# language: sv
@statusuppdateringar
Egenskap: Statusuppdateringar skickas till vårdsystem med djupintegration

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att vårdsystemet skickat ett intygsutkast
   Och att jag är inloggad som djupintegrerad läkare
   Och jag går in på intygsutkastet via djupintegrationslänk

@skicka-till-fk
Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   När när jag fyller i fältet "Min undersökning av patienten"
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet. Totalt: "1"

   Och när jag fyller i fältet "ICD-10"
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "1"

   Och när jag fyller i fältet "Arbetsförmåga"
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "2"

   Och när jag fyller i resten av de nödvändiga fälten.
   Och signerar intyget
   Så ska statusuppdatering "HAN2" skickas till vårdsystemet. Totalt: "1"

   Och när jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

@makulera
Scenario: Statusuppdateringar då intyg makuleras
   När när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"
   Och när jag fyller i resten av de nödvändiga fälten.
   Och signerar intyget

   Och när jag makulerar intyget
   Så ska statusuppdatering "HAN5" skickas till vårdsystemet. Totalt: "1"

@radera
Scenario: Statusuppdateringar då intyg raderas
   När när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"
   Och när jag fyller i resten av de nödvändiga fälten.
     
   Och när jag raderar intyget
   Så ska statusuppdatering "HAN4" skickas till vårdsystemet. Totalt: "1"

@fråga-från-fk
Scenario: Statusuppdateringar med fråga från FK
   När när jag fyller i fältet "Min undersökning av patienten"
   Och när jag fyller i fältet "ICD-10"
   Och när jag fyller i fältet "Arbetsförmåga"

   Och när jag fyller i resten av de nödvändiga fälten.
   Och signerar intyget

   Och när jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan ställer en fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och när jag svarar på frågan
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

@fråga-till-fk
Scenario: Statusuppdateringar med fråga till FK
   
   När jag fyller i alla nödvändiga fält för intyget
   Och signerar intyget

   Och när jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
   Så ska statusuppdatering "HAN8" skickas till vårdsystemet. Totalt: "1"

   Och när Försäkringskassan skickar ett svar
   Så ska statusuppdatering "HAN7" skickas till vårdsystemet. Totalt: "1"
   
   Och när jag markerar frågan som hanterad
   Så ska statusuppdatering "HAN10" skickas till vårdsystemet. Totalt: "1"
   
