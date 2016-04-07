# language: sv
@smoke @sjukersattning @luse @waitingForFix
Egenskap: Hantera Läkarutlåtande för sjukersättning

Bakgrund: Jag befinner mig på webcerts förstasida
	Givet att jag är inloggad som läkare

@keepIntyg @foo
Scenario: Skapa och signera ett intyg
	När jag väljer patienten "19520617-2339"
	Och jag går in på att skapa ett "Läkarutlåtande för sjukersättning" intyg
	Och jag fyller i alla nödvändiga fält för intyget
	Och jag signerar intyget
	Och jag ska se den data jag angett för intyget
	Så ska intygets status vara "Intyget är signerat"	
	
	När jag går till Mina intyg för patienten "19520617-2339"
	Så ska intyget finnas i Mina intyg

	När jag går in på intyget i Mina intyg
	Så ska intygets information i Mina intyg vara den jag angett

@minaintyg @keepIntyg @intygTillFK
Scenario: Skicka ett befintligt intyg till Försäkringskassan
	När jag väljer patienten "19520617-2339"
    Och jag går in på ett "Läkarutlåtande för sjukersättning" med status "Signerat"
	Och jag skickar intyget till Försäkringskassan
	Så ska intygets status vara "Intyget är signerat och har skickats till Försäkringskassans system."

	När jag går till Mina intyg för patienten "19520617-2339"
	Så ska intygets status i Mina intyg visa "Mottaget av Försäkringskassans system"

@makulera
Scenario: Makulera ett skickat intyg
	När jag väljer patienten "19520617-2339"
    Och jag går in på ett "Läkarutlåtande för sjukersättning" med status "Mottaget"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"
	
	När jag går till Mina intyg för patienten "19520617-2339"
	Så ska intygets status i Mina intyg visa "Makulerat"
