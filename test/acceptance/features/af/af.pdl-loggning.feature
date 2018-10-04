# language: sv

@PDL @GE-005 @AF @NOTREADY
Egenskap: GE-005 - PDL-loggning för AF-intyg

Bakgrund: Jag är inloggad
	Givet att jag är inloggad som läkare
	Och jag går in på en patient

#1 #4
# Första ändring per ändringssession ska loggas
@SKRIVA @LÄSA @WIP
Scenario: GE-005 - Skapa AF-intyg
	När jag går in på att skapa ett AF-intyg
	Så ska det nu finnas 1 loggaktivitet "Skriva" för intyget
	Och jag går till Sök/skriv intyg
	Och jag går in på utkastet
	Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
	Och jag ändrar i slumpat fält
	Så ska det nu finnas 2 loggaktivitet "Skriva" för intyget

#2
@LÄSA @WIP
Scenario: GE-005 - Öppna AF-intyg
	När jag går in på ett slumpat AF-intyg med status "Signerat"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten

#3 #8
@SJF @UTSKRIFT @LÄSA
Scenario: GE-005 - Händelser på AF-intyg utfärdat på annan vårdgivare ska PDL-loggas
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och att vårdsystemet skapat ett intygsutkast för slumpat AF-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
    När jag går in på intyget via djupintegrationslänk med parameter "sjf=true"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten med argument "Läsning i enlighet med sammanhållen journalföring"
	
	Och jag skriver ut intyget
	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg utskrivet. Läsning i enlighet med sammanhållen journalföring"
	
#3
@SJF @LÄSA @MAKULERAT
Scenario: GE-005 - Händelser på makulerat AF-intyg utfärdat på annan vårdgivare ska PDL-loggas
	Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
	Och att vårdsystemet skapat ett intygsutkast för slumpat AF-intyg
    Och jag går in på intygsutkastet via djupintegrationslänk
    Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget	
	När jag makulerar intyget
	
	Och att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
    Och jag går in på intyget via djupintegrationslänk med parameter "sjf=true"
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten med argument "Läsning i enlighet med sammanhållen journalföring"
	
#5 #7
@UTSKRIFT
Scenario: GE-005 - Skriv ut AF-intyg
	När att vårdsystemet skapat ett intygsutkast för slumpat AF-intyg
	Och jag går in på utkastet
	Och jag skriver ut utkastet
	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Utkastet utskrivet"
	
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
   	Så ska loggaktivitet "Signera" skickas till loggtjänsten
   
	Och jag skriver ut intyget
	Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg utskrivet"

#6
@SKICKA @UTSKRIFT  @WIP
Scenario: GE-005 - PDL - Skicka AF-intyg till Arbetsförmedlingen
    När jag går in på ett slumpat AF-intyg med status "Skickat"
	Och jag går in på utkastet
    Så har intyget skickats till Arbetsförmedlingen
    Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyget är skickat till Arbetsförmedlingen"

#9
@RADERA
Scenario: GE-005 - PDL - Radera AF-utkast
	När jag går in på att skapa ett AF-intyg
	Och jag går in på utkastet
	Och jag raderar utkastet
	Så ska loggaktivitet "Radera" skickas till loggtjänsten

#10
@MAKULERA @WIP
Scenario: GE-005 - Makulera AF-intyg
	När jag går in på ett slumpat AF-intyg med status "Skickat"
	Och jag går in på utkastet
	Och jag makulerar intyget
	Så ska loggaktivitet "Radera" skickas till loggtjänsten

#11
#Förnya finns inte på AF intyg.

#11
@ERSÄTT @LÄSA @SKRIVA
Scenario: GE-005 - Ersätta AF-intyg
	När jag går in på ett slumpat AF-intyg med status "Signerat"
	Och jag går in på utkastet
	Och jag klickar på ersätta knappen
	Och jag klickar på ersätt-knappen i dialogen
	Och jag fyller i nödvändig information ( om intygstyp är "Läkarintyg för sjukpenning")
    Och jag signerar intyget
	Så ska loggaktivitet "Läsa" skickas till loggtjänsten
	Och ska loggaktivitet "Skriva" skickas till loggtjänsten
	
