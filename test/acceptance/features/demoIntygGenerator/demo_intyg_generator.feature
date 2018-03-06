# language: sv
@backoffice @notReady
Egenskap: Generera Demo-data

Bakgrund: Inloggad som uthoppsläkare
   Givet att jag är inloggad som läkare "Ingrid Nilsson Olsson"

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
  | 19790124-9297 | 0 |
  | 19940901-2383 | 1 |
  | 19970328-2393 | 2 |
  | 19931209-2381 | 3 |
  | 19641024-3056 | 4 |
  | 19641024-3056 | 5 |
  | 19641024-3056 | 6 |
  | 19920815-2398 | 7 |
  | 19920815-2398 | 8 |
  | 19920815-2398 | 9 |
  | 19920815-2398 | 10 |
  | 19920815-2398 | 11 |
  | 19930419-2397 | 12 |
  | 19940421-2392 | 13 |
  | 19950725-2394 | 14 |
  | 19971008-2398 | 15 |
  | 19971008-2398 | 16 |
  | 19971008-2398 | 17 |
  | 19950917-2392 | 18 |
  | 19950917-2392 | 19 |
  | 19900920-2392 | 20 |
  | 19900628-2389 | 21 |
  | 19900628-2389 | 22 |
  | 19590313-3279 | 23 |
  | 19590313-3279 | 24 |
  | 19601027-2661 | 25 |
  | 19601027-2661 | 26 |
  | 19601027-2661 | 27 |
  
# Extra Demo patienter #######
#  | 19900731-2383 |  |
#  | 19960629-2390 |  |
#  | 19900301-2391 |  |
#  | 19930308-2391 |  |
#  | 19910515-2392 |  |
#  | 19950402-2394 |  |
#  | 19910120-2399 |  |
#  | 19951013-2393 |  |
#  | 19940517-2397 |  |
#  | 19901226-2391 |  |
#  | 19910116-2387 |  |
#  | 19910717-2398 |  |
#  | 19990526-2383 |  |
#  | 19930830-2398 |  |
#  | 19980828-2397 |  |
#  | 19911202-2398 |  |
#  | 19980327-2393 |  |
#  | 19940902-2390 |  |
#  | 19940902-2390 |  |