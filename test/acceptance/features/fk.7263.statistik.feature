# language: sv

@statistik @integration @fk7263
Egenskap: FK7263-integration med Statistiktjänsten

#Bakgrund: Jag befinner mig på Rehabstöds förstasida

@skapa @databas
Scenario: Ett nyskapat FK7263 intyg ska finnas i Statistiktjänstens databas
    När att jag är inloggad som läkare
	Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
    Så ska jag se intyget i databasen
