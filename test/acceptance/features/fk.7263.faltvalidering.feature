# language: sv
@faltValid
Egenskap: Fältvalidering fk7263

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag väljer patienten "19971019-2387"
	Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg

Scenario: Validera felaktig diagnoskod i Fk7263
	När jag fyller i "A0" som diagnoskod
	Så ska valideringsfelet "är ej giltig"