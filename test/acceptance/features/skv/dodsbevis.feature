# language: sv

@db @dodsbevis @skatteverket @skv @notReady
Egenskap: Dödsbevis

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet jag har makulerat tidigare "Dödsbevis" intyg för "första" testpatienten
		  Givet jag har makulerat tidigare "Dödsorsaksintyg" intyg för "första" testpatienten
		  Och att jag är inloggad som läkare
		  När jag går in på "första" testpatienten
		  

@signera
Scenario: Kan signera dödsbevisintyg 
		  Och jag går in på att skapa ett "Dödsbevis" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Så ska jag se den data jag angett för intyget
	
	
@doi @notReady
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