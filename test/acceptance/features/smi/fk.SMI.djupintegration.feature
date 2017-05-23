# language: sv
@djupintegration @SMI
Egenskap: Djupintegration SMI intyg

Bakgrund: Jag är inloggad som djupintegrerad läkare
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"

@namnbyte
Scenario: Informera om patienten har bytt namn
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn
    Så ska ett info-meddelande visa "Observera att patientens namn har ändrats sedan det här intyget utfärdades."

@adressbyte @nyttIntyg
Scenario: Informera om patienten har bytt adress och använd address på nya intyg
    När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annan adress
    Så ska ett info-meddelande visa "Observera att patientens adress har ändrats sedan det här intyget utfärdades."

    När jag kopierar intyget
    Och jag signerar intyget
    Så ska intyget visa den nya addressen

@nytt-personnummer
Scenario: Patienten har fått ett nytt personnummer
	När att vårdsystemet skapat ett intygsutkast för slumpat intyg med samordningsnummer eller personnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer
    Så ska ett varning-meddelande visa "Patienten har ett nytt personnummer"

    När jag kopierar intyget
    Och jag signerar intyget
    Så ska intyget visa det nya person-id:numret

@reservnummer
Scenario: Patienten har fått ett reservnummer
	När att vårdsystemet skapat ett intygsutkast för slumpat intyg med samordningsnummer eller personnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer
    Så ska ett varning-meddelande visa "Patienten har samordningsnummer kopplat till reservnummer"

    När jag kopierar intyget
    Och jag signerar intyget
    Så ska intyget visa det gamla person-id:numret

@parametrar
Scenario: Parametrar
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget

    När jag går in på intyget via djupintegrationslänk och har parametern "responsibleHospName" satt till "Peter Parameter"
    Så ska jag se signerande läkare "Peter Parameter"
    Och jag signerar intyget

    När jag går in på intyget via djupintegrationslänk och har parametern "kopieringOK" satt till "false"
    Så ska det inte finnas knappar för "kopiera,förnya"

    När jag går in på intyget via djupintegrationslänk och har parametern "inaktivEnhet" satt till "true"
    Så ska det inte finnas knappar för "kopiera,förnya"

    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Så ska jag varnas om att "Patienten har avlidit"
    Så ska det inte finnas knappar för "kopiera,ersätta,förnya"

    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
    När jag går in på intyget via djupintegrationslänk och har parametern "sjf" satt till "true"
    Så ska det finnas knappar för "kopiera,förnya"
    Och ska det inte finnas knappar för "ersätta,makulera"



