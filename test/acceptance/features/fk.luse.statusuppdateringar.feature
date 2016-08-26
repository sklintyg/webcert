# language: sv
@statusuppdateringar @luse
Egenskap: Statusuppdateringar för LUSE

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"
   Och att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning"
   Och jag går in på intygsutkastet via djupintegrationslänk

@skicka-till-fk
Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"

   När jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

@makulera
Scenario: Statusuppdateringar då intyg makuleras
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    När jag makulerar intyget
    Så ska statusuppdatering "MAKULE" skickas till vårdsystemet. Totalt: "1"

@radera
Scenario: Statusuppdateringar då intyg raderas
    När jag fyller i alla nödvändiga fält för intyget
    Och jag raderar intyget
    Så ska statusuppdatering "RADERA" skickas till vårdsystemet. Totalt: "1"

@fråga-från-fk
Scenario: Statusuppdateringar vid fråga från FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

    När Försäkringskassan skickar ett "Kontakt" meddelande på intyget
    Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "1"

    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag svarar på frågan
    Så ska statusuppdatering "HANFRA" skickas till vårdsystemet. Totalt: "1"

@fråga-till-fk
Scenario: Statusuppdateringar vid fråga till FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"

    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    Så ska statusuppdatering "NYFRTM" skickas till vårdsystemet. Totalt: "1"

    Och Försäkringskassan skickar ett svar
    Så ska statusuppdatering "NYSVFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 0

    Och jag markerar svaret från Försäkringskassan som hanterat
    Så ska statusuppdatering "HANSVA" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 1

@ANDRAT
Scenario: Statusuppdateringar vid ändring av utkast
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

    När jag ändrar i fältet
    Så ska statusuppdatering "ANDRAT" skickas till vårdsystemet. Totalt: "1"
