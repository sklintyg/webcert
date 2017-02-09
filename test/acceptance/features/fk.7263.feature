# language: sv
@smoke @fk7263
Egenskap: Hantera FK7263-intyg

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@RegisterMedicalCertificate @minaintyg @keepIntyg @signera
Scenario: Skapa och signera ett intyg
	När jag går in på att skapa ett "Läkarintyg FK 7263" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"
	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg

@SendMedicalCertificate @minaintyg @keepIntyg @intygTillFK @skicka
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system"
	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Skickat till Försäkringskassan"

@RevokeMedicalCertificate
Scenario: Makulera ett skickat intyg
	När jag går in på ett "Läkarintyg FK 7263" med status "Mottaget"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"
	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Makulerat"

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
Scenariomall: Samtida användare ska generera felmeddelande (<intygKod>)
	När jag går in på att skapa ett <intyg> intyg
	Och sedan öppnar intyget i två webbläsarinstanser
	Så ska ett felmeddelande visas

Exempel:
	|	intygKod   | 	intyg					|
	|	FK7263     | 	"Läkarintyg FK 7263" 	|

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
