# language: sv
@BACKOFFICE
Egenskap: Generera Utb-data för Leonie Keohl

# Leonie Keohl TSTNMT2321000156-103F
# TSTNMT2321000156-1039
Bakgrund: Inloggad som läkare
   Givet att jag är inloggad som läkare "Leonie Keohl"


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
   Och jag fyller i alla nödvändiga fält för intyget med utbdata för Leonie Keohl "<utbDataIndex>"
   Så är signeraknappen tillgänglig
   När jag signerar intyget

Exempel:
  | Patient       | utbDataIndex |
  | 19870410-2386 | 0            | 
  | 19660407-2667 | 1            |
  | 19620323-3066 | 2            |
  | 19540187-5769 | 3            |
  | 19550307-1770 | 4            |


