# language: sv

@DB @DODSBEVIS @SKATTEVERKET @SKV @PDL @GE-005
Egenskap: GE-005 - PDL-loggning för Dödsbevis

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet jag har raderat alla intyg och utkast för "tredje" "Dödsbevis" testpatienten
		  Och att jag är inloggad som läkare
		  Och jag går in på "tredje" testpatienten för "Dödsbevis"
		  
#1 #4
# Första ändring per ändringssession ska loggas
@skapa @skriva @läsa
Scenario: GE-005 - Skapa Dödsbevis 
		Och jag går in på att skapa ett "Dödsbevis" intyg
		Så ska det nu finnas 1 loggaktivitet "Skriva" för intyget
		  
		Och jag går till Sök/skriv intyg
		Och jag går in på utkastet
		Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
		Och jag ändrar i slumpat fält
		Så ska det nu finnas 2 loggaktivitet "Skriva" för intyget
		
#2
@öppna @läsa
Scenario: GE-005 - Öppna Dödsbevis
		När jag går in på att skapa ett "Dödsbevis" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag går till Sök/skriv intyg
		Och jag går in på intyget
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten
		
#3 #8
@OLIKA-VÅRDGIVARE @SKRIV-UT @UTSKRIFT @LÄSA
Scenario: GE-005 - Händelser på Dödsbevis utfärdat på annan vårdgivare ska PDL-loggas
		Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
		Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsbevis"
		Och jag går in på intygsutkastet via djupintegrationslänk
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		
		Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
		När jag går in på intyget via djupintegrationslänk och har parametern "sjf" satt till "true"
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten med argument "Läsning i enlighet med sammanhållen journalföring"
		
		Och jag skriver ut intyget
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg utskrivet. Läsning i enlighet med sammanhållen journalföring"
		
		Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
		Och jag går in på intyget via djupintegrationslänk
		Och jag makulerar intyget
		Och att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
		Och jag går in på intyget via djupintegrationslänk och har parametern "sjf" satt till "true"
		Och jag skriver ut intyget
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Makulerat intyg utskrivet. Läsning i enlighet med sammanhållen journalföring"

	
#5 #7
@skriv-ut @utskrift
Scenario: GE-005 - Skriv ut Dödsbevis
		Och jag går in på att skapa ett "Dödsbevis" intyg
		Och jag skriver ut utkastet
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Utkastet utskrivet"
		
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Så ska loggaktivitet "Signera" skickas till loggtjänsten
	   
		Och jag skriver ut intyget
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg utskrivet"
		
		Och jag makulerar intyget
		Och jag skriver ut intyget
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Makulerat intyg utskrivet"

#6
@skicka @utskrift
Scenario: GE-005 - PDL - Skicka Dödsbevis till Skatteverket
		När jag går in på att skapa ett "Dödsbevis" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		#Vid signering skickas DB automatiskt
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg skickat till mottagare SKV"

#9
@radera
Scenario: GE-005 - PDL - Radera Dödsbevis utkast
		När jag går in på att skapa ett "Dödsbevis" intyg
		Och jag raderar utkastet
		Så ska loggaktivitet "Radera" skickas till loggtjänsten

#10
@makulera
Scenario: GE-005 - Makulera Dödsbevis
		När jag går in på att skapa ett "Dödsbevis" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag makulerar intyget
		Så ska loggaktivitet "Radera" skickas till loggtjänsten

#11
@ersatt @läsa @skriva
Scenario: GE-005 - Ersätta Dödsbevis
		När jag går in på att skapa ett "Dödsbevis" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag klickar på ersätta knappen
		Och jag klickar på ersätt-knappen i dialogen
		Och jag signerar intyget
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten
		Och ska loggaktivitet "Skriva" skickas till loggtjänsten