# language: sv
@statusuppdateringar @lisu @notReady
Egenskap: Statusuppdateringar för LISU

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"
   Och att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning utökat"
   Och jag går in på intygsutkastet via djupintegrationslänk

@skicka-till-fk
Scenario: Statusuppdateringar då intyg skickas till Försäkringskassan
   Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

   När jag fyller i alla nödvändiga fält för intyget
   Och jag signerar intyget
   Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"

   När jag skickar intyget till Försäkringskassan
   Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"
   Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 0, hanterade svar 0

@makulera
Scenario: Statusuppdateringar då intyg makuleras
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    När jag makulerar intyget
    Så ska statusuppdatering "MAKULE" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 0, hanterade svar 0

@radera
Scenario: Statusuppdateringar då intyg raderas
    När jag fyller i alla nödvändiga fält för intyget
    Och jag raderar intyget
    Så ska statusuppdatering "RADERA" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 0, hanterade svar 0

@fråga-från-fk
Scenario: Statusuppdateringar vid fråga från FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 0, hanterade svar 0

    När Försäkringskassan skickar ett "Kontakt" meddelande på intyget
    Så ska statusuppdatering "NYFRFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 1, hanterade frågor 0,antal svar 0, hanterade svar 0

    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag svarar på frågan
    Så ska statusuppdatering "HANFRA" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 1, hanterade frågor 1,antal svar 0, hanterade svar 0

@fråga-till-fk
Scenario: Statusuppdateringar vid fråga till FK
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Försäkringskassan

    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 0, hanterade svar 0

    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag skickar en fråga med ämnet "Kontakt" till Försäkringskassan
    Så ska statusuppdatering "NYFRTM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 0, hanterade svar 0

    Och Försäkringskassan skickar ett svar
    Så ska statusuppdatering "NYSVFM" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 0

    Och jag markerar svaret från Försäkringskassan som hanterat
    Så ska statusuppdatering "HANSVA" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 1

    Och jag markerar svaret från Försäkringskassan som INTE hanterat
    Så ska statusuppdatering "HANSVA" skickas till vårdsystemet. Totalt: "2"
    Och ska statusuppdateringen visa frågor 0, hanterade frågor 0,antal svar 1, hanterade svar 0

@ANDRAT
Scenario: Statusuppdateringar vid ändring av utkast
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

    När jag ändrar i slumpat fält
    Så ska statusuppdatering "ANDRAT" skickas till vårdsystemet. Totalt: "1"

@vardkontakt-skickas-med
Scenario: Vårdkontakt skickas med statusuppdateringar
    Så ska jag gå in på intyget med extra parametrar

    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag kopierar intyget
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"
    Och egenskapen ref med värdet "testref" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
