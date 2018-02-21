# language: sv

@db @dodsbevis @skatteverket @skv
Egenskap: Dödsbevis - sekretessmarkering

Bakgrund: Jag befinner mig på webcerts förstasida
		Givet jag har makulerat tidigare "Dödsbevis" intyg för "sekretessmarkering" testpatienten
		Och att jag är inloggad som läkare
		  
		  
@sekretessmarkering
Scenario: Ska inte kunna utfärda Dödsbevis på patienter med sekretessmarkering
		När jag går in på "sekretessmarkering" testpatienten
		Så ska jag inte kunna skapa ett "Dödsbevis" intyg