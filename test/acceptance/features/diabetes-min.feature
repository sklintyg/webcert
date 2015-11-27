# language: sv

@smoke @webcert @ts @diabetes-min
Egenskap: Kontrollera att det går att skapa diabetes--MIN intyg för transportstyrelsen

Bakgrund: Jag befinner mig på webcerts förstasida
    Givet att jag är inloggad som läkare "Jan Nilsson"

@dev
Scenario: Skapa och signera ett diabetesintyg-MIN till transportstyrelsen
    När jag väljer patienten "19121212-1212"
    Och jag går in på  att skapa ett "Transportstyrelsens läkarintyg, diabetes" intyg
    Och jag fyller i alla nödvändiga fält enligt mall för ett Diabetes-MIN-intyg
    Och signerar intyget
    Så ska intygets status vara "Intyget är signerat"
    Och jag ska se den data jag angett för intyget

# Scenario: Skicka ett befintligt diabetesintyg-MIN till Transportstyrelsen
#     Givet att ett intyg är skapat
#     När jag öppnar TS Diabetes intyget
#     Och intyget är signerat
#     Och jag skickar intyget till Transportstyrelsen
#     Så ska intygets status vara "Intyget är signerat och har skickats till Transportstyrelsens system"

#     När jag går till mvk på patienten "19520727-2252"
#     Så ska intygets status i mvk visa "Mottaget av Transportstyrelsens system"

# @RevokeMedicalCertificate
# Scenario: Makulera ett skickat intyg
#     Givet att ett intyg är skapat
#     När jag öppnar TS Diabetes intyget
#     Så ska intygets status vara "Intyget är signerat och mottaget av Transportstyrelsens system."
    
#     Och jag makulerar intyget
#     Så ska jag få en dialogruta som säger "Kvittens - Återtaget intyg"
#     Så ska intyget visa varningen "Begäran om makulering skickad till intygstjänsten"

#     När jag går till mvk på patienten "19520727-2252"
#     Så ska intygets status i mvk visa "Makulerat"

# @mvk @arkivera
# Scenario: Arkivera ett intyg i mvk
#     Givet att ett intyg är skapat
#     När jag går till mvk på patienten "19520727-2252"
#     Och jag arkiverar intyget i mvk
#     Så ska intygets inte visas i mvk