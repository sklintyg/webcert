# language: sv
@faltvalidering @ts_diabets

Egenskap: Fältvalidering TS-Diabetes

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg

@validering_alla_fält_diabetes
Scenario: test test test test test test
	När jag klickar på signera-knappen
	Så ska alla standard valideringsfel för "Transportstyrelsens läkarintyg, diabetes" visas

	
	När jag anger "Nej" i valet "a) Ögonläkarintyg kommer att skickas in separat"
	Och jag anger "Ja" i valet "b) Förekommer hypoglykemier med tecken på nedsatt hjärnfunktion (neuroglukopena symtom) som bedöms kunna innebära en trafiksäkerhetsrisk?"
	Så ska valideringsfelet "Du måste välja ett alternativ." visas "7" gånger
	Och jag anger "Ja" i valet "d) Har patienten haft allvarlig hypoglykemi (som krävt hjälp av annan för att hävas) under det senaste året?"
	Och jag anger "Ja" i valet "e) Har patienten haft allvarlig hypoglykemi i trafiken under det senaste året?"
	Och jag trycker på checkboxen med texten "Insulin"
	Så ska alla utökade valideringsfel för "Transportstyrelsens läkarintyg, diabetes" visas

	När jag fyller i text i "diabetes-årtal" fältet
	Så ska valideringsfelet "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900." visas "2" gånger

	När jag fyller i text i "alla synfält" fältet
	Så ska valideringsfelet "Måste ligga i intervallet 0,0 till 2,0." visas "6" gånger


