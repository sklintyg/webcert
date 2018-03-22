# language: sv
@FALTVALIDERING @TS
Egenskap: Fältvalidering TS

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	Och jag går in på en patient med personnummer "190007179815"
	

Scenario: Kontrollera att rätt mängd valideringsfel visas när man klickat i alternativ som gett maximalt antal obligatoriska fält.
	Givet jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
	
	När jag anger "Ja" i valet "Har patienten diabetes?"
	
	Och jag anger "Typ 2" i valet "Vilken typ?"
	Och jag anger "Ja" i valet "a) Har patienten någon sjukdom eller funktionsnedsättning som påverkar rörligheten och som medför att fordon inte kan köras på ett trafiksäkert sätt?"
	Och jag anger "Ja" i valet "c) Föreligger viktiga riskfaktorer för stroke (tidigare stroke eller TIA, förhöjt blodtryck, förmaksflimmer eller kärlmissbildning)?"
	Och jag anger "Ja" i valet "a) Finns journaluppgifter, anamnestiska uppgifter, resultat av laboratorieprover eller andra tecken på missbruk eller beroende av alkohol, narkotika eller läkemedel?"
	Och jag anger "Ja" i valet "c) Pågår regelbundet läkarordinerat bruk av läkemedel som kan innebära en trafiksäkerhetsrisk?"
	Och jag anger "Ja" i valet "Har patienten vårdats på sjukhus eller haft kontakt med läkare med anledning av fälten 1-13?"
	Och jag anger "Ja" i valet "Har patienten någon stadigvarande medicinering?"

	Och jag klickar på signera-knappen

	Så ska "10" valideringsfel visas med texten "Fältet får inte vara tomt."
	Och ska "21" valideringsfel visas med texten "Du måste välja ett alternativ."
	Och ska "1" valideringsfel visas med texten "Du måste välja minst ett alternativ."
