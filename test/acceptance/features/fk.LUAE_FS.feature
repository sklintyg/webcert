# language: sv
@smoke @nedsattarbetsformaga @luaefs
Egenskap: Hantera Läkarutlåtande för aktivitetsersättning vid förlängd skolgång

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare
	När jag går in på en patient

@keepIntyg @signera
Scenario: Skapa och signera ett intyg
	När jag går in på att skapa ett "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"

	När jag går till Mina intyg för patienten
	Så ska intyget finnas i Mina intyg

	#När jag går in på intyget i Mina intyg
	#Så ska intygets information i Mina intyg vara den jag angett

@minaintyg @keepIntyg @intygTillFK @skicka
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	När jag går in på ett "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system."

	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Mottaget av Försäkringskassans system"

@makulera
Scenario: Makulera ett skickat intyg
	När jag går in på ett "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" med status "Mottaget"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"

	När jag går till Mina intyg för patienten
	Så ska intygets status i Mina intyg visa "Makulerat"

#@samtidaanvandare @notReady
#Scenario: Samtida användare ska generera felmeddelande
	#När jag går in på att skapa ett "Läkarutlåtande för aktivitetsersättning vid förlängd skolgång" intyg
	#Och sedan öppnar intyget i två webbläsarinstanser
	#Så ska ett felmeddelande visas
