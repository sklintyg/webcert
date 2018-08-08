# language: sv

@DOI @SOCIALSTYRELSEN @SOC @SEKRETESSMARKERING
Egenskap: Dödsorsaksintyg sekretessmarkering

Bakgrund: Jag befinner mig på webcerts förstasida
		Givet jag har raderat alla intyg och utkast för "första" "sekretessmarkering" testpatienten
		Och att jag är inloggad som läkare
		  

Scenario: Ska inte kunna utfärda DOI på patienter med sekretessmarkering
		När jag går in på "första" testpatienten för "sekretessmarkering"
		#TODO behöver en till testpatient med sekretessmarkering p.g.a. att denna redan används för test av Dödsbevis
		Så ska jag inte kunna skapa ett "Dödsorsaksintyg" intyg
		  
