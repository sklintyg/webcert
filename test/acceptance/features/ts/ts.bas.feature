# language: sv

@ts @bas
Egenskap: Hantera Transportstyrelsens basintyg

Bakgrund: Jag är inloggad
    Givet att jag är inloggad som läkare
    När jag går in på en patient

@tsbas @keepIntyg @signera @smoke
Scenario: Skapa och signera ett intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

@keepIntyg @skicka @smoke
Scenario: Skicka ett signerat intyg till Transportstyrelsen
    När jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat"
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets status vara "Intyget är signerat och har skickats till Transportstyrelsens system"

@makulera @smoke
Scenario: Makulera ett skickat intyg
	När jag går in på ett "Transportstyrelsens läkarintyg" med status "Skickat"
    Så ska intygets status vara "Intyget är signerat, skickat och mottaget av Transportstyrelsens system"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Intyget är makulerat"
    
    När jag går till Mina intyg för patienten
    Så ska intyget inte finnas i Mina intyg

@saknatFalt
Scenario: Validera uteblivna fält i intyget
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag raderar ett  slumpat obligatoriskt fält
    Och jag klickar på signera-knappen
    Så ska jag se en rubrik med texten "Utkastet saknar uppgifter i följande avsnitt"
    Och ska jag se en lista med vad som saknas
      

@fornya @signera
Scenario: Förnya ett signerat intyg
    När jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
    Och jag fyller i alla nödvändiga fält för intyget
    Och jag signerar intyget
    Och jag förnyar intyget
    Och jag signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget
