# language: sv

@TS @KORKORTSBEHORIGHET
Egenskap: Hantera Transportstyrelsens basintyg

Bakgrund: Jag är inloggad
    Givet att jag är inloggad som läkare
    När jag går in på en patient

@tsbas  @SIGNERA @SMOKE
Scenario: Skapa och signera ett TS bas intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg högre körkortsbehörighet" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska intygets första status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

 @SKICKA @SMOKE
Scenario: Skicka ett signerat intyg till Transportstyrelsen
    När jag går in på ett "Transportstyrelsens läkarintyg högre körkortsbehörighet" med status "Signerat"
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets första status vara "Intyget är skickat till Transportstyrelsen"

@MAKULERA @SMOKE
Scenario: Makulera ett skickat TS Bas intyg
	När jag går in på ett "Transportstyrelsens läkarintyg högre körkortsbehörighet" med status "Skickat"
    Så ska intygets första status vara "Intyget är skickat till Transportstyrelsen"
	Och jag makulerar intyget
	Så ska intygets första status vara "Intyget är makulerat"
    
    När jag går till Mina intyg för patienten
    Så ska intyget inte finnas i Mina intyg
