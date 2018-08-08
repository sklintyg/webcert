# language: sv

@DB @DODSBEVIS @SKV @SEKRETESSMARKERING
Egenskap: Dödsbevis - sekretessmarkering

Bakgrund: Jag befinner mig på webcerts förstasida
		Givet jag har raderat alla intyg och utkast för "första" "sekretessmarkering" testpatienten
		Och att jag är inloggad som läkare
		  
		  
Scenario: Ska inte kunna utfärda Dödsbevis på patienter med sekretessmarkering
		När jag går in på "första" testpatienten för "sekretessmarkering"
		Så ska jag inte kunna skapa ett "Dödsbevis" intyg