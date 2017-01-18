# language: sv

@kopiera-knapp
Egenskap: Makulerat intyg ska kunna kopieras

Bakgrund: Jag befinner mig på webcerts förstasida
   Givet att jag är inloggad som läkare
   När jag går in på en patient

@FK7263-kopiera-knapp
Scenario: Det ska gå att kopiera ett makulerat FK7263 intyg
  När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
  Och jag skickar intyget till Försäkringskassan
  Och jag makulerar intyget
  Så ska det inte finnas en knapp med texten "Kopiera"

@SMI-kopiera-knapp
Scenario: Det ska gå att kopiera slumpat och makulerat SMI-intyg
   När jag går in på ett slumpat SMI-intyg med status "Signerat"
   Och jag skickar intyget till Försäkringskassan
   Och jag makulerar intyget
   Så ska intyget visa varningen "Intyget är makulerat"
   Så ska det finnas en knapp med texten "Kopiera"


   Scenariomall: Utkast för <intygKod> ska inte kunna makuleras
  När jag går in på att skapa ett <intyg> intyg
  Så ska det inte finnas någon knapp för "makulera"

Exempel:
  |intygKod |   intyg                        |
  |FK7263 |   "Läkarintyg FK 7263"           |
  |LISJP    |   "Läkarintyg för sjukpenning" |

 @upplys-om-makulering
  Scenario: Användaren ska upplysas om att intyget makulerats
  När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
  Och jag makulerar intyget
  Så ska intyget visa varningen "Intyget är makulerat"

  Scenario: Användaren ska kunna skriva ut ett makulerat intyg
  När jag går in på ett "Läkarintyg FK 7263" med status "Signerat"
  Och jag makulerar intyget
  Så ska det finnas en knapp med texten "Skriv ut"

@notReady
  Scenario: Ersatt intyg vid makulering ska innehålla uppdaterade personuppgifter
  När jag skickar ett intyg med ändrade personuppgifter till Intygstjänsten
  Och jag går in på intyget
  Och jag makulerar intyget och ersätter med nytt intyg
  Så ska intyget inte innehålla gamla personuppgifter
