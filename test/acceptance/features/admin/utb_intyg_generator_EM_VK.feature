# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data för Eva Måne

# Eva Måne TSTNMT2321000156-2014
# Vårdcentralen Klövern TSTNMT2321000156-1002
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Eva Måne"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

Exempel:
  | Patient |  
  | 19870410-2386 |  
  | 19660407-2667 |
  | 19620323-3066 |
  | 19540187-5769 |  
  | 19550307-1770 |  

@UTBDATA-REHABSTOD
Scenariomall: [REHABSTOD] - Generera intyg för <Patient> med utbDataIndex <utbDataIndex>
   
   När jag går in på testpatienten "<Patient>"
   Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
   Och jag fyller i alla nödvändiga fält för intyget med utbdata för Marcus Gran "<utbDataIndex>"
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Exempel:
  | Patient       | utbDataIndex |
  | 19870410-2386 | 0            | 
  | 19660407-2667 | 1            |
  | 19620323-3066 | 2            |
  | 19540187-5769 | 3            |
  | 19550307-1770 | 4            |


