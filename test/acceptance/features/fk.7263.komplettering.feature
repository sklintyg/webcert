# language: sv
@komplettera @fk7263
Egenskap: Komplettering av intyg

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare
   Och att vårdsystemet skapat ett intygsutkast
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

Scenario: Svara med nytt intyg
   När Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag väljer att svara med ett nytt intyg
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan
   Så ska 1 statusuppdatering "HAN9" skickas för det ursprungliga intyget
   Så ska intygets status vara "Intyget är signerat"	
