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
    Så ska alla sektioner innehållandes valideringsfel listas
    Och ska statusmeddelande att obligatoriska uppgifter saknas visas
    När jag fyller i alla nödvändiga fält för intyget "<intyg>"
    Så ska inga valideringsfel listas
    Och ska statusmeddelande att intyget är klart att signera visas

Exempel:
  | intyg                                                             |
  | Transportstyrelsens läkarintyg                                    |
  | Transportstyrelsens läkarintyg, diabetes                          |
  #| Dödsbevis                                                         |
  #| Dödsorsaksintyg                                                   |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |