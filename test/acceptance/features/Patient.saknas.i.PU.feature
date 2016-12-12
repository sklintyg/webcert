#language: sv

@PnrEjPU
Egenskap: Personnummer ej i PU

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare

Scenario: Jag ska få ett felmeddelande när jag skriver in ett personnummer som inte finns i PU
	När jag anger ett personnummer som inte finns i PUtjänsten
	Så ska valideringsfelet "Personnumret du har angivit finns inte i folkbokföringsregistret. Kontrollera om du har skrivit rätt." visas