# language: sv
@faltValid
Egenskap: Fältvalidering LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg

Scenario: Validera felaktig diagnoskod i LUSE
	När jag fyller i "000" som diagnoskod
	Så ska valideringsfelet "är ej giltig"