# language: sv
@FALT_VALIDERING
Egenskap: Generella valideringsegenskaper som gäller samtliga intyg

@WIP
Scenariomall: Alla sektioner som är markerade med obligatoriska fält ska generera valideringsfel när de inte är ifyllda.
    Givet att jag är inloggad som läkare
    Och jag går in på en patient
    Och jag går in på att skapa ett "<intyg>" intyg
    Och att textfält i intyget är rensade
    När jag klickar på signera-knappen
    Så ska alla valideringsfel visas

Exempel:
  | intyg                                                             |
  | Transportstyrelsens läkarintyg                                    |
  | Transportstyrelsens läkarintyg, diabetes                          |
  | Dödsbevis                                                         |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |