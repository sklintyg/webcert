# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data för Annika Larsson (TV)

# Annika Larsson TSTNMT2321000156-10CD
# Tallbackens Vårdcentral TSTNMT2321000156-1001
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Annika Larsson"


@UTBDATA-REHABSTOD-RADERA
Scenariomall: Ta bort intyg för <Patient>
   Givet att jag har raderat alla intyg för "<Patient>" via testAPI
   Och att jag har raderat alla utkast för "<Patient>" via testAPI   

Exempel:
  | Patient | 
  | 19930905-2380 |
  | 19620323-3066 |
  | 19810423-9267 | 
  | 19950119-2380 |
  | 19541006-2656 | 
  | 19550607-2882 |
  | 19591016-2642 |
  | 19530807-2395 |
  | 19931230-2384 |
  | 19851021-9994 |
  | 19540187-5769 |
  | 19870410-2386 |
  | 19950119-2380 |
  | 19911124-2393 |
  | 19890725-2392 |
 

@UTBDATA-REHABSTOD
Scenariomall: [REHABSTOD] - Generera intyg för <Patient> med utbDataIndex <utbDataIndex>
   
   När jag går in på testpatienten "<Patient>"
   Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
   Och jag fyller i alla nödvändiga fält för intyget med utbdata för Annika Larsson "<utbDataIndex>"
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Exempel:
  | Patient       | utbDataIndex |
  | 19930905-2380 | 0            |
  | 19930905-2380 | 1            |
  | 19930905-2380 | 2            |
  | 19930905-2380 | 3            |
  | 19620323-3066 | 4            |
  | 19810423-9267 | 5            |  
  | 19950119-2380 | 6            |
  | 19930905-2380 | 7            |
  | 19541006-2656 | 8            | 
  | 19550607-2882 | 9            |
  | 19930905-2380 | 10           |
  | 19591016-2642 | 11           |
  | 19530807-2395 | 12           |
  | 19931230-2384 | 13           |
  | 19851021-9994 | 14           |
  | 19540187-5769 | 15           |
  | 19870410-2386 | 16           |
  | 19950119-2380 | 17           |
  | 19930905-2380 | 18           |
  | 19911124-2393 | 19           |
  | 19591016-2642 | 20           |
  | 19890725-2392 | 21           |
  | 19870410-2386 | 22           |
  | 19931230-2384 | 23           |
  | 19931230-2384 | 24           |


