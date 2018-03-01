# language: sv
@backoffice @notReady
Egenskap: Generera Demo-data

Bakgrund: Inloggad som uthoppsläkare
   Givet att jag är inloggad som läkare

@demoRehabstöd
Scenariomall: Generera intyg för <Patient> med demoDataIndex <demoDataIndex>
   När jag går in på testpatienten "<Patient>"
   Och jag går in på att skapa ett "Läkarintyg för sjukpenning" intyg
   Och jag fyller i alla nödvändiga fält för intyget med demodata "<demoDataIndex>"
   Så är signeraknappen tillgänglig
   När jag signerar intyget
   Så ska jag se den data jag angett för intyget

Exempel:
  | Patient | demoDataIndex |
  | 19920205-2396 | 0 |
  | 19890725-2392 | 1 |
  | 19900511-2389 | 2 |
  | 19810423-9267 | 3 |
  | 19000101-9801 | 4 |
  | 19000101-9801 | 5 |
  | 19000101-9801 | 6 |
  | 19520617-2339 | 7 |
  | 19520617-2339 | 8 |
  | 19520617-2339 | 9 |
  | 19520617-2339 | 10 |
  | 19520617-2339 | 11 |
  | 19911124-2393 | 12 |
  | 19620323-3066 | 13 |
  | 19550607-2882 | 14 |
  | 19931230-2384 | 15 |
  | 19931230-2384 | 16 |
  | 19931230-2384 | 17 |
  | 19950119-2380 | 18 |
  | 19950119-2380 | 19 |
  | 19851021-9994 | 20 |
  | 19870410-2386 | 21 |
  | 19870410-2386 | 22 |
  | 19630102-2866 | 23 |
  | 19630102-2866 | 24 |
  | 19930905-2380 | 25 |
  | 19930905-2380 | 26 |
  | 19930905-2380 | 27 |