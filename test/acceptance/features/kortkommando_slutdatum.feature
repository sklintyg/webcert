# language: sv
@kortkommando @notReady
Egenskap: Kortkommandon för slutdatum 

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@fk7263
Scenario: Kortkommando för slutdatum på intyg FK7263
	Givet att vårdsystemet skapat ett intygsutkast för slumpat FK7263-intyg
	Och jag går in på utkastet
	Och jag fyller i ett from datum
	Och jag fyller i kortkommando som till och med datum
	Så ska till och med datum räknas ut automatiskt
