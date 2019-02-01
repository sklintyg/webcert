# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data

# Marcus Gran TSTNMT2321000156-2014
# Linköpings Östra Rehabcentrum TSTNMT2321000156-2001
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Marcus Gran"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

# Ändrad testpersoner (PU-stubbe)
Exempel:
  | Patient |
  | 19770523-2382 |
  | 19540187-5769 |
  | 19900825-2398 | 
  | 19960811-2380 |
  | 19520727-2252 | 
  | 19460610-9108 |
  | 19520614-2597 |
  | 19840820-9990 |
  

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
  | 19770523-2382 | 0            |
  | 19540187-5769 | 1            |
  | 19900825-2398 | 2            |
  | 19960811-2380 | 3            |
  | 19520727-2252 | 4            |
  | 19460610-9108 | 5            |
  | 19520614-2597 | 6            |
  | 19770523-2382 | 7            |
  | 19840820-9990 | 8            |
