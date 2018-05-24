# language: sv
@KORTKOMMANDO @NOTREADY
Egenskap: Kortkommandon för slutdatum 

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@LISJP
Scenario: Kortkommando för slutdatum på Läkarintyg för sjukpenning
	Givet att vårdsystemet skapat ett intygsutkast för "Läkarintyg för sjukpenning"
	Och jag går in på utkastet
	Och jag fyller i ett from datum
	Och jag fyller i kortkommando som till och med datum
	Så ska till och med datum räknas ut automatiskt
