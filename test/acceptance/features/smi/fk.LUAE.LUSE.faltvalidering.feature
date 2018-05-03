# language: sv
@FALTVALIDERING @LUAE_FS @LUAE_AF @LUSE
Egenskap: Fältvalidering för LUAE

Bakgrund:
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "fältvalidering"

@F.VAL-012
Scenariomall: Man kan inte undersöka en patient senare än man känt patienten
    Givet jag går in på att skapa ett "<intyg>" intyg
    När jag anger undersökningsdatum senare än patientkännedom
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Får inte vara senare än 'Min undersökning av patienten'."

Exempel:
    | intyg                                                             |
    | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |
    | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
    | Läkarutlåtande för sjukersättning                                 |

@F.VAL-013
Scenariomall: Man kan inte undersöka en patient senare än man känt patienten
    Givet jag går in på att skapa ett "<intyg>" intyg
    När jag anger anhörigs beskrivning senare än patientkännedom
    Och jag klickar på signera-knappen
    Så ska "1" valideringsfel visas med texten "Får inte vara senare än 'Anhörigs beskrivning av patienten'."

Exempel:
    | intyg                                                             |
    | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |
    | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
    | Läkarutlåtande för sjukersättning                                 |

@F.VAL-016
Scenariomall: Man kan inte undersöka en patient senare än man känt patienten
    Givet jag går in på att skapa ett "<intyg>" intyg
    När jag väljer alternativet "Ja" i frågan "Är utlåtandet även baserat på andra medicinska utredningar eller underlag?"
    Och jag väljer "Underlag från logoped" i dropdownen "Ange utredning eller underlag" 
    Och jag klickar på signera-knappen
    Så ska "3" valideringsfel visas med texten "Du måste ange datum för underlaget."

Exempel:
    | intyg                                                             |
    | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |
    | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
    | Läkarutlåtande för sjukersättning                                 |
