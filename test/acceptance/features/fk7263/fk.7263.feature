# language: sv
@fk7263
Egenskap: Hantera FK7263-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@minaintyg @keepIntyg @signera @smoke
Scenario: Skapa och signera ett intyg
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"
	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg

@minaintyg @keepIntyg @intygTillFK @skicka
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system"
	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Skickat till Försäkringskassan"

@smoke
Scenario: Makulera ett skickat intyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Skickat"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"
	När jag går till Mina intyg för patienten
    Så ska intyget inte finnas i Mina intyg

@kopiera
Scenario: Kopiera ett signerat intyg
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag kopierar intyget
	Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

@samtidaanvandare
Scenario: Samtida användare ska generera felmeddelande (FK7263)
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och sedan öppnar intyget i två webbläsarinstanser
	Så ska ett felmeddelande visas

@samtidaanvandare @skicka-makulera
Scenario: Samtida användare ska generera felmeddelande om fråga/svar skickas efter makulering (FK7263)
		När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag klickar på signera-knappen
		Och sedan öppnar intyget i två webbläsarinstanser
		Och jag klickar på skicka knappen
		Och jag makulerar intyget
		Och jag skickar en fråga till Försäkringskassan
		Så ska varningen "Ett tekniskt problem inträffade" visas

@samtidaanvandare @makulera-skicka
Scenario: Samtida användare ska generera felmeddelande om man skickar efter makulering (FK7263)
		När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag klickar på signera-knappen
		Och sedan öppnar intyget i två webbläsarinstanser
		Och jag makulerar intyget
		Så ska varningen "Kunde inte skicka intyget" visas om man försöker skicka intyget i andra webbläsarinstansen

@samtidaanvandare @fornya
Scenario: Samtida användare ska generera felmeddelande om man förnyar efter makulering (FK7263)
		När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
		Och jag fyller i alla nödvändiga fält för intyget
		Och jag klickar på signera-knappen
		Och sedan öppnar intyget i två webbläsarinstanser
		Och jag makulerar intyget
		Så ska varningen "Kunde inte förnya intyget" visas om man försöker förnya intyget i andra webbläsarinstansen

@samtidaanvandare @makulera-kopiera
Scenario: Samtida användare ska generera felmeddelande om man kopiera efter makulering (FK7263)
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag klickar på signera-knappen
	Och sedan öppnar intyget i två webbläsarinstanser
	Och jag makulerar intyget
	Så ska varningen "Kunde inte kopiera intyget" visas om man försöker kopiera intyget i andra webbläsarinstansen

# @makulera @ersatt
# Scenario: Makulera ett signerat intyg och ersätt det
#	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
#	Och jag fyller i alla nödvändiga fält för intyget
#	Och jag signerar intyget
#
#	Och jag makulerar intyget och ersätter med nytt intyg
#	Så ska det finnas en referens till gamla intyget
#
#	Och jag går till Mina intyg för patienten
#	Så ska intygets status i Mina intyg visa "Makulerat"
