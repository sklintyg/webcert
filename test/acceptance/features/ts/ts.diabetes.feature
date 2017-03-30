# language: sv

@ts @diabetes
Egenskap: Hantera Transportstyrelsens diabetesintyg

Bakgrund: Jag befinner mig på webcerts förstasida
    Givet att jag är inloggad som läkare
    När jag går in på en patient

@keepIntyg @signera @smoke
Scenario: Skapa och signera ett intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

@keepIntyg @skicka @smoke
Scenario: Skicka ett befintligt intyg till Transportstyrelsen
	När jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat"
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets status vara "Intyget är signerat och har skickats till Transportstyrelsens system"

    När jag går till Mina intyg för patienten
    Så ska intygets status i Mina intyg visa "Skickat till Transportstyrelsen"

@makulera @smoke
Scenario: Makulera ett skickat intyg
	När jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Mottaget"
    Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"

    När jag går till Mina intyg för patienten
    Så ska intygets status i Mina intyg visa "Makulerat"


@kopiera @signera
Scenario: Kopiera ett signerat intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag kopierar intyget
    Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget
