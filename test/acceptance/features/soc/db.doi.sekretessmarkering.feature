# language: sv

@SEKRETESSMARKERING
Egenskap: DB/DOI sekretessmarkering

Bakgrund: Jag befinner mig på webcerts förstasida
		Givet jag har raderat alla intyg och utkast för "första" "sekretessmarkering" testpatienten
		Och att jag är inloggad som läkare
		  
@DOI @SOCIALSTYRELSEN @SOC 
Scenario: Ska inte kunna utfärda DOI på patienter med sekretessmarkering
	När jag går in på "första" testpatienten för "sekretessmarkering"
	#TODO behöver en till testpatient med sekretessmarkering p.g.a. att denna redan används för test av Dödsbevis
	Så ska jag inte kunna skapa ett "Dödsorsaksintyg" intyg

@DB @DODSBEVIS @SKV 
Scenario: Ska inte kunna utfärda Dödsbevis på patienter med sekretessmarkering
	När jag går in på "första" testpatienten för "sekretessmarkering"
	Så ska jag inte kunna skapa ett "Dödsbevis" intyg