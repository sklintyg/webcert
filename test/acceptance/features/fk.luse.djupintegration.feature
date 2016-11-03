# language: sv
@djupintegration @luse
Egenskap: Djupintegration Läkarutlåtande för sjukersättning

Bakgrund: Jag är inloggad som djupintegreread läkare
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1004"

@adressbyte @namnbyte
Scenario: Informera om patienten har bytt namn
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning"
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn
    Så ska ett info-meddelande visa "Observera att patientens namn har ändrats sedan det här intyget utfärdades."


@adressbyte
Scenario: Informera om patienten har bytt adress
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning"
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annan adress
    Så ska ett info-meddelande visa "Observera att patientens adress har ändrats sedan det här intyget utfärdades."

@namnbyte @adressbyte
Scenario: Informera om patienten har bytt namn och address
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning"
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn och adress
    Så ska ett info-meddelande visa "Observera att patientens namn och adress har ändrats sedan det här intyget utfärdades."

@samordningsnummer
Scenario: Informera om patienten har fått ett nytt personnummer
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning" med samordningsnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer
    Så ska ett varning-meddelande visa "Patienten har ett nytt personnummer"


@reservnummer
Scenario: Informera om patienten har fått ett reservnummer
	När att vårdsystemet skapat ett intygsutkast för "Läkarutlåtande för sjukersättning" med samordningsnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer
    Så ska ett varning-meddelande visa "Patienten har ett samordningsnummer kopllat till ett reservnummer"
