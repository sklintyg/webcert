# language: sv
@FALTVALIDERING 
Egenskap: Generella valideringsegenskaper som gäller samtliga intyg

Bakgrund: Rensar gamla intyg för patienten.
    Givet jag har raderat alla intyg och utkast för "fältvalidering" testpatienten


@WC-F006 @GIK-005 @GIK-001b 
Scenariomall: <intyg> - Obligatoriska avsnitt som inte är ifyllda ska visas i lista med avsnitt som saknar uppgifter.
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "fältvalidering"
    Och jag går in på att skapa ett "<intyg>" intyg
    Och att textfält i intyget är rensade
    När jag klickar på signera-knappen
    Så ska alla sektioner innehållandes valideringsfel listas
    Och ska statusmeddelande att obligatoriska uppgifter saknas visas
    När jag fyller i alla nödvändiga fält för intyget "<intyg>"
    Så ska inga valideringsfel listas
    Och ska statusmeddelande att intyget är klart att signera visas
    Och ska inga asterisker finnas

Exempel:
  | intyg                                                             |
  | Transportstyrelsens läkarintyg högre körkortsbehörighet           |
  | Transportstyrelsens läkarintyg diabetes                           |
  | Dödsbevis                                                         |
  | Dödsorsaksintyg                                                   |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för sjukersättning                                 |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |
  
@TOMT-ELEMENT @WAITINGFORFIX @INTYG-6451
Scenariomall: <intyg> - valideringsfel vid tomma fält 
  Givet att jag är inloggad som läkare
  Och jag går in på testpatienten för "fältvalidering"
  Och jag går in på att skapa ett "<intyg>" intyg
  Och att textfält i intyget är rensade
  Och jag gör val för att få fram maximalt antal fält i "<intyg>"
  Och jag klickar på signera-knappen
  
  Så ska alla "tomma fält" valideringsfel för "<intyg>" visas
  Och ska alla valideringsmeddelanden finnas med i listan över godkända meddelanden

Exempel:
  | intyg                                                             |
  | Transportstyrelsens läkarintyg högre körkortsbehörighet           |
  | Transportstyrelsens läkarintyg diabetes                           |
  | Dödsbevis                                                         |
  | Dödsorsaksintyg                                                   |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för sjukersättning                                 |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |
  
  
  
@OTILLÅTEN-INPUT @WAITINGFORFIX @INTYG-6451
Scenariomall: <intyg> - valideringsfel vid otillåten input 
  Givet att jag är inloggad som läkare
  Och jag går in på testpatienten för "fältvalidering"
  Och jag går in på att skapa ett "<intyg>" intyg 
  Och att textfält i intyget är rensade
  Och jag gör val för att få fram maximalt antal fält i "<intyg>"
  Och jag fyller i textfält med felaktiga värden i "<intyg>"
  Och jag klickar på signera-knappen 

  Så ska alla "otillåten input" valideringsfel för "<intyg>" visas
  Och ska alla valideringsmeddelanden finnas med i listan över godkända meddelanden
  
Exempel:
  | intyg                                                             |
  | Transportstyrelsens läkarintyg högre körkortsbehörighet           |
  | Transportstyrelsens läkarintyg diabetes                           |
  | Dödsbevis                                                         |
  | Dödsorsaksintyg                                                   |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för sjukersättning                                 |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |