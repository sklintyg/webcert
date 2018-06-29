# language: sv

@DB @DODSBEVIS @SKV @INTEGRATIONSPARAMETRAR @IWC-AF1 @IWC-AF2 @WIP
Egenskap: Dödsbevis

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet jag har raderat alla intyg och utkast för "fjärde" "Dödsbevis" testpatienten
		  Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
		  När jag går in på "fjärde" testpatienten för "Dödsbevis"
		  Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsbevis"

@NAMNBYTE @PS-004
Scenario: PS-004 - Informera om patienten har bytt namn
    När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med annat namn
    Så ska ett info-meddelande visa "Patientens namn skiljer sig från det i journalsystemet"

@NYTT-PERSONNUMMER @PS-003
Scenario: PS-003 - Patienten har fått ett nytt personnummer
	När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett annat personnummer
    Så ska ett varning-meddelande visa "Patientens personummer har ändrats"

@RESERVNUMMER @PS-007
Scenario: PS-007 - Patienten har fått ett reservnummer
	När jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag går in på intygsutkastet via djupintegrationslänk med ett reservnummer
    Så ska ett varning-meddelande visa "Patienten har samordningsnummer kopplat till reservnummer:"

@RESPONSIBLEHOSPNAME	
Scenario: [responsibleHospName] - Endast vårdadmin ska se signerande läkare
    När jag går in på intyget via djupintegrationslänk med parameter "responsibleHospName=Peter Parameter"
	Så ska jag inte se signerande läkare "Peter Parameter"		
	#Endast vårdadmin ska se signerande läkare 

@GE-003 @INGAPARAMETRAR
@GE-007
Scenario: GE-003 - Det ska vara möjligt att gå in på utkast och intyg utan integrationsparametrar.
    När jag går in på intygsutkastet utan integrations parametrar
	Och jag väljer vårdenheten "TSTNMT2321000156-INT2"
    Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	Så ska det finnas knappar för "ersätta,makulera"
	Så ska det inte finnas knappar för "förnya"
	
@GE-003 
Scenario: GE-003 - Tillgängliga funktioner #1 - enhetId=medskickad enhetId och parameter: kopieringOK=false

	#Kontrollerar att det går att radera intygsutkast
    När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=false"
    Och jag raderar utkastet

	#Kontrollerar att det går att skapa, skriva, signera och skriva ut intyg
	Givet att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsbevis"
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=false"
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
	
	Och jag skriver ut intyget
	
	#Kontrollerar att det inte går att förnya men att det går att ersätta, makulera och använda ärendekommunikation
	Så ska det inte finnas knappar för "förnya"
	Så ska det finnas knappar för "ersätta,makulera"
	

@GE-003
Scenario: GE-003 - Tillgängliga funktioner #2 - enhetId!=medskickad enhetId och parametrar: kopieringOK=false & sjf=true
	#Kontrollerar utkast	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk och har parameter "kopieringOK=false&sjf=true"
	Så ska jag varnas om att "Behörighet saknas"
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	#Kontrollerar signerat intyg
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=false&sjf=true"
	
	Så ska det inte finnas knappar för "ersätta,förnya,makulera"
	
	#Kontrollerar att det går att skriva ut intyget
	Och jag skriver ut intyget
	
	
@GE-003
Scenario: GE-003 - Tillgängliga funktioner #3 - enhetId=medskickad enhetId och parameter: kopieringOK=true
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=true"
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Så ska det finnas knappar för "ersätta,makulera"
	#GE-007
	Så ska det inte finnas knappar för "förnya"
	Och jag skriver ut intyget
	
@GE-003 @WC-F017
Scenario: GE-003 - Tillgängliga funktioner #4 - enhetId!=medskickad enhetId och parameter: kopieringOK=true
	#Kontrollerar utkast
	Och att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk och har parameter "kopieringOK=true&sjf=true"
	Så ska jag varnas om att "Behörighet saknas"
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	#Kontrollerar signerat intyg
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag går in på intyget via djupintegrationslänk med parameter "kopieringOK=true&sjf=true"
	
	Så ska det inte finnas knappar för "ersätta,makulera"
	
	#Kontrollerar att det går att skriva ut intyget
	Och jag skriver ut intyget

@GE-003	
Scenario: GE-003 - Tillgängliga funktioner #6 - avliden=true
	När jag går in på intyget via djupintegrationslänk med parameter "avliden=true"
	Så ska jag varnas om att "Patienten är avliden"
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska det inte finnas knappar för "förnya,ersätta"
	
	Så ska det finnas knappar för "makulera"	
	
	
@GE-003	
Scenario: GE-003 - Tillgängliga funktioner #8 - inaktivEnhet=true
	När jag går in på intyget via djupintegrationslänk med parameter "inaktivEnhet=true"
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska det inte finnas knappar för "förnya,ersätta"
	
	Så ska det finnas knappar för "makulera"	
    
	
@GE-003
Scenario: GE-003 - Ej tillgängliga funktioner #2 och #4 - enhetId!=medskickad enhetId och parameter: sjf=false
	#Utkast
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk
	Så ska jag varnas om att "Behörighet saknas"
	
	#Intyg
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsbevis"
	När jag går in på intyget via djupintegrationslänk
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
	När jag försöker gå in på intygsutkastet via djupintegrationslänk
	Så ska jag varnas om att "Kunde inte hämta intyget eftersom du saknar behörighet."
	
	