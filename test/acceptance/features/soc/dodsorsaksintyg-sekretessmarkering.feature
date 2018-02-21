# language: sv

@doi @dodsorsaksintyg @socialstyrelsen @soc
Egenskap: Dödsorsaksintyg sekretessmarkering

Bakgrund: Jag befinner mig på webcerts förstasida
		Givet jag har makulerat tidigare "Dödsorsaksintyg" intyg för "sekretessmarkering" testpatienten
		Och att jag är inloggad som läkare
		  

@sekretessmarkering
Scenario: Ska inte kunna utfärda DOI på patienter med sekretessmarkering
		När jag går in på "sekretessmarkering" testpatienten
		Så ska jag inte kunna skapa ett "Dödsorsaksintyg" intyg
		  
