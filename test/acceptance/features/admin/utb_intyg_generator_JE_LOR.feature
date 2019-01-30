# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data

# Jane Ewery TSTNMT2321000156-2011
# Linköpings Östra Rehabcentrum TSTNMT2321000156-2001
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Jane Ewery Linköpings Östra Rehabcentrum"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

Exempel:
  | Patient |
  | 19671031-3195 |
  | 19900614-2385 |
  | 19550307-1770 |
  | 19590714-2599 |
  | 19630120-2633 |
  | 19670505-3723 |
  | 19710116-9295 |
  | 19541006-2656 |
  | 19520516-2331 |
  | 19830923-9294 |

  
@UTBDATA-REHABSTOD
Scenariomall: [REHABSTOD] - Generera intyg för <Patient> med utbDataIndex <utbDataIndex>
   
   När jag går in på testpatienten "<Patient>"
   Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
   Och jag fyller i alla nödvändiga fält för intyget med utbdata för Jane Ewery "<utbDataIndex>"
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Exempel:
  | Patient | utbDataIndex |
  | 19671031-3195 | 0 | 
  | 19900614-2385 | 1 |
  | 19550307-1770 | 2 |
  | 19590714-2599 | 3 | 
  | 19630120-2633 | 4 |
  | 19630120-2633 | 5 |
  | 19630120-2633 | 6 |
  | 19670505-3723 | 7 |
  | 19710116-9295 | 8 |
  | 19541006-2656 | 9 |
  | 19520516-2331 | 10 |
  | 19830923-9294 | 11 |



