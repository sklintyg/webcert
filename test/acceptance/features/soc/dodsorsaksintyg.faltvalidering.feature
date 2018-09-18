# language: sv
@FALTVALIDERING @DOI 
Egenskap: Fältvalidering för DOI

Bakgrund: DOI måste vara rensat för patienten.
    Givet jag har raderat alla intyg och utkast för "andra" "Dödsorsaksintyg" testpatienten
    Och att jag är inloggad som läkare
    Och jag går in på "andra" testpatienten för "Dödsorsaksintyg"
    Och jag går in på att skapa ett "Dödsorsaksintyg" intyg

@F.VAL-039
Scenario: Operationsdatum får inte vara senare än anträffad död
    När jag väljer alternativet "Ej säkert" i frågan "Dödsdatum"
    Och jag väljer "2018" i dropdownen "År"
    Och jag väljer "03" i dropdownen "Månad"
    Och jag fyller i "2018-03-01" i fältet "Anträffad död"
    Och jag väljer alternativet "Ja" i frågan "Opererad inom 4 veckor före döden?"
    Och jag fyller i "2018-04-01" i fältet "Operationsdatum"
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Ange ett datum, samma som eller tidigare än "Anträffad död"."

@F.VAL-048 @WAITINGFORFIX @UTR-2014 @INTYG-5683
Scenario: Operationsdatum får inte anges tidigare än 4 veckor före dödsdatum
    När jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag fyller i "2018-04-16" i fältet "Datum"
    Och jag väljer alternativet "Ja" i frågan "Opererad inom 4 veckor före döden?"
    Och jag fyller i "2018-03-01" i fältet "Operationsdatum"
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara tidigare än fyra veckor före döden."

@F.VAL-050
@F.VAL-051
Scenario: Datum för en föregående dödsorsak kan inte vara senare än datumet för en efterkommande
    När jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag fyller i "2018-04-16" i fältet "Datum"
    Och jag anger dödsorsaker med datum i stigande ordning
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara senare än datumet för den terminala dödsorsaken."
    Så ska "2" valideringsfel visas med texten "Datumet får inte vara senare än datumet för sjukdomen eller skadan som angavs under den föregående "Som var en följd av"."

@F.VAL-054 @WAITINGFORFIX @UTR-2014 @INTYG-5683
Scenario: Dödsdatum får inte vara före 1 januari föregående år
    När jag väljer alternativet "Ja" i frågan "Dödsfall i samband med skada/förgiftning?"
    Och jag anger 31 december förrförra året som skada/förgiftnings-datum
    Och jag väljer alternativet "Ja" i frågan "Opererad inom 4 veckor före döden?"
    Och jag anger 31 december förrförra året som operationsdatum
    Så ska "2" valideringsfel visas med texten "Datumet får inte vara tidigare än 1 januari föregående året."
