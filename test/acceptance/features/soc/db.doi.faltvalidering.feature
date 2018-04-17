# language: sv
@FALTVALIDERING @DB @DOI
Egenskap: Fältvalidering för DB/DOI

Bakgrund: DB/DOI måste vara rensat för patienten.
    Givet jag har raderat alla intyg och utkast för "Fältvalidering" testpatienten

@F.VAL-036
@F.VAL-037
Scenariomall: Dödsdatum måste väljas
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "Fältvalidering"
    Och jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Du måste välja datum."
    När jag väljer alternativet "Ej säkert" i frågan "Dödsdatum"
    Så ska "1" valideringsfel visas med texten "Du måste ange år och månad."
    När jag väljer "2018" i dropdownen "År"
    Så ska "1" valideringsfel visas med texten "Du måste ange månad."

Exempel:
    | intyg           |
    | Dödsbevis       |
    | Dödsorsaksintyg |


@F.VAL-038
Scenariomall: Anträffad död kan inte vara tidigare än dödsdatum
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "Fältvalidering"
    Och jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Ej säkert" i frågan "Dödsdatum"
    Och jag anger dagens datum som dödsdatum
    Och jag anger ett tidigare datum för anträffad död
    Och jag klickar på signera-knappen
    Och pausa
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara tidigare än "Dödsdatum""

Exempel:
    | intyg           |
    | Dödsbevis       |
    | Dödsorsaksintyg |

@F.VAL-040
Scenariomall: Dödsdatum kan inte vara i framtiden
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "Fältvalidering"
    Och jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag anger ett dödsdatum i framtiden
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Datumet får inte vara senare än dagens datum."

Exempel:
    | intyg           |
    | Dödsbevis       |
    | Dödsorsaksintyg |
