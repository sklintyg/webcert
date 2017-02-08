# language: sv
@faltvalidering @ts @notReady

Egenskap: Fältvalidering TS-Bas

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient
	När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg

@validering_alla_fält_TSbas
Scenario: Validerar alla fält i TS diabetes intyget
	När jag klickar på signera-knappen
	Så ska alla standard valideringsfel för "Transportstyrelsens läkarintyg" visas

	När jag anger "Ja" i valet "Har patienten diabetes?"
	Så ska valideringsfelet "Du måste välja ett alternativ." visas "23" gånger

	När jag trycker på checkboxen med texten "Taxi"
	Och jag anger "Ja" i valet "a) Har patienten någon sjukdom eller funktionsnedsättning som påverkar rörligheten och som medför att fordon inte kan köras på ett trafiksäkert sätt?"
	Och jag anger "Ja" i valet "c) Föreligger viktiga riskfaktorer för stroke (tidigare stroke eller TIA, förhöjt blodtryck, förmaksflimmer eller kärlmissbildning)?"
	Och jag anger "Typ 2" i valet "Vilken typ?"
	Och jag anger "Ja" i valet "a) Finns journaluppgifter, anamnestiska uppgifter, resultat av laboratorieprover eller andra tecken på missbruk eller beroende av alkohol, narkotika eller läkemedel?"
	Och jag anger "Ja" i valet "c) Pågår regelbundet läkarordinerat bruk av läkemedel som kan innebära en trafiksäkerhetsrisk?"
	Och jag anger "Ja" i valet "Har patienten vårdats på sjukhus eller haft kontakt med läkare med anledning av fälten 1-13?"
	Och jag anger "Ja" i valet "Har patienten någon stadigvarande medicinering?"
	Så ska alla utökade valideringsfel för "Transportstyrelsens läkarintyg" visas

	När jag fyller i text i "alla synfält" fältet
	Och jag byter fokus från fält
	Så ska valideringsfelet "Måste ligga i intervallet 0,0 till 2,0." visas "3" gånger

	När jag tar bort information i "synfälten" fältet
	Och jag fyller i alla nödvändiga fält för intyget
    Och jag byter fokus från fält
    Så ska inga valideringsfel visas