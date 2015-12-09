# language: sv

@smoke @webcert @ts 
Egenskap: Kontrollera att det går att skapa intyg för transportstyrelsen

Bakgrund: Jag befinner mig på webcerts förstasida
    Givet att jag är inloggad som läkare "Jan Nilsson"

Scenario: Skapa och signera ett intyg till transportstyrelsen
    När jag väljer patienten "19121212-1212"
    Och jag går in på att skapa ett "Transportstyrelsens läkarintyg" intyg
    Och jag fyller i alla nödvändiga fält för ett läkarintyg
    Och signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

Scenario: Skicka ett befintligt intyg-MIN till Transportstyrelsen
När jag väljer patienten "19121212-1212"
    Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Signerat" 
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets status vara "Intyget är signerat och har skickats till Transportstyrelsens system"

Scenario: Makulera ett skickat intyg
	När jag väljer patienten "19121212-1212"
    Och jag går in på ett "Transportstyrelsens läkarintyg" med status "Mottaget" 
    Så ska intygets status vara "Intyget är signerat och mottaget av Transportstyrelsens system"
	Och jag makulerar intyget
	Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"
