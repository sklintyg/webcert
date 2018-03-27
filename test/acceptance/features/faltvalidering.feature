# language: sv
@FALTVALIDERING
Egenskap: Generella valideringsegenskaper som gäller samtliga intyg

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
  | Läkarutlåtande för sjukersättning                                 |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     |
  | Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga |

@F.VAL-030 @F.VAL-001
Scenariomall: Expanderar intyget så maximalt antal fält blir synliga och kontrollera att olika typer av valideringsfel visas för olika fält i olika intyg.
  Givet att jag är inloggad som läkare
  Och jag går in på en patient med personnummer "190007179815"
  Och jag går in på att skapa ett "<intyg>" intyg
  
  När jag gör val för att få fram maximalt antal fält i "<intyg>"
  Och jag klickar på signera-knappen

  Så ska "<tomt>" valideringsfel visas med texten "Fältet får inte vara tomt."
  Och ska "<ett_alternativ>" valideringsfel visas med texten "Du måste välja ett alternativ."
  Och ska "<minst_ett_alternativ>" valideringsfel visas med texten "Du måste välja minst ett alternativ."
  Och ska "<diagnos>" valideringsfel visas med texten "Minst en diagnos måste anges."
  Och ska "<åtgärder>" valideringsfel visas med texten "Åtgärder måste väljas eller Inte aktuellt."
  Och ska "<minst_en_rad>" valideringsfel visas med texten "Minst en rad måste fyllas i."

  När jag fyller i textfält med felaktiga värden i "<intyg>"
  Och jag klickar på signera-knappen
  
  Så ska "<synintervall>" valideringsfel visas med texten "Måste ligga i intervallet 0,0 till 2,0."
  Och ska "<år_födelse>" valideringsfel visas med texten "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än patientens födelseår."
  Och ska "<år_1900>" valideringsfel visas med texten "År måste anges enligt formatet ÅÅÅÅ. Det går inte att ange årtal som är senare än innevarande år eller tidigare än år 1900."
  Och ska "<datum>" valideringsfel visas med texten "Datum behöver skrivas på formatet ÅÅÅÅ-MM-DD"
  Och ska "<datum_hypoglykemi>" valideringsfel visas med texten "Tidpunkt för allvarlig hypoglykemi under vaken tid måste anges som åååå-mm-dd, och får inte vara tidigare än ett år tillbaka eller senare än dagens datum."
  Och ska "<underlag>" valideringsfel visas med texten "Du måste ange ett underlag."
  Och ska "<utredning_info>" valideringsfel visas med texten "Du måste ange var Försäkringskassan kan få information om utredningen."
  Och ska "<funk_debut>" valideringsfel visas med texten "Funktionsnedsättningens debut och utveckling måste fyllas i."
  Och ska "<funk_påverkan>" valideringsfel visas med texten "Funktionsnedsättningens påverkan måste fyllas i."
  
Exempel:
  | intyg                                                             | tomt | ett_alternativ | minst_ett_alternativ | synintervall | år_födelse  | år_1900 | postnummer | datum | datum_hypoglykemi  | diagnos | åtgärder | minst_en_rad | underlag | utredning_info | funk_debut | funk_påverkan |
  | Läkarintyg för sjukpenning                                        | 13   | 1              | 0                    | 0            | 0           | 0       | 2          | 12    | 0                  | 1       | 1        | 0            | 0        | 0             | 0          | 0             |
  | Transportstyrelsens läkarintyg, diabetes                          | 9    | 9              | 0                    | 6            | 1           | 1       | 2          | 0     | 1                  | 0       | 0        | 0            | 0        | 0             | 0          | 0             |
  | Transportstyrelsens läkarintyg                                    | 14   | 22             | 1                    | 0            | 0           | 0       | 2          | 0     | 0                  | 0       | 0        | 0            | 0        | 0             | 0          | 0             |
  | Läkarutlåtande för sjukersättning                                 | 11   | 0              | 1                    | 0            | 0           | 0       | 2          | 8     | 0                  | 1       | 0        | 1            | 3        | 3             | 0          | 0             |
  | Läkarutlåtande för aktivitetsersättning vid förlängd skolgång     | 6    | 0              | 0                    | 0            | 0           | 0       | 1          | 8     | 0                  | 1       | 0        | 1            | 3        | 3             | 1          | 1             |
  #| Läkarutlåtande för aktivitetsersättning vid nedsatt arbetsförmåga | 13    | 1              | 0                    | 0            | 0           | 0       | 2          | 7     | 0                  | 1       | 1        | 0            | 0        | 0             | 0          | 0             |

@F.VAL-044
Scenario: F.Val-044 - Intyget kan inte signeras om slut är före startdatum
    Givet att jag är inloggad som läkare
    Och jag går in på en patient
    Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
    När jag fyller i alla nödvändiga fält för intyget
    Och anger ett slutdatum som är tidigare än startdatum
    Och jag klickar på signera-knappen
    Så ska valideringsfelet "Startdatum får inte vara efter slutdatum" visas
