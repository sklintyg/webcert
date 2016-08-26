# language: sv
@faltvalidering @luse @notReady
# Funktion har ändrats. TF behöver uppdateras
Egenskap: Fältvalidering LUSE

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient
	Och jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg

Scenario: Validera felaktig diagnoskod i LUSE
	När jag fyller i "000" som diagnoskod
	Så ska valideringsfelet "är ej giltig" visas
