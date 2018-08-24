# language: sv
@DJUPINTEGRATION @AF @INTEGRATIONSPARAMETRAR @IWC-AF1 @IWC-AF2 @NOTREADY
Egenskap: IWC-AF1 - Djupintegration AF intyg

Bakgrund: Jag är inloggad som djupintegrerad läkare
   Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"

@NAMNBYTE @PS-04
Scenario: PS-04 - Informera om patienten har bytt namn
	När att vårdsystemet skapat ett intygsutkast för slumpat AF-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn
    Så ska ett info-meddelande visa "Patientens namn skiljer sig från det i journalsystemet"

@NYTT-PERSONNUMMER @PS-03
Scenario: PS-03 - Patienten har fått ett nytt personnummer
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

@RESERVNUMMER @PS-07
Scenario: PS-07 - Patienten har fått ett reservnummer
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

@RESPONSIBLEHOSPNAME	
Scenario: [responsibleHospName] - Endast vårdadmin ska se signerande läkare
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intyget via djupintegrationslänk med parameter "responsibleHospName=Peter Parameter"
	Så ska jag inte se signerande läkare "Peter Parameter"		
	#Endast vårdadmin ska se signerande läkare 

	

@GE-003 @INGAPARAMETRAR
Scenario: GE-003 - Det ska vara möjligt att gå in på utkast och intyg utan integrationsparametrar.
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intygsutkastet utan integrations parametrar
	Och jag väljer vårdenheten "TSTNMT2321000156-INT2"
    Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Arbetsförmedlingen
	
	Så ska det finnas knappar för "förnya,ersätta,makulera,fråga/svar"
	
@GE-003 
Scenario: GE-003 - Tillgängliga funktioner #1 - enhetId=medskickad enhetId och parameter: kopieringOK=false

	#Kontrollerar att det går att radera intygsutkast
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
    När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=false"
    Och jag raderar utkastet

	#Kontrollerar att det går att skapa, skriva, signera och skriva ut intyg
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=false"
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	
	Och jag skriver ut intyget
	
	#Kontrollerar att det inte går att förnya med att det går att ersätta, makulera och använda ärendekommunikation
	Så ska det inte finnas knappar för "förnya"
	Så ska det finnas knappar för "ersätta,makulera"
	
	När jag skickar intyget till Arbetsförmedlingen
	Så ska det finnas knappar för "fråga/svar"

@GE-003
Scenario: GE-003 - Tillgängliga funktioner #2 - enhetId!=medskickad enhetId och parametrar: kopieringOK=false & sjf=true
	#Kontrollerar utkast
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk och har parameter "kopieringOK=false&sjf=true"
	Så ska jag varnas om att "Behörighet saknas"
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Arbetsförmedlingen
	
	#Kontrollerar signerat intyg
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=false&sjf=true"
	
	Så ska det inte finnas knappar för "skicka,ersätta,förnya,makulera,fråga/svar"
	
	#Kontrollerar att det går att skriva ut intyget
	Och jag skriver ut intyget
	
	
@GE-003
Scenario: GE-003 - Tillgängliga funktioner #3 - enhetId=medskickad enhetId och parameter: kopieringOK=true
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=true"
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Arbetsförmedlingen
	Så ska det finnas knappar för "ersätta,förnya,makulera,fråga/svar"
	Och jag skriver ut intyget
	
@GE-003 @WC-F017
Scenario: GE-003 - Tillgängliga funktioner #4 - enhetId!=medskickad enhetId och parameter: kopieringOK=true
	#Kontrollerar utkast
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	Och att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk och har parameter "kopieringOK=true&sjf=true"
	Så ska jag varnas om att "Behörighet saknas"
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag skickar intyget till Arbetsförmedlingen
	
	#Kontrollerar signerat intyg
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=true&sjf=true"
	
	Så ska det inte finnas knappar för "skicka,ersätta,makulera,fråga/svar"
	
	#Gäller SMI; WC-F017
	Så ska det finnas knappar för "förnya"
	
	#Kontrollerar att det går att skriva ut intyget
	Och jag skriver ut intyget
	
@GE-003 @WC-F017
Scenario: GE-003 - Tillgängliga funktioner #5 - enhetId!=medskickad enhetId och patienten är sekretessmarkerad &sjf=true
	Givet jag går in på en patient med sekretessmarkering
	Och att vårdsystemet skapat ett intygsutkast för samma patient för slumpat SMI-intyg
	
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk och har parameter "kopieringOK=true&sjf=true"
	Så ska jag varnas om att "Behörighet saknas"

@GE-003	
Scenario: GE-003 - Tillgängliga funktioner #6 - avliden=true
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intyget via djupintegrationslänk med parameter "avliden=true"
	Så ska jag varnas om att "Patienten är avliden"
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska det inte finnas knappar för "förnya,ersätta"
	
	Och jag skickar intyget till Arbetsförmedlingen
	Så ska det finnas knappar för "fråga/svar,makulera"	
	
	
@GE-003	
Scenario: GE-003 - Tillgängliga funktioner #8 - inaktivEnhet=true
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intyget via djupintegrationslänk med parameter "inaktivEnhet=true"
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska det inte finnas knappar för "förnya,ersätta"
	
	Och jag skickar intyget till Arbetsförmedlingen
	Så ska det finnas knappar för "fråga/svar,makulera"	
    
	
@GE-003
Scenario: GE-003 - Ej tillgängliga funktioner #2 och #4 - enhetId!=medskickad enhetId och parameter: sjf=false
	#Utkast
	Givet att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk
	Så ska jag varnas om att "Behörighet saknas"
	
	#Intyg
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och att vårdsystemet skapat ett intygsutkast för slumpat SMI-intyg
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk
	Så ska jag varnas om att "Kunde inte hämta intyget eftersom du saknar behörighet."
	
	