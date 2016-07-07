# language: sv
@paminnelse @lisu
Egenskap: Som läkare vill jag att påminnelser syns tydligt och leder mig till ursprungsfrågan

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning utökat"
   Och jag går in på intygsutkastet via djupintegrationslänk

@notReady
Scenario: Påminnelse
   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

   Och Försäkringskassan ställer en "Komplettering" fråga om intyget
   Och Försäkringskassan ställer en "Paminnelse" fråga om intyget
   Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "2"
   Och kan jag se påminnelse under hanterade frågor
