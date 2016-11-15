# language: sv
@djupintegration @luaefs
Egenskap: Djupintegration Läkarutlåtande för aktivitetsersättning vid förlängd skolgång

Bakgrund: Jag är inloggad som djupintegreread läkare
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"

@namnbyte
Scenario: Informera om patienten har bytt namn
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn
    Så ska ett info-meddelande visa "Observera att patientens namn har ändrats sedan det här intyget utfärdades."

@adressbyte @nyttIntyg
Scenario: Informera om patienten har bytt adress och använd address på nya intyg
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annan adress
    Så ska ett info-meddelande visa "Observera att patientens adress har ändrats sedan det här intyget utfärdades."

    När jag kopierar intyget
    Och jag signerar intyget
    Så ska intyget visa den nya addressen

@namnbyte @adressbyte
Scenario: Informera om patienten har bytt namn och address
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång"
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn och adress
    Så ska ett info-meddelande visa "Observera att patientens namn och adress har ändrats sedan det här intyget utfärdades."

@samordningsnummer
Scenario: Informera om patienten har fått ett nytt personnummer
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" med samordningsnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer
    Så ska ett varning-meddelande visa "Patienten har ett nytt personnummer"

@reservnummer @notReady
Scenario: Informera om patienten har fått ett reservnummer
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" med samordningsnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer
    Så ska ett varning-meddelande visa "Patienten har ett samordningsnummer kopllat till ett reservnummer"
