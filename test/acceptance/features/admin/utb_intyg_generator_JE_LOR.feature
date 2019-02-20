# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data

# Jane Ewery TSTNMT2321000156-2011
# Linköpings Östra Rehabcentrum TSTNMT2321000156-2001
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Jane Ewery"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

# Ändrad testpersoner (PU-stubbe)
Exempel:
  | Patient |
  | 19900825-2398 |
  | 19960811-2380 |
  | 19520727-2252 |
  | 19520614-2597 |
  | 19121212-9999 |
  | 19520712-2184 |
  | 19640516-5942 |
  | 19000525-9809 |
  | 19520617-2339 |
  | 19840820-9990 |

  
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
  | 19900825-2398 | 0 | 
  | 19960811-2380 | 1 |
  | 19520727-2252 | 2 |
  | 19520614-2597 | 3 | 
  | 19121212-9999 | 4 |
  | 19121212-9999 | 5 |
  | 19121212-9999 | 6 |
  | 19520712-2184 | 7 |
  | 19640516-5942 | 8 |
  | 19000525-9809 | 9 |
  | 19520617-2339 | 10 |
  | 19840820-9990 | 11 |



