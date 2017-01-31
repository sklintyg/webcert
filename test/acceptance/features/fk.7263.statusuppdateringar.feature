# language: sv
@statusuppdateringar @fk7263
Egenskap: Statusuppdateringar för FK7263

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg FK 7263"
   Och jag går in på intygsutkastet via djupintegrationslänk

@skicka-till-fk @HAN11
Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet. Totalt: "1"

   När jag ändrar i fältet diagnoskod
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "1"

   När jag ändrar i fältet arbetsförmåga
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "2"

   När jag ändrar i fältet sjukskrivningsperiod
   Så ska statusuppdatering "HAN11" skickas till vårdsystemet. Totalt: "3"

   Och jag fyller i resten av de nödvändiga fälten.
   Och jag signerar intyget
   Så ska statusuppdatering "HAN2" skickas till vårdsystemet. Totalt: "1"

   Och jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

@makulera
Scenario: Statusuppdateringar då intyg makuleras
   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

   Och jag makulerar intyget
   Så ska statusuppdatering "HAN5" skickas till vårdsystemet. Totalt: "1"

@radera
Scenario: Statusuppdateringar då intyg raderas
   När jag fyller i alla nödvändiga fält för intyget
   Och jag raderar intyget
   Så ska statusuppdatering "HAN4" skickas till vårdsystemet. Totalt: "1"

@fråga-från-fk
Scenario: Statusuppdateringar vid fråga från FK
   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget

   Och jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och Försäkringskassan ställer en "Kontakt" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"
   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag svarar på frågan
   Så ska statusuppdatering "HAN9" skickas till vårdsystemet. Totalt: "1"

@fråga-till-fk
Scenario: Statusuppdateringar vid fråga till FK
   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Så ska ett info-meddelande visa "Det går därför inte att ställa frågor om intyget."
   Och jag skickar intyget till Försäkringskassan

   Så ska statusuppdatering "HAN3" skickas till vårdsystemet. Totalt: "1"

   Och jag går in på intygsutkastet via djupintegrationslänk
   Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
   Så ska statusuppdatering "HAN8" skickas till vårdsystemet. Totalt: "1"

   Och Försäkringskassan skickar ett svar
   Så ska statusuppdatering "HAN7" skickas till vårdsystemet. Totalt: "1"
   
   Och jag markerar svaret från Försäkringskassan som hanterat
   Så ska statusuppdatering "HAN10" skickas till vårdsystemet. Totalt: "1"
   
@komplettering
Scenario: Statusuppdateringar vid komplettering med nytt intyg

   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Och jag skickar intyget till Försäkringskassan

   Och Försäkringskassan ställer en "Komplettering_av_lakarintyg" fråga om intyget
   Så ska statusuppdatering "HAN6" skickas till vårdsystemet. Totalt: "1"

   När jag går in på intygsutkastet via djupintegrationslänk
   Och jag väljer att svara med ett nytt intyg
   Så ska jag se kompletteringsfrågan på utkast-sidan

   När jag signerar och skickar kompletteringen
   Så ska statusuppdatering "HAN1" skickas till vårdsystemet. Totalt: "1"
   Och ska intygets status vara "Intyget är signerat"
   Och ska 1 statusuppdatering "HAN9" skickas för det ursprungliga intyget
