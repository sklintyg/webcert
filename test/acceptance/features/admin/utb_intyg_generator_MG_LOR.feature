# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data

# Marcus Gran TSTNMT2321000156-2014
# Linköpings Östra Rehabcentrum TSTNMT2321000156-2001
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Marcus Gran Linköpings Östra Rehabcentrum"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

Exempel:
  | Patient |
  | 19620323-3066 |
  | 19540187-5769 |
  | 19671031-3195 | 
  | 19900614-2385 |
  | 19550307-1770 | 
  | 19930905-2380 |
  | 19590714-2599 |
  | 19830923-9294 |
  

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
  | 19620323-3066 | 0            |
  | 19540187-5769 | 1            |
  | 19671031-3195 | 2            |
  | 19900614-2385 | 3            |
  | 19550307-1770 | 4            |
  | 19930905-2380 | 5            |
  | 19590714-2599 | 6            |
  | 19620323-3066 | 7            |
  | 19830923-9294 | 8            |
