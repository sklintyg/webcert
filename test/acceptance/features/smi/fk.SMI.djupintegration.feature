# language: sv
@DJUPINTEGRATION @SMI @INTEGRATIONSPARAMETRAR @IWC-AF1 @IWC-AF2
Egenskap: IWC-AF1 - Djupintegration SMI intyg

Bakgrund: Jag är inloggad som djupintegrerad läkare
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"

@NAMNBYTE @PS-004
Scenario: PS-004 - Informera om patienten har bytt namn
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn
    Så ska ett info-meddelande visa "Patientens namn skiljer sig från det i journalsystemet"

@NYTT-PERSONNUMMER @PS-003
Scenario: PS-003 - Patienten har fått ett nytt personnummer
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg med samordningsnummer eller personnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer
    Så ska ett varning-meddelande visa "Patientens personummer har ändrats"

    När jag förnyar intyget
	Och jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
    Och jag signerar intyget
    Så ska intyget visa det nya person-id:numret

@RESERVNUMMER @PS-007 @WAITINGFORFIX @INTYG-5743
Scenario: PS-007 - Patienten har fått ett reservnummer
	När att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg med samordningsnummer eller personnummer
	Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer
    Så ska ett varning-meddelande visa "Patienten har samordningsnummer kopplat till reservnummer:"

    När jag förnyar intyget
	Och jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
    Och jag signerar intyget
    Så ska intyget visa det gamla person-id:numret

@INTYGSDELNING-VÅRDENHET @GE-003
Scenario: GE-003 - Parametrar i djupintegrationslänk, och intygsdelning mellan vårdenheter
    Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget

    När jag går in på intyget via djupintegrationslänk och har parametern "responsibleHospName" satt till "Peter Parameter"
    Så ska jag se signerande läkare "Peter Parameter"
    Och jag signerar intyget

    När jag går in på intyget via djupintegrationslänk och har parametern "kopieringOK" satt till "false"
    Så ska det inte finnas knappar för "förnya"

    När jag går in på intyget via djupintegrationslänk och har parametern "inaktivEnhet" satt till "true"
    Så ska det inte finnas knappar för "förnya"

    När jag går in på intyget via djupintegrationslänk och har parametern "avliden" satt till "true"
    Så ska jag varnas om att "Patienten är avliden"
    Så ska det inte finnas knappar för "ersätta,förnya"
	
	Givet jag skickar intyget till Försäkringskassan
	#Behövs för att kontrollera att fråga/svar inte visas vid SJF = false

    Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
    När jag går in på intyget via djupintegrationslänk och har parametern "sjf" satt till "true"
    Så ska det finnas knappar för "förnya"
    Och ska det inte finnas knappar för "ersätta,makulera"
	
    Och att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-107P"
	När jag går in på intyget via djupintegrationslänk och har parametern "sjf" satt till "false"
	Så ska det inte finnas knappar för "ersätta,makulera,fråga/svar"
	Så ska det finnas knappar för "förnya" om intygstyp är "Läkarintyg för sjukpenning"

@GE-003 @INGAPARAMETRAR
Scenario: GE-003 - Det ska vara möjligt att gå in på utkast och intyg utan integrationsparametrar.
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet utan integrations parametrar
	Och jag väljer vårdenheten "TSTNMT2321000156-INT2"
    Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Försäkringskassan
	
	Så ska det finnas knappar för "förnya,ersätta,makulera,fråga/svar"