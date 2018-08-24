# language: sv
@STATUSUPPDATERINGAR @AF @NOTREADY
Egenskap: Statusuppdateringar för AF intyg

Bakgrund: Jag har skickat en CreateDraft till Webcert.
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
   Och att vårdsystemet skapat ett intygsutkast för "Arbetsförmedlingens medicinska utlåtande"
   Och jag går in på intygsutkastet via djupintegrationslänk

@SKICKA-TILL-AF @SKAPAT @SIGNAT @SKICKA
Scenario: Statusuppdateringar då AMU intyg skickas till Arbetsförmedlingen
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"

    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"

    När jag skickar intyget till Arbetsförmedlingen
    Så ska statusuppdatering "SKICKA" skickas till vårdsystemet. Totalt: "1"


@MAKULE
Scenario: Statusuppdateringar då AMU intyg makuleras
    När jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag skickar intyget till Arbetsförmedlingen

    När jag makulerar intyget
    Så ska statusuppdatering "MAKULE" skickas till vårdsystemet. Totalt: "1"

@RADERA
Scenario: Statusuppdateringar då AMU utkast raderas
    När jag fyller i alla nödvändiga fält för intyget
    Och jag raderar utkastet
    Så ska statusuppdatering "RADERA" skickas till vårdsystemet. Totalt: "1"

@VÅRDKONTAKT @REF
Scenario: AF - ref (vårdkontakt) skickas med statusuppdateringar
    När jag går in på intyget via djupintegrationslänk med parameter "ref=testref"

    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska statusuppdatering "SKAPAT" skickas till vårdsystemet. Totalt: "1"
	Så ska statusuppdatering "SIGNAT" skickas till vårdsystemet. Totalt: "1"
	#Viktigt att vi kollar efter Signat innan nedan steg
    Och ska statusuppdateringen visa att parametern "ref" är mottagen med värdet "testref"
    Och ska statusuppdateringen visa mottagna frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
    Och ska statusuppdateringen visa skickade frågor totalt 0,ej besvarade 0,besvarade 0, hanterade 0
