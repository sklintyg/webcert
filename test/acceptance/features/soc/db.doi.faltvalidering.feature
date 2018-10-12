# language: sv
@FALTVALIDERING @DB @DOI 
Egenskap: Fältvalidering för DB/DOI

Bakgrund: DB/DOI måste vara rensat för patienten.
    Givet jag har raderat alla intyg och utkast för "tredje" "Dödsorsaksintyg" testpatienten
    Och att jag är inloggad som läkare
    Och jag går in på "tredje" testpatienten för "Dödsorsaksintyg"

@F.VAL-036
@F.VAL-037 @WAITINGFORFIX @INTYG-6449
Scenariomall: <intyg> - Dödsdatum måste väljas
    Givet jag går in på att skapa ett "<intyg>" intyg
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

@E-05
Scenariomall: <intyg> - Dödsdatum kan inte vara i framtiden
    Givet jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag anger ett säkert dödsdatum i framtiden
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Ange dagens eller ett tidigare datum."


Exempel:
    | intyg           |
    | Dödsbevis       |
    | Dödsorsaksintyg |


@E-08
Scenariomall: <intyg> - Anträffad död kan inte vara tidigare än dödsdatum
    Givet jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Ej säkert" i frågan "Dödsdatum"
    Och jag anger dagens datum som ej säkert dödsdatum
    Och jag anger ett tidigare datum för anträffad död
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Ange ett datum, samma som eller senare än "Dödsdatum"."

Exempel:
    | intyg           |
    | Dödsbevis       |
    | Dödsorsaksintyg |

@E-07 @WAITINGFORFIX @UTR-2014 @INTYG-5683
Scenariomall: <intyg> - Dödsdatum får inte vara före 1 januari föregående år
    Givet jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Säkert" i frågan "Dödsdatum"
    Och jag anger 31 december förrförra året som säkert dödsdatum
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Ange ett datum, samma som eller senare än 1 januari föregående året."
    När jag väljer alternativet "Ej säkert" i frågan "Dödsdatum"
    När jag anger 31 december förrförra året som anträffad död
    Så ska "1" valideringsfel visas med texten "Ange ett datum, samma som eller senare än 1 januari föregående året."

Exempel:
    | intyg           |
    | Dödsbevis       |
    | Dödsorsaksintyg |

