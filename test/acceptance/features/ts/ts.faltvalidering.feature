# language: sv
@FALTVALIDERING @TS
Egenskap: Fältvalidering TS

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med personnummer "190007179815"
	
@TS_BAS
Scenario: Kontrollera att rätt mängd valideringsfel visas när man klickat i alternativ som gett maximalt antal obligatoriska fält.
	Givet jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	
	När jag gör val för att få fram maximalt antal fält i "Transportstyrelsens läkarintyg"

	Och jag klickar på signera-knappen

	Så ska "10" valideringsfel visas med texten "Fältet får inte vara tomt."
	Och ska "22" valideringsfel visas med texten "Du måste välja ett alternativ."
	Och ska "1" valideringsfel visas med texten "Du måste välja minst ett alternativ."


@TS_DIABETES
Scenario: Validerar alla fält i TS diabetes intyget
	Givet jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
	
	När jag gör val för att få fram maximalt antal fält i "Transportstyrelsens läkarintyg, diabetes"
	
	Och jag klickar på signera-knappen

	Så ska "10" valideringsfel visas med texten "Fältet får inte vara tomt."
	Så ska "9" valideringsfel visas med texten "Du måste välja ett alternativ."
	Så ska "6" valideringsfel visas med texten "Måste ligga i intervallet 0,0 till 2,0."
	Så ska "1" valideringsfel visas med texten "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än patientens födelseår."