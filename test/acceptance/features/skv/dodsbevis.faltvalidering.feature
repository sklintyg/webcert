# language: sv
@FALTVALIDERING @DB @WAITINGFORFIX @TI-594
Egenskap: Fältvalidering för DB

Bakgrund: DB måste vara rensat för patienten.
    Givet jag har raderat alla intyg och utkast för "andra" "Dödsbevis" testpatienten
    Och att jag är inloggad som läkare
    Och jag går in på "andra" testpatienten för "Dödsbevis"
    Och jag går in på att skapa ett "Dödsbevis" intyg

@F.VAL-038
Scenario: Undersökningsdatum kan inte vara efter säkert dödsdatum
    Givet jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag fyller i "2018-04-18" i fältet "Datum"
    Och jag väljer alternativet "Nej, den avlidne undersökt kort före döden" i frågan "Har yttre undersökning av kroppen genomförts?"
    När jag fyller i "2018-04-19" i fältet "Undersökningsdatum"
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara senare än "Dödsdatum"."

@F.VAL-039
Scenario: Undersökningsdatum kan inte vara efter ej säkert dödsdatum
    Givet jag väljer alternativet "Ej säkert" i frågan "Dödsdatum"
    Och jag väljer "2018" i dropdownen "År"
    Och jag väljer "03" i dropdownen "År"
    Och jag fyller i "2018-03-19" i fältet "Anträffad död"
    Och jag väljer alternativet "Nej, den avlidne undersökt kort före döden" i frågan "Har yttre undersökning av kroppen genomförts?"
    När jag fyller i "2018-04-19" i fältet "Undersökningsdatum"
    När jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara senare än datumet för "Anträffad död"."

@F.VAL-054 @WAITINGFORFIX @UTR-2014 @INTYG-5683
Scenario: Undersökningsdatum får inte vara före 1 januari föregående år
    Givet jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag fyller i "2018-03-19" i fältet "Datum"
    Och jag väljer alternativet "Nej, den avlidne undersökt kort före döden" i frågan "Har yttre undersökning av kroppen genomförts?"
    När jag fyller i "2016-12-31" i fältet "Undersökningsdatum"
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara tidigare än 1 januari föregående året."
