# language: sv

@smoke @webcert @ts @diabetes-min 
Egenskap: Kontrollera att det går att skapa diabetes-intyg för transportstyrelsen

Bakgrund: Jag befinner mig på webcerts förstasida
    Givet att jag är inloggad som läkare "Jan Nilsson"

Scenario: Skapa och signera ett diabetesintyg till transportstyrelsen
    När jag väljer patienten "19121212-1212"
    Och jag går in på att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
    Och jag fyller i alla nödvändiga fält för ett Diabetes-intyg
    Och signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

Scenario: Skicka ett befintligt diabetesintyg-MIN till Transportstyrelsen
	När jag väljer patienten "19121212-1212"
    Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Signerat" 
    Och jag skickar intyget till Transportstyrelsen
    Så ska intygets status vara "Intyget är signerat och har skickats till Transportstyrelsens system"

    När jag går till Mina intyg för patienten "19121212-1212"
    Så ska intygets status i Mina intyg visa "Mottaget av Transportstyrelsens system"

@RevokeMedicalCertificate
Scenario: Makulera ett skickat intyg
	När jag väljer patienten "19121212-1212"
    Och jag går in på ett "Transportstyrelsens läkarintyg, diabetes" med status "Mottaget"
    Och jag makulerar intyget
	Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"

    När jag går till Mina intyg för patienten "19121212-1212"
    Så ska intygets status i Mina intyg visa "Makulerat"
