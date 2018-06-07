# language: sv

@DB @SKV
Egenskap: Dödsbevis

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet jag har raderat alla intyg och utkast för "första" "Dödsbevis" testpatienten
		  Och att jag är inloggad som läkare
		  När jag går in på "första" testpatienten för "Dödsbevis"

@SIGNERA
Scenario: Kan signera dödsbevisintyg 
		  Och jag går in på att skapa ett "Dödsbevis" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Så ska jag se den data jag angett för intyget

		  
@SIGNERATVY @SIGNERA
Scenario: Signera Dödsbevis och kontrollera fält i signerat vyn
		  Och jag går in på att skapa ett "Dödsbevis" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Så ska jag se den data jag angett för intyget
	
@DOI
Scenario: Ska kunna skapa Dödsorsaksintyg utifrån ett Dödsbevis
		  Och jag går in på att skapa ett "Dödsbevis" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Och jag ska se den data jag angett för intyget
		  Så klickar jag på knappen "Skriv dödsorsaksintyg"
		  Och jag fyller i nödvändig information ( om intygstyp är "Dödsorsaksintyg")
		  Och jag uppdaterar enhetsaddress
		  Och jag signerar intyget
		  Så ska jag se den data jag angett för intyget