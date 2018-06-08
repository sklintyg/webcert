# language: sv

@TS @DIABETES
Egenskap: Hantera Transportstyrelsens diabetesintyg

Bakgrund: Jag befinner mig på webcerts förstasida
    Givet att jag är inloggad som läkare
    När jag går in på en patient

@SIGNERA @SMOKE
Scenario: Skapa och signera ett TS diabetes intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg diabetes" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska intygets första status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

@SKICKA @SMOKE @MI-F010
Scenario: Skicka ett befintligt intyg till Transportstyrelsen
	När jag går in på ett "Transportstyrelsens läkarintyg diabetes" med status "Signerat"
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets första status vara "Intyget är skickat till Transportstyrelsen"

    När jag går till Mina intyg för patienten
    Så ska intygets status i Mina intyg visa "Skickat till Transportstyrelsen"

@MAKULERA @SMOKE
Scenario: Makulera ett skickat TS Diabetes intyg
	När jag går in på ett "Transportstyrelsens läkarintyg diabetes" med status "Skickat"
    Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"

    När jag går till Mina intyg för patienten
    Så ska intyget inte finnas i Mina intyg
