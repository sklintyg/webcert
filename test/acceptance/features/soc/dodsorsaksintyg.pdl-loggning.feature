# language: sv

@DOI @SOC @PDL @GE-005 @WIP
Egenskap: GE-005 - PDL-loggning för Dödsorsaksintyg

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet jag har raderat alla intyg och utkast för "femte" "Dödsorsaksintyg" testpatienten
		  Och att jag är inloggad som läkare
		  Och jag går in på "femte" testpatienten för "Dödsorsaksintyg"
		  
#1 #4
# Första ändring per ändringssession ska loggas
 @SKRIVA @LÄSA
Scenario: GE-005 - Skapa Dödsorsaksintyg 
		Och jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Så ska det nu finnas 1 loggaktivitet "Skriva" för intyget
		  
		Och jag går till Sök/skriv intyg
		Och jag går in på utkastet
		Så ska det nu finnas 1 loggaktivitet "Läsa" för intyget
		Och jag ändrar i slumpat fält
		Så ska det nu finnas 2 loggaktivitet "Skriva" för intyget
		
#2
 @LÄSA
Scenario: GE-005 - Öppna Dödsorsaksintyg
		När jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag går till Sök/skriv intyg
		Och jag går in på intyget
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten
		
#3 #8
@SJF @UTSKRIFT @UTSKRIFT @LÄSA
Scenario: GE-005 - Händelser på Dödsorsaksintyg utfärdat på annan vårdgivare ska PDL-loggas
		Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
		Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsorsaksintyg"
		Och jag går in på intygsutkastet via djupintegrationslänk
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		
		Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
		När jag går in på intyget via djupintegrationslänk med parameter "sjf=true"
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten med argument "Läsning i enlighet med sammanhållen journalföring"
		
		Och jag skriver ut intyget
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg utskrivet. Läsning i enlighet med sammanhållen journalföring"
		
#3
@SJF @UTSKRIFT @LÄSA @MAKULERAT
Scenario: GE-005 - Händelser på makulerat Dödsorsaksintyg utfärdat på annan vårdgivare ska PDL-loggas
		Givet att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-INT2"
		Och att vårdsystemet skapat ett intygsutkast för samma patient för "Dödsorsaksintyg"
		Och jag går in på intygsutkastet via djupintegrationslänk
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag makulerar intyget
		
		Och att jag är inloggad som djupintegrerad läkare på vårdenhet "TSTNMT2321000156-1077" och inte har uppdrag på "TSTNMT2321000156-INT2"
		När jag går in på intyget via djupintegrationslänk med parameter "sjf=true"
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten med argument "Läsning i enlighet med sammanhållen journalföring"
		
	
#5 #7
@UTSKRIFT
Scenario: GE-005 - Skriv ut Dödsorsaksintyg
		Och jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Och jag skriver ut utkastet
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Utkastet utskrivet"
		
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Så ska loggaktivitet "Signera" skickas till loggtjänsten
	   
		Och jag skriver ut intyget
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg utskrivet"
		
#6
@SKICKA @UTSKRIFT
Scenario: GE-005 - PDL - Skicka Dödsorsaksintyg till Socialstyrelsen
		När jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		#Vid signering skickas DB automatiskt
		Så ska loggaktivitet "Utskrift" skickas till loggtjänsten med argument "Intyg skickat till mottagare SOS"

#9
@RADERA
Scenario: GE-005 - PDL - Radera Dödsorsaksintyg utkast
		När jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Och jag raderar utkastet
		Så ska loggaktivitet "Radera" skickas till loggtjänsten

#10
@MAKULERA
Scenario: GE-005 - Makulera Dödsorsaksintyg
		När jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag makulerar intyget
		Så ska loggaktivitet "Radera" skickas till loggtjänsten

#11
@ERSÄTT @LÄSA @SKRIVA
Scenario: GE-005 - Ersätta Dödsorsaksintyg
		När jag går in på att skapa ett "Dödsorsaksintyg" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag signerar intyget
		Och jag klickar på ersätta knappen
		Och jag klickar på ersätt-knappen i dialogen
		Och jag signerar intyget
		Så ska loggaktivitet "Läsa" skickas till loggtjänsten
		Och ska loggaktivitet "Skriva" skickas till loggtjänsten