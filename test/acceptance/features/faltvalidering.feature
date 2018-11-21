# language: sv
@FALTVALIDERING
Egenskap: Generella valideringsegenskaper som gäller samtliga intyg

Bakgrund: DB/DOI måste vara rensat för patienten.
    Givet jag har raderat alla intyg och utkast för "Fältvalidering" testpatienten


@WC-F006 @GIK-005 @GIK-001b
Scenariomall: Alla sektioner som är markerade med obligatoriska fält ska generera valideringsfel när de inte är ifyllda.
    Givet att jag är inloggad som läkare
    Och jag går in på testpatienten för "Fältvalidering"
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
  | Transportstyrelsens läkarintyg                                    |
  | Transportstyrelsens läkarintyg, diabetes                          |
  | Dödsbevis                                                         |
  | Dödsorsaksintyg                                                   |
  | Läkarintyg för sjukpenning                                        |
  | Läkarutlåtande för sjukersättning                                 |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |

Scenariomall: Expanderar intyget så maximalt antal fält blir synliga och kontrollera att olika typer av valideringsfel visas för olika fält i olika intyg.
  Givet att jag är inloggad som läkare
  Och jag går in på testpatienten för "Fältvalidering"
  Och jag går in på att skapa ett "<intyg>" intyg
  Och att textfält i intyget är rensade
  
  När jag gör val för att få fram maximalt antal fält i "<intyg>"
  Och jag klickar på signera-knappen

  Så ska alla valideringsmeddelanden finnas med i listan över godkända meddelanden
  Och ska "<tomt>" valideringsfel visas med texten "Fältet får inte vara tomt."
  Och ska "<postnummer>" valideringsfel visas med texten "Postnummer måste anges med fem siffror."
  Och ska "<ett_alternativ>" valideringsfel visas med texten "Du måste välja ett alternativ."
  Och ska "<minst_ett_alternativ>" valideringsfel visas med texten "Du måste välja minst ett alternativ."
  Och ska "<diagnos>" valideringsfel visas med texten "Minst en diagnos måste anges."
  Och ska "<åtgärder>" valideringsfel visas med texten "Åtgärder måste väljas eller Inte aktuellt."
  Och ska "<minst_en_rad>" valideringsfel visas med texten "Minst en rad måste fyllas i."
  Och ska "<datum>" valideringsfel visas med texten "Du måste välja datum."
  Och ska "<arbetstid>" valideringsfel visas med texten "Arbetstidsförläggning måste fyllas i om period 75%, 50% eller 25% har valts."

  När jag fyller i textfält med felaktiga värden i "<intyg>"
  Och jag klickar på signera-knappen 

  Så ska alla valideringsmeddelanden finnas med i listan över godkända meddelanden
  Och ska "<synintervall>" valideringsfel visas med texten "Måste ligga i intervallet 0,0 till 2,0."
  Och ska "<år_födelse>" valideringsfel visas med texten "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än patientens födelseår."
  Och ska "<år_1900>" valideringsfel visas med texten "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900."
  Och ska "<datum_format>" valideringsfel visas med texten "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"
  Och ska "<datum_hypoglykemi>" valideringsfel visas med texten "Tidpunkt för allvarlig hypoglykemi under vaken tid måste anges som åååå-mm-dd, och får inte vara tidigare än ett år tillbaka eller senare än dagens datum."
  Och ska "<underlag>" valideringsfel visas med texten "Du måste ange ett underlag."
  Och ska "<utredning_info>" valideringsfel visas med texten "Du måste ange var Försäkringskassan kan få information om utredningen."
  Och ska "<funk_debut>" valideringsfel visas med texten "Funktionsnedsättningens debut och utveckling måste fyllas i."
  Och ska "<funk_påverkan>" valideringsfel visas med texten "Funktionsnedsättningens påverkan måste fyllas i."
  Och ska "<postnummer>" valideringsfel visas med texten "Postnummer måste anges med fem siffror."
  
Exempel:
  | intyg                                                             | tomt | ett_alternativ | minst_ett_alternativ | synintervall | år_födelse  | år_1900 | postnummer | datum_format | datum_hypoglykemi  | diagnos | åtgärder | minst_en_rad | underlag | utredning_info | funk_debut | funk_påverkan | datum | arbetstid |
  | Läkarintyg för sjukpenning                                        | 12   | 1              | 0                    | 0            | 0           | 0       | 1          | 12           | 0                  | 1       | 1        | 0            | 0        | 0              | 0          | 0             | 0     | 1         |
  | Transportstyrelsens läkarintyg, diabetes                          | 10   | 9              | 0                    | 6            | 1           | 1       | 2          | 0            | 1                  | 0       | 0        | 0            | 0        | 0              | 0          | 0             | 1     | 0         |
  | Transportstyrelsens läkarintyg                                    | 15   | 22             | 1                    | 0            | 0           | 0       | 2          | 0            | 0                  | 0       | 0        | 0            | 0        | 0              | 0          | 0             | 0     | 0         |
  | Läkarutlåtande för sjukersättning                                 | 10   | 0              | 1                    | 0            | 0           | 0       | 1          | 8            | 0                  | 1       | 0        | 1            | 3        | 3              | 0          | 0             | 1     | 0         |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     | 5    | 0              | 0                    | 0            | 0           | 0       | 1          | 8            | 0                  | 1       | 0        | 1            | 3        | 3              | 1          | 1             | 1     | 0         |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga | 10   | 0              | 1                    | 0            | 0           | 0       | 1          | 8            | 0                  | 1       | 0        | 1            | 3        | 3              | 0          | 0             | 1     | 0         |
  | Dödsbevis                                                         | 7    | 4              | 0                    | 0            | 0           | 0       | 2          | 2            | 0                  | 0       | 0        | 0            | 0        | 0              | 0          | 0             | 2     | 0         |
  | Dödsorsaksintyg                                                   | 9    | 3              | 1                    | 0            | 0           | 0       | 2          | 7            | 0                  | 0       | 0        | 0            | 0        | 0              | 0          | 0             | 3     | 0         |



