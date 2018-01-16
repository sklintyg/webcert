# language: sv

@db @dodsbevis @skatteverket @skv @notReady
Egenskap: Dödsbevis - sekretessmarkering

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet jag har makulerat tidigare "Dödsbevis" intyg för "sekretessmarkering" testpatienten
		  Givet jag har makulerat tidigare "Dödsorsaksintyg" intyg för "sekretessmarkering" testpatienten
		  Och att jag är inloggad som läkare
		  När jag går in på "sekretessmarkering" testpatienten
		  
@sekretessmarkering
Scenario: Ska inte kunna utfärda Dödsbevis på patienter med sekretessmarkering
		När jag går in på en patient med sekretessmarkering
		Och jag makulerar tidigare "Dödsbevis" intyg
		Så ska jag inte kunna skapa ett "Dödsbevis" intyg