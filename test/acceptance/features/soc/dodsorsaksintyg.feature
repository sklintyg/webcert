# language: sv

@doi @dodsorsaksintyg @socialstyrelsen @soc
Egenskap: Dödsbevis

Bakgrund: Jag befinner mig på webcerts förstasida
		  Givet att jag är inloggad som läkare

@signera
Scenario: Kan signera Dödsorsaksintyg 
          När jag går in på "andra" testpatienten
  		  Och jag har makulerat tidigare "Dödsbevis" intyg
		  Och jag går in på att skapa ett "Dödsorsaksintyg" intyg
		  Och jag fyller i alla nödvändiga fält för intyget
		  Och jag signerar intyget
		  Och jag ska se den data jag angett för intyget
		  
		  
@sekretessmarkering
Scenario: Ska inte kunna utfärda DOI på patienter med sekretessmarkering
	När jag går in på en patient med sekretessmarkering
	Och jag har makulerat tidigare "Dödsbevis" intyg
	Så ska jag inte kunna skapa ett "Dödsorsaksintyg" intyg