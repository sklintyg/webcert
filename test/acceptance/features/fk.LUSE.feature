# language: sv
@smoke @sjukersattning @luse
Egenskap: Hantera Läkarutlåtande för sjukersättning

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare

Scenario: Skapa och signera ett intyg
	När jag väljer patienten "19520617-2339"
	Och jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"	
	# När jag går till Mina intyg för patienten "19520617-2339"
	# Så ska intyget finnas i Mina intyg

