# language: sv
@statusuppdateringar @ts @bas @waitingForFix
Egenskap: Statusuppdateringar för TS-bas intyg

Bakgrund: Jag har skickat en CreateDraft:2 till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
   Och att vårdsystemet skapat ett intygsutkast för "Transportstyrelsens läkarintyg"
   Och jag går in på intygsutkastet via djupintegrationslänk

@skicka-till-ts
Scenario: Statusuppdateringar då intyg skickas till Transportstyrelsen
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"

    När jag skickar intyget till Transportstyrelsen
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

@vardkontakt-skickas-med
Scenario: Vårdkontakt skickas med statusuppdateringar
    När jag går in på intyget via djupintegrationslänk och har parametern "ref" satt till "testref"

    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag kopierar intyget
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"
    Och ska statusuppdateringen visa att parametern "ref" är mottagen med värdet "testref"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
