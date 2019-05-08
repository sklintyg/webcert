# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data

# Per Svensson TSTNMT2321000156-2013
# Tallbackens Vårdcentral TSTNMT2321000156-1001
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Per Svensson"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

Exempel:
  | Patient |
  | 19550307-1770 |
  | 19920205-2396 |
  | 19510713-2119 | 
  | 19520513-1575 |
  | 19630102-2866 |
  | 19810423-9267 |
  | 19930301-2398 |
  | 19931230-2384 |
  | 19900511-2389 |
  | 19520712-2184 |


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
  | 19550307-1770 | 0            |
  | 19920205-2396 | 1            |
  | 19510713-2119 | 2            |
  | 19520513-1575 | 3            |
  | 19630102-2866 | 4            | 
  | 19630102-2866 | 5            |
  | 19630102-2866 | 6            | 
  | 19810423-9267 | 7            |
  | 19810423-9267 | 8            |
  | 19930301-2398 | 9            |
  | 19931230-2384 | 10           | 
  | 19931230-2384 | 11           |
  | 19520513-1575 | 12           |
  | 19900511-2389 | 13           |
  | 19520712-2184 | 14           |
