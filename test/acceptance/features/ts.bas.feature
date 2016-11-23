# language: sv

@smoke @webcert @ts @bas
Egenskap: Hantera Transportstyrelsens basintyg

Bakgrund: Jag är inloggad
    Givet att jag är inloggad som läkare
    När jag går in på en patient

@tsbas @keepIntyg @signera
Scenario: Skapa och signera ett intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

@keepIntyg @skicka
Scenario: Skicka ett signerat intyg till Transportstyrelsen
    När jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets status vara "Intyget är signerat och har skickats till Transportstyrelsens system"

@makulera
Scenario: Makulera ett skickat intyg
	När jag går in på ett "Transportstyrelsens läkarintyg" med status "Mottaget"
    Så ska intygets status vara "Intyget är signerat, skickat och mottaget av Transportstyrelsens system"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"
