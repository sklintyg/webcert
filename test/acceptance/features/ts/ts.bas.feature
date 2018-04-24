# language: sv

@ts @bas
Egenskap: Hantera Transportstyrelsens basintyg

Bakgrund: Jag är inloggad
    Givet att jag är inloggad som läkare
    När jag går in på en patient

@tsbas @keepIntyg @signera @smoke
Scenario: Skapa och signera ett TS bas intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska intygets första status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

@keepIntyg @skicka @smoke
Scenario: Skicka ett signerat intyg till Transportstyrelsen
    När jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets första status vara "Intyget är skickat till Transportstyrelsen"

@makulera @smoke
Scenario: Makulera ett skickat TS Bas intyg
	När jag går in på ett "Transportstyrelsens läkarintyg" med status "Skickat"
    Så ska intygets första status vara "Intyget är skickat till Transportstyrelsen"
	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"
    
    När jag går till Mina intyg för patienten
    Så ska intyget inte finnas i Mina intyg
